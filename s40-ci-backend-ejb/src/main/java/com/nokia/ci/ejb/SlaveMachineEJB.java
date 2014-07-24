/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SlaveInstance;
import com.nokia.ci.ejb.model.SlaveMachine;
import com.nokia.ci.ejb.model.SlaveMachine_;
import com.nokia.ci.ejb.model.SlaveStatPerMachine;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class SlaveMachineEJB
 *
 * @author aklappal
 * @since Feb 7, 2013
 */
@Stateless
@LocalBean
public class SlaveMachineEJB extends CrudFunctionality<SlaveMachine> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(SlaveMachineEJB.class);

    public SlaveMachineEJB() {
        super(SlaveMachine.class);
        log.debug("SlaveMachineEJB()");
    }

    public List<SlaveMachine> getSlaveMachines() {
        log.debug("SlaveMachineEJB.getSlaveMachines()");

        CriteriaQuery<SlaveMachine> query = cb.createQuery(SlaveMachine.class);
        Root<SlaveMachine> slaveMachineRoot = query.from(SlaveMachine.class);

        query.select(slaveMachineRoot);
        query.orderBy(cb.asc(slaveMachineRoot.get(SlaveMachine_.url)));

        log.debug("SlaveMachineEJB.getSlaveMachines() return(" + em.createQuery(query).getResultList() + ")");
        return em.createQuery(query).getResultList();

    }

    /**
     * Query a list of SlaveMachines that are enabled from CI UI.
     *
     * @return
     */
    public List<SlaveMachine> getEnabledSlaveMachines() {
        log.debug("SlaveMachineEJB.getEnabledSlaveMachines()");

        CriteriaQuery<SlaveMachine> query = cb.createQuery(SlaveMachine.class);
        Root<SlaveMachine> slaveMachineRoot = query.from(SlaveMachine.class);

        Predicate wherePredicate = cb.or(cb.equal(slaveMachineRoot.get(SlaveMachine_.disabled), false),
                cb.isNull(slaveMachineRoot.get(SlaveMachine_.disabled)));

        query.select(slaveMachineRoot);
        query.where(wherePredicate);

        log.debug("SlaveMachineEJB.getEnabledSlaveMachines() return(" + em.createQuery(query).getResultList() + ")");
        return em.createQuery(query).getResultList();

    }

    /**
     * Query a list of SlaveMachines.
     *
     * @return String list of SlaveMachine url's as result.
     */
    public List<String> querySlaveMachinesList() {
        log.debug("SlaveInstanceEJB.querySlaveMachinesList()");

        List<SlaveMachine> slaveMachines = getEnabledSlaveMachines();
        Set<String> foundSlaves = new HashSet<String>();

        for (SlaveMachine sm : slaveMachines) {
            String slave = sm.getUrl();

            if (!StringUtils.isEmpty(slave)) {
                foundSlaves.add(slave);
            }
        }

        List<String> results = new ArrayList<String>();

        if (foundSlaves != null && !foundSlaves.isEmpty()) {
            for (String s : foundSlaves) {
                results.add(s);
            }
        }

        log.debug("Found {} slaves", results.size());
        return results;
    }

    /**
     * Set selected SlaveMachine status to disabled.
     *
     * @param slave
     * @throws NotFoundException
     */
    public void disableSlaveMachine(String slave) throws NotFoundException {
        SlaveMachine slaveMachine = new SlaveMachine();

        if (!StringUtils.isEmpty(slave)) {
            slaveMachine = getSlaveMachineByURL(slave);
        }

        if (slaveMachine != null) {
            slaveMachine.setDisabled(Boolean.TRUE);
        }
    }

    public void addSlaveInstance(Long id, SlaveInstance slaveInstance) throws NotFoundException {
        SlaveMachine sm = read(id);
        RelationUtil.relate(sm, slaveInstance);
    }

    public List<SlaveInstance> getSlaveInstances(Long id) throws NotFoundException {
        return getJoinList(id, SlaveMachine_.slaveInstances);
    }

    public Long getSlaveInstanceCount(Long id) {
        return getJoinListCount(id, SlaveMachine_.slaveInstances);
    }

    public void detachSlaveInstance(SlaveInstance slaveInstance) {
        SlaveMachine slaveMachine = slaveInstance.getSlaveMachine();
        RelationUtil.unrelate(slaveMachine, slaveInstance);
    }

    public SlaveMachine getSlaveMachineByURL(String url) {
        log.debug("SlaveMachineEJB.getSlaveMachineByURL()");

        if (url == null) {
            return null;
        }

        CriteriaQuery<SlaveMachine> cq = cb.createQuery(SlaveMachine.class);
        Root<SlaveMachine> root = cq.from(SlaveMachine.class);

        cq.select(root);
        cq.where(cb.equal(root.get(SlaveMachine_.url), url));

        SlaveMachine slaveMachine = em.createQuery(cq).getResultList().get(0);
        log.debug("SlaveMachineEJB.getSlaveMachineByURL() return(" + slaveMachine + ")");

        return slaveMachine;
    }

    public SlaveMachine addSlaveStat(Long id, SlaveStatPerMachine slaveStatPerMachine) throws NotFoundException {
        log.debug("Received {} slave stat for machine {}", id);
        long startTime = System.currentTimeMillis();

        SlaveMachine slaveMachine = read(id);

        RelationUtil.relate(slaveMachine, slaveStatPerMachine);

        log.debug("Finished adding slave stat for machine {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return slaveMachine;
    }
}
