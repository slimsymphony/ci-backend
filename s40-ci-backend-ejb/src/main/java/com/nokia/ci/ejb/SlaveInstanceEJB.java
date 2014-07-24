/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SlaveInstance;
import com.nokia.ci.ejb.model.SlaveInstance_;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.SlaveLabel_;
import com.nokia.ci.ejb.model.SlaveMachine;
import com.nokia.ci.ejb.model.SlaveMachine_;
import com.nokia.ci.ejb.model.SlavePool;
import com.nokia.ci.ejb.model.SlavePool_;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.util.MapValueComparator;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jaakkpaa
 */
@Stateless
@LocalBean
public class SlaveInstanceEJB extends CrudFunctionality<SlaveInstance> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(SlaveInstanceEJB.class);
    private static final long SLAVE_RESERVATION_TIMEOUT_DEFAULT = 3 * 60 * 60 * 1000;
    @EJB
    SysConfigEJB sysConfigEJB;

    public SlaveInstanceEJB() {
        super(SlaveInstance.class);
    }

    /**
     * Provides suitable slave instances but not yet reserves them.
     *
     * @param slavePool Name of slave pool
     * @param slaveLabel Name of slave label
     * @param requestedAmount Amount of requested slave instances
     * @return List of suitable slave instances
     */
    public List<SlaveInstance> provideSlaveInstances(String slavePool, String slaveLabel, int requestedAmount) {
        long startTime = System.currentTimeMillis();

        List<SlaveInstance> compatibleSlaveInstances = getAvailableSlaveInstances(slavePool, slaveLabel);
        log.info("Found {} compatible slaves with pool {} and label {}", new Object[]{compatibleSlaveInstances.size(), slavePool, slaveLabel});
        List<SlaveInstance> providedSlaveInstances = getLoadBalancedSlaveInstances(compatibleSlaveInstances, requestedAmount);

        log.info("Provided {} slave instances in {}ms", providedSlaveInstances.size(), System.currentTimeMillis() - startTime);
        return providedSlaveInstances;
    }

    /**
     * Returns a List of SlaveInstances
     */
    public List<SlaveInstance> getSlaveInstances() {
        log.debug("SlaveInstanceEJB.getSlaveInstances()");

        CriteriaQuery<SlaveInstance> query = cb.createQuery(SlaveInstance.class);
        Root<SlaveInstance> slaveInstanceRoot = query.from(SlaveInstance.class);

        query.select(slaveInstanceRoot);
        query.orderBy(cb.asc(slaveInstanceRoot.get(SlaveInstance_.slaveMachine)));

        List<SlaveInstance> results = em.createQuery(query).getResultList();
        log.debug("Found {} slaveInstances", results.size());

        return results;
    }

    /**
     * Returns a List of SlaveInstances
     */
    public List<SlaveInstance> getSlaveInstancesReservedForMaster(String master) {
        log.debug("SlaveInstanceEJB.getSlaveInstancesForMaster(): {}", master);
        if (StringUtils.isEmpty(master)) {
            log.warn("Given master was null or empty!");
            return new ArrayList<SlaveInstance>();
        }

        CriteriaQuery<SlaveInstance> query = cb.createQuery(SlaveInstance.class);
        Root<SlaveInstance> slaveInstanceRoot = query.from(SlaveInstance.class);
        Predicate wherePredicate = cb.equal(slaveInstanceRoot.get(SlaveInstance_.currentMaster), master);
        query.select(slaveInstanceRoot);
        query.where(wherePredicate);
        query.orderBy(cb.asc(slaveInstanceRoot.get(SlaveInstance_.slaveMachine)));

        List<SlaveInstance> results = em.createQuery(query).getResultList();
        log.debug("Found {} slaveInstances for master {}", results.size(), master);

        return results;
    }

    public List<String> getAllCurrentMasters() {
        log.debug("SlaveInstanceEJB.getAllMasters()");

        List<SlaveInstance> slaveInstances = getSlaveInstances();
        Set<String> foundMasters = new HashSet<String>();

        for (SlaveInstance si : slaveInstances) {
            String master = si.getCurrentMaster();

            if (!StringUtils.isEmpty(master)) {
                foundMasters.add(master);
            }
        }

        List<String> results = new ArrayList<String>();

        if (foundMasters != null && !foundMasters.isEmpty()) {
            for (String master : foundMasters) {
                results.add(master);
            }
        }

        log.debug("Found {} masters", results.size());

        return results;
    }

    /**
     * Detach all slaveinstances from selected master
     *
     * @param master
     * @throws NotFoundException
     */
    public void detachAllSlaveInstancesFromMaster(String master) {
        List<SlaveInstance> slaveInstances = new ArrayList<SlaveInstance>();

        if (StringUtils.isNotEmpty(master)) {
            slaveInstances = getSlaveInstancesReservedForMaster(master);
        }

        if (slaveInstances != null || !slaveInstances.isEmpty()) {
            for (SlaveInstance si : slaveInstances) {
                try {
                    detach(si.getId());
                } catch (NotFoundException e) {
                    log.error("Slave instance with id {} could not be found", si.getId());
                }
            }
        }
    }

    /**
     * Get available slave instances from given pool.
     *
     * @param slavePool Given slave pool name
     * @param slaveLabel Given slave label name
     * @return List of available slave instances
     */
    public List<SlaveInstance> getAvailableSlaveInstances(String slavePool, String slaveLabel) {
        log.debug("SlaveInstanceEJB.getAvailableSlaveInstances()");
        CriteriaQuery<SlaveInstance> query = cb.createQuery(SlaveInstance.class);
        Root<SlaveInstance> slaveInstanceRoot = query.from(SlaveInstance.class);

        Join<SlaveInstance, SlaveMachine> machinejoin = slaveInstanceRoot.join(SlaveInstance_.slaveMachine);
        Join<SlaveInstance, SlavePool> pooljoin = slaveInstanceRoot.join(SlaveInstance_.slavePools);

        Predicate slaveMachineNotDisabled = cb.or(cb.equal(machinejoin.get(SlaveMachine_.disabled), false),
                cb.isNull(machinejoin.get(SlaveMachine_.disabled)));

        Predicate wherePredicate = cb.and(cb.isNull(slaveInstanceRoot.get(SlaveInstance_.currentMaster)),
                cb.equal(pooljoin.get(SlavePool_.name), slavePool), slaveMachineNotDisabled);

        if (StringUtils.isEmpty(slaveLabel) || slaveLabel.equals("null")) {
            wherePredicate = cb.and(wherePredicate, cb.isEmpty(slaveInstanceRoot.get(SlaveInstance_.slaveLabels)));
        } else {
            Join<SlaveInstance, SlaveLabel> labeljoin = slaveInstanceRoot.join(SlaveInstance_.slaveLabels);
            wherePredicate = cb.and(wherePredicate, cb.equal(labeljoin.get(SlaveLabel_.name), slaveLabel));
        }

        query.select(slaveInstanceRoot);
        query.where(wherePredicate);

        List<SlaveInstance> queryResults = em.createQuery(query).getResultList();
        log.debug("Found {} slaveInstances", queryResults.size());
        return queryResults;
    }

    /**
     * Returns a List of reserved SlaveInstances
     */
    public List<SlaveInstance> getReservedSlaveInstances(String slavePool, String slaveLabel) {
        CriteriaQuery<SlaveInstance> query = cb.createQuery(SlaveInstance.class);
        Root<SlaveInstance> slaveInstanceRoot = query.from(SlaveInstance.class);
        slaveInstanceRoot.fetch(SlaveInstance_.slaveMachine);

        Join<SlaveInstance, SlavePool> pooljoin = slaveInstanceRoot.join(SlaveInstance_.slavePools);
        Predicate wherePredicate = cb.and(cb.isNotNull(slaveInstanceRoot.get(SlaveInstance_.currentMaster)), cb.equal(pooljoin.get(SlavePool_.name), slavePool));

        if (StringUtils.isEmpty(slaveLabel) || slaveLabel.equals("null")) {
            wherePredicate = cb.and(wherePredicate, cb.isEmpty(slaveInstanceRoot.get(SlaveInstance_.slaveLabels)));
        } else {
            Join<SlaveInstance, SlaveLabel> labeljoin = slaveInstanceRoot.join(SlaveInstance_.slaveLabels);
            wherePredicate = cb.and(wherePredicate, cb.equal(labeljoin.get(SlaveLabel_.name), slaveLabel));
        }

        query.where(wherePredicate);
        List<SlaveInstance> reservedSlaveInstances = em.createQuery(query).getResultList();

        log.debug("Found {} reserved slave instances.", reservedSlaveInstances.size());
        return reservedSlaveInstances;
    }

    public List<SlaveInstance> getAllSlaveInstances(String slavePool, String slaveLabel) {
        CriteriaQuery<SlaveInstance> query = cb.createQuery(SlaveInstance.class);
        Root<SlaveInstance> slaveInstanceRoot = query.from(SlaveInstance.class);
        slaveInstanceRoot.fetch(SlaveInstance_.slaveMachine);

        Join<SlaveInstance, SlavePool> pooljoin = slaveInstanceRoot.join(SlaveInstance_.slavePools);
        Predicate wherePredicate = cb.equal(pooljoin.get(SlavePool_.name), slavePool);

        if (StringUtils.isEmpty(slaveLabel) || slaveLabel.equals("null")) {
            wherePredicate = cb.and(wherePredicate, cb.isEmpty(slaveInstanceRoot.get(SlaveInstance_.slaveLabels)));
        } else {
            Join<SlaveInstance, SlaveLabel> labeljoin = slaveInstanceRoot.join(SlaveInstance_.slaveLabels);
            wherePredicate = cb.and(wherePredicate, cb.equal(labeljoin.get(SlaveLabel_.name), slaveLabel));
        }

        query.where(wherePredicate);
        List<SlaveInstance> allSlaveInstances = em.createQuery(query).getResultList();

        log.debug("Found {} reserved slave instances.", allSlaveInstances.size());
        return allSlaveInstances;
    }

    /**
     * Gets slave pools for given id
     *
     * @param id SlaveInstance id
     * @return List of SlavePools
     * @throws NotFoundException If given project not found.
     */
    public List<SlavePool> getSlavePools(Long id) throws NotFoundException {
        log.debug("SlaveInstanceEJB.getSlavePools(", id, ")");
        return getJoinList(id, SlaveInstance_.slavePools);
    }

    /**
     * setSlavePools
     *
     * @param id
     * @param slavePools
     * @throws NotFoundException
     */
    public void setSlavePools(Long id, List<SlavePool> slavePools) throws NotFoundException {
        SlaveInstance slave = read(id);
        List<SlavePool> managedSlavePools = new ArrayList<SlavePool>();
        for (SlavePool slavePool : slavePools) {
            managedSlavePools.add(em.find(SlavePool.class, slavePool.getId()));
        }
        for (SlavePool slavePool : managedSlavePools) {
            RelationUtil.relate(slave, slavePool);
        }
    }

    /**
     * Gets SlaveLabels for given id
     *
     * @param id SlaveInstance id
     * @return List of SlaveLabels
     * @throws NotFoundException If given project not found.
     */
    public List<SlaveLabel> getSlaveLabels(Long id) throws NotFoundException {
        log.debug("Querying slavelabels for id {}", id);
        return getJoinList(id, SlaveInstance_.slaveLabels);
    }

    /**
     * setSlaveLabels
     *
     * @param id
     * @param slaveLabels
     * @throws NotFoundException
     */
    public void setSlaveLabels(Long id, List<SlaveLabel> slaveLabels) throws NotFoundException {
        SlaveInstance slaveInstance = read(id);
        List<SlaveLabel> managedSlaveLabels = new ArrayList<SlaveLabel>();
        for (SlaveLabel slaveLabel : slaveLabels) {
            managedSlaveLabels.add(em.find(SlaveLabel.class, slaveLabel.getId()));
        }
        for (SlaveLabel slaveLabel : managedSlaveLabels) {
            RelationUtil.relate(slaveInstance, slaveLabel);
        }
    }

    /**
     * Returns all slaves which have been connected longer than expiration time
     *
     */
    public void detachExpiredSlaves() {
        // We want array of objects as result
        CriteriaQuery<SlaveInstance> query = cb.createQuery(SlaveInstance.class);

        // Original root table is Slave
        Root<SlaveInstance> slaveRoot = query.from(SlaveInstance.class);

        query.select(slaveRoot);
        long timeout = sysConfigEJB.getValue(SysConfigKey.SLAVE_RESERVATION_TIMEOUT,
                SLAVE_RESERVATION_TIMEOUT_DEFAULT);
        Date expirationDate = new Date(new Date().getTime() - timeout);

        query.where(cb.and(cb.isNotNull(slaveRoot.get(SlaveInstance_.currentMaster)),
                cb.lessThan(slaveRoot.get(SlaveInstance_.reserved), expirationDate)));

        List<SlaveInstance> results = em.createQuery(query).getResultList();

        for (SlaveInstance slaveInstance : results) {
            try {
                detach(slaveInstance.getId());
            } catch (NotFoundException e) {
                log.error("Slave instance with id {} could not be found", slaveInstance.getId());
            }
        }

        log.info("Detached {} expired slave reservations", results.size());
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void detach(Long id) throws NotFoundException {
        try {
            SlaveInstance slaveInstance = readWithLock(id, LockModeType.OPTIMISTIC);
            slaveInstance.setCurrentMaster(null);
            slaveInstance.setReserved(null);
            log.debug("Detached expired reservation of slave instance: {}", slaveInstance);
        } catch (OptimisticLockException ole) {
            log.info("Already detached {} by another transaction!", ole.getEntity());
        }
    }

    public List<SlaveInstance> getLoadBalancedSlaveInstances(List<SlaveInstance> compatibleSlaveInstances, int requestedAmount) {
        List<SlaveInstance> reservedSlaveInstances = new ArrayList<SlaveInstance>();

        //Randomize order of slave instances to minimize collisions in slave instance reservation
        //if multiple masters are trying to reserve slave instances same time
        Collections.shuffle(compatibleSlaveInstances);

        Map<String, List<SlaveInstance>> slaveInstanceMap = new HashMap<String, List<SlaveInstance>>();
        for (SlaveInstance slaveInstance : compatibleSlaveInstances) {
            if (Boolean.TRUE.equals(slaveInstance.getSlaveMachine().getDisabled())) {
                log.info("SlaveMachine {} was disabled -- skipping from loadbalancing.", slaveInstance.getSlaveMachine().getUrl());
                continue;
            }
            List<SlaveInstance> slaveInstances = slaveInstanceMap.get(slaveInstance.getSlaveMachine().getUrl());
            if (slaveInstances == null) {
                slaveInstances = new ArrayList<SlaveInstance>();
            }
            slaveInstances.add(slaveInstance);
            slaveInstanceMap.put(slaveInstance.getSlaveMachine().getUrl(), slaveInstances);
        }

        MapValueComparator<String, List<SlaveInstance>> mapComparator = new MapValueComparator<String, List<SlaveInstance>>();
        mapComparator.setMap(slaveInstanceMap);
        TreeMap<String, List<SlaveInstance>> sortedSlaveTreeMap = new TreeMap<String, List<SlaveInstance>>(mapComparator);

        while (reservedSlaveInstances.size() < requestedAmount) {
            sortedSlaveTreeMap.clear();
            sortedSlaveTreeMap.putAll(slaveInstanceMap);
            if (sortedSlaveTreeMap.isEmpty()) {
                break;
            }
            String slaveMachine = sortedSlaveTreeMap.lastKey();
            List<SlaveInstance> slaveInstances = slaveInstanceMap.get(slaveMachine);
            if (!slaveInstances.isEmpty()) {
                SlaveInstance slaveInstance = slaveInstances.get(0);
                reservedSlaveInstances.add(slaveInstance);
                slaveInstances.remove(slaveInstance);
            }

            if (slaveInstances.isEmpty()) {
                slaveInstanceMap.remove(slaveMachine);
            }
        }
        return reservedSlaveInstances;
    }

    /**
     * Tries to reserve slave instance atomically for given master. Reservation
     * might fail: If there is no slave instance with given id or slave instance
     * is already reserved.
     *
     * @param id Slave instance id to reserve
     * @param master Name of master that is reserving slave instance
     * @return {@code true} only if reserving was successfully made.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean reserveSlaveInstanceAtomic(Long id, String master) {
        try {
            SlaveInstance slaveInstance = readWithLock(id, LockModeType.OPTIMISTIC);
            if (!StringUtils.isEmpty(slaveInstance.getCurrentMaster())) {
                log.info("Slave instance {} is already reserved for {}!", slaveInstance, slaveInstance.getCurrentMaster());
                return false;
            }
            slaveInstance.setCurrentMaster(master);
            slaveInstance.setReserved(new Date());
            em.flush();
            return true;
        } catch (NotFoundException nfe) {
            log.warn("Not found! {}", nfe.getMessage());
        } catch (OptimisticLockException ole) {
            log.info("Already updated {} by another transaction!", ole.getEntity());
        }
        return false;
    }
}
