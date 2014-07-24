package com.nokia.ci.ejb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.nokia.ci.ejb.model.SlaveInstance;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.SlaveMachine;
import com.nokia.ci.ejb.model.SlavePool;
import com.nokia.ci.ejb.util.RelationUtil;

public class SlaveInstanceEJBTest extends EJBTestBase {

    private SlaveInstanceEJB ejb;
    private List<SlaveMachine> slaveMachines;
    private List<SlavePool> slavePools;
    private List<SlaveLabel> slaveLabels;
    
    @Override
    @Before
    public void before() {
        super.before();
        ejb = new SlaveInstanceEJB();
        ejb.em = em;
        ejb.cb = cb;
        
        ejb.sysConfigEJB = Mockito.mock(SysConfigEJB.class);
    }

    @Test
    public void provideSlaveInstances() {
        List<SlaveInstance> slaveInstances = createProvidableSlaveInstances();
        int requestedAmount = 10;
        List<SlaveInstance> loadBalancedSlaveInstances = ejb.getLoadBalancedSlaveInstances(slaveInstances, requestedAmount);
        Map<SlaveMachine, Integer> slaveMachines = new HashMap<SlaveMachine, Integer>();
        for (SlaveInstance slaveInstance : loadBalancedSlaveInstances) {
            Integer slaves = slaveMachines.get(slaveInstance.getSlaveMachine());
            if (slaves == null) {
                slaves = new Integer(1);
            } else {
                slaves++;
            }
            slaveMachines.put(slaveInstance.getSlaveMachine(), slaves);
        }
        
        for (Integer slaves : slaveMachines.values()) {
            Assert.assertEquals("Load balancing did not balance slave instances equally", 2, slaves.intValue());
        }
    }

    private List<SlaveInstance> createProvidableSlaveInstances() {
        slaveMachines = createEntityList(SlaveMachine.class, 5);
        populateSlaveMachines(slaveMachines);

        slavePools = createEntityList(SlavePool.class, 3);
        populateSlavePools(slavePools);

        slaveLabels = createEntityList(SlaveLabel.class, 2);
        populateSlaveLabels(slaveLabels);
        
        List<SlaveInstance> slaveInstances = createEntityList(SlaveInstance.class, 15);
        populateSlaveInstances(slaveInstances);
        
        for (int i = 0; i < 15; i++) {
            SlaveInstance slaveInstance = slaveInstances.get(i);
            RelationUtil.relate(slaveInstance, slaveLabels.get(1));
            RelationUtil.relate(slaveInstance, slavePools.get(1));
            RelationUtil.relate(slaveMachines.get((i / 3)), slaveInstance);
        }

        return slaveInstances;
    }
    
}
