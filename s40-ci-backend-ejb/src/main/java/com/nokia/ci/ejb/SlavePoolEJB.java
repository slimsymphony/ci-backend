/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SlaveInstance;
import com.nokia.ci.ejb.model.SlaveInstance_;
import com.nokia.ci.ejb.model.SlavePool;
import com.nokia.ci.ejb.model.SlavePool_;
import com.nokia.ci.ejb.model.SlaveStatPerLabel;
import com.nokia.ci.ejb.model.SlaveStatPerPool;
import com.nokia.ci.ejb.model.SysConfigKey;

import com.nokia.ci.ejb.util.RelationUtil;
import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jaakkpaa
 */
@Stateless
@LocalBean
public class SlavePoolEJB extends CrudFunctionality<SlavePool> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(SlavePoolEJB.class);
    private static final int DEFAULT_MAX_RESERVED_SLAVE_INSTANCES_PER_MASTER = 300;

    @EJB
    SysConfigEJB sysConfigEJB;

    public SlavePoolEJB() {
        super(SlavePool.class);
    }

    /**
     * getSlavePoolWithName
     *
     * @param name
     * @return
     */
    public SlavePool getSlavePoolByName(String name) {

        log.debug("SlavePoolEJB().getSlavePoolByName()");
        CriteriaQuery<SlavePool> query = cb.createQuery(SlavePool.class);
        Root<SlavePool> root = query.from(SlavePool.class);
        query.where(cb.equal(root.get(SlavePool_.name), name));

        try {
            SlavePool slavePool = em.createQuery(query).getSingleResult();
            log.debug("SlavePoolEJB().getSlavePoolByName() return " + slavePool);
            return slavePool;
        } catch (NoResultException nre) {
            return null;
        }
    }

    public int getMaxSlaveInstancesLimit(String slavePool) {
        SlavePool pool = getSlavePoolByName(slavePool);
        if (pool == null || pool.getReservedSlaveInstancesLimit() == null) {
            return sysConfigEJB.getValue(SysConfigKey.MAX_RESERVED_SLAVE_INSTANCES_PER_MASTER, DEFAULT_MAX_RESERVED_SLAVE_INSTANCES_PER_MASTER);
        }
        return pool.getReservedSlaveInstancesLimit().intValue();
    }

    public List<SlaveInstance> getSlaveInstances(String poolName) {
        CriteriaQuery<SlaveInstance> query = cb.createQuery(SlaveInstance.class);
        Root<SlaveInstance> slaveInstanceRoot = query.from(SlaveInstance.class);
        slaveInstanceRoot.fetch(SlaveInstance_.slaveMachine);

        Join<SlaveInstance, SlavePool> pooljoin = slaveInstanceRoot.join(SlaveInstance_.slavePools);
        Predicate wherePredicate = cb.equal(pooljoin.get(SlavePool_.name), poolName);

        query.where(wherePredicate);
        List<SlaveInstance> allSlaveInstances = em.createQuery(query).getResultList();

        log.debug("Found {} slave instances for pool {}.", allSlaveInstances.size(), poolName);
        return allSlaveInstances;
    }
    
    public SlavePool addSlaveStat(Long id, SlaveStatPerPool slaveStatPerPool) throws NotFoundException {
        log.debug("Received {} slave stat for pool {}", id);
        long startTime = System.currentTimeMillis();

        SlavePool slavePool = read(id);

        RelationUtil.relate(slavePool, slaveStatPerPool);

        log.debug("Finished adding slave stat for pool {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return slavePool;
    }
    
    public SlavePool addSlaveStat(Long id, SlaveStatPerLabel slaveStatPerNullLabel) throws NotFoundException {
        log.debug("Received {} slave stat for pool {} with null label", id);
        long startTime = System.currentTimeMillis();

        SlavePool slavePool = read(id);

        RelationUtil.relate(slavePool, slaveStatPerNullLabel);

        log.debug("Finished adding slave stat for pool {} with null label. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return slavePool;
    }
}
