/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.CIServer;
import com.nokia.ci.ejb.model.CIServer_;
import com.nokia.ci.ejb.model.SlaveInstance;
import com.nokia.ci.ejb.model.SlavePool;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class CIServerEJB extends CrudFunctionality<CIServer> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(CIServerEJB.class);
    @EJB
    SysConfigEJB sysConfigEJB;
    @EJB
    SlaveInstanceEJB slaveInstanceEJB;
    @EJB
    CIServerLoadEJB ciServerLoadEJB;

    public CIServerEJB() {
        super(CIServer.class);
    }

    public CIServer getCIServerByUuid(String uuid) {
        if (StringUtils.isEmpty(uuid)) {
            return null;
        }

        log.debug("Finding CIServer with uuid {}", uuid);
        CriteriaQuery<CIServer> query = cb.createQuery(CIServer.class);
        Root<CIServer> root = query.from(CIServer.class);

        query.where(cb.equal(root.get(CIServer_.uuid), uuid));

        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException nre) {
            log.debug("No verification found with uuid {}", uuid);
        }

        return null;
    }

    public void startBuild(Long id) throws NotFoundException {
        ciServerLoadEJB.startBuild(id);
    }

    public void finalizeBuild(String uuid) {
        try {
            CIServer ciServer = getCIServerByUuid(uuid);
            ciServerLoadEJB.finalizeBuild(ciServer.getId());
        } catch (NotFoundException e) {
            log.error("Could not finalize build on CI Server with uuid " + uuid + " {}", e);
        }
    }

    public CIServer resolveCIServer(List<CIServer> ciServers) {
        return resolveCIServer(ciServers, null);
    }

    public CIServer resolveCIServer(List<CIServer> ciServers, List<Build> builds) {

        if (ciServers == null) {
            return null;
        }

        // remove disabled CIServers from list
        Iterator<CIServer> i = ciServers.iterator();
        while (i.hasNext()) {
            CIServer ciServer = i.next();
            if (ciServer.getDisabled()) {
                i.remove();
            }
        }

        if (ciServers.isEmpty()) {
            return null;
        }

        Set<SlavePool> availableSlavePools = new HashSet<SlavePool>();
        Set<SlavePool> validSlavePools = new HashSet<SlavePool>();

        for (CIServer ciServer : ciServers) {
            if (ciServer.getSlavePool() != null) {
                availableSlavePools.add(ciServer.getSlavePool());
            }
        }

        if (builds == null || builds.isEmpty() || availableSlavePools.isEmpty()) {
            return getBestCIServer(ciServers);
        }

        Set<String> requiredSlaveLabels = new HashSet<String>();

        for (SlavePool slavePool : availableSlavePools) {
            boolean isSlavePoolValid = false;

            for (Build build : builds) {
                isSlavePoolValid = false;
                Set<String> slaveLabelNames = build.getBuildVerificationConf().getLabelNames();

                if (slaveLabelNames.isEmpty()) {
                    List<SlaveInstance> slaveInstances = slaveInstanceEJB.getAllSlaveInstances(slavePool.getName(), null);

                    if (!slaveInstances.isEmpty()) {
                        isSlavePoolValid = true;
                        requiredSlaveLabels.add("null");
                    }
                }

                for (String slaveLabelName : slaveLabelNames) {
                    List<SlaveInstance> slaveInstances = slaveInstanceEJB.getAllSlaveInstances(slavePool.getName(), slaveLabelName);

                    if (!slaveInstances.isEmpty()) {
                        isSlavePoolValid = true;
                        requiredSlaveLabels.add(slaveLabelName);
                        break;
                    }
                }
                if (!isSlavePoolValid) {
                    break;
                }
            }

            if (isSlavePoolValid) {
                validSlavePools.add(slavePool);
            }

        }

        Map<SlavePool, Integer> slavePoolLabelAmounts = new HashMap<SlavePool, Integer>();

        for (String slaveLabelName : requiredSlaveLabels) {
            for (SlavePool slavePool : validSlavePools) {

                List<SlaveInstance> currentSlaveInstancesList = slaveInstanceEJB.getAvailableSlaveInstances(slavePool.getName(), slaveLabelName);

                if (!slavePoolLabelAmounts.containsKey(slavePool)) {
                    slavePoolLabelAmounts.put(slavePool, currentSlaveInstancesList.size());
                } else {
                    Integer amount = slavePoolLabelAmounts.get(slavePool);
                    amount += currentSlaveInstancesList.size();
                    slavePoolLabelAmounts.put(slavePool, amount);
                }
            }
        }

        Map.Entry<SlavePool, Integer> maxEntry = null;

        for (Map.Entry<SlavePool, Integer> entry : slavePoolLabelAmounts.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }

        if (maxEntry == null || maxEntry.getKey() == null) {
            return getBestCIServer(ciServers);
        }

        SlavePool selectedSlavePool = maxEntry.getKey();
        List<CIServer> validCiServers = new ArrayList<CIServer>();

        for (CIServer ciServer : ciServers) {
            if (ciServer.getSlavePool() == null) {
                continue;
            }

            if (ciServer.getSlavePool().equals(selectedSlavePool)) {
                validCiServers.add(ciServer);
            }
        }

        // Try to get best from valid servers
        CIServer server = getBestCIServer(validCiServers);

        // If not possible, get best from all servers
        if (server == null) {
            server = getBestCIServer(ciServers);
        }

        return server;
    }

    private CIServer getBestCIServer(List<CIServer> ciServers) {

        if (ciServers == null || ciServers.isEmpty()) {
            return null;
        }

        CIServer ciServer = ciServers.get(0);

        if (ciServers.size() == 1) {
            return ciServer;
        }

        int maxLoad = ciServer.getBuildsRunning();
        for (CIServer c : ciServers) {
            if (c.getBuildsRunning() < maxLoad) {
                ciServer = c;
                maxLoad = c.getBuildsRunning();
            }
        }

        log.debug("Resolved following server as result {}", ciServer.getUrl());

        return ciServer;
    }

    private CIServer getRandomCIServer(List<CIServer> ciServers) {
        if (ciServers == null || ciServers.isEmpty()) {
            return null;
        }

        Random random = new Random();
        final int index = random.nextInt(ciServers.size());
        CIServer ciServer = ciServers.get(index);
        return ciServer;
    }

    public List<Branch> getBranches(Long id) {
        return getJoinList(id, CIServer_.branches);
    }
}
