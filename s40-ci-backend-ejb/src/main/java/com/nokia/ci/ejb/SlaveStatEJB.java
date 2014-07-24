package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.SlaveInstance;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.SlaveMachine;
import com.nokia.ci.ejb.model.SlavePool;
import com.nokia.ci.ejb.model.SlaveStatPerLabel;
import com.nokia.ci.ejb.model.SlaveStatPerMachine;
import com.nokia.ci.ejb.model.SlaveStatPerPool;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author larryang
 */
@Stateless
@LocalBean
public class SlaveStatEJB implements Serializable {

    private static Logger log = LoggerFactory.getLogger(SlaveStatEJB.class);
    @EJB
    private SlaveMachineEJB slaveMachineEJB;
    @EJB
    private SlavePoolEJB slavePoolEJB;
    @EJB
    private SlaveLabelEJB slaveLabelEJB;
    protected CriteriaBuilder cb;

    public String generateSlaveStat(String slavePoolName, Date provisionTime) {

        long startTime = System.currentTimeMillis();
        List<SlaveInstance> slaveInstances = null;
        String desc = "";
        try {
            slaveInstances = slavePoolEJB.getSlaveInstances(slavePoolName);
            SlavePool slavePool = slavePoolEJB.getSlavePoolByName(slavePoolName);

            HashMap<SlaveLabel, SlaveStatPerLabel> labelBasedSlaveStatMap = new HashMap<SlaveLabel, SlaveStatPerLabel>();
            HashMap<SlaveMachine, SlaveStatPerMachine> machineBasedSlaveStatMap = new HashMap<SlaveMachine, SlaveStatPerMachine>();
            
            SlaveStatPerLabel slaveStatPerNullLabel = new SlaveStatPerLabel();
            slaveStatPerNullLabel.setProvisionTime(provisionTime);

            SlaveStatPerPool slaveStatPerPool = new SlaveStatPerPool();
            slaveStatPerPool.setProvisionTime(provisionTime);
            slaveStatPerPool.setSlavePool(slavePool);

            for (SlaveInstance slaveInstance : slaveInstances) {
                
                //Handle null label instances.
                if (slaveInstance.getSlaveLabels().isEmpty()){

                    //Stat for the whole null label
                    if (StringUtils.isNotEmpty(slaveInstance.getCurrentMaster())) {
                        slaveStatPerNullLabel.setReservedInstanceCount(slaveStatPerNullLabel.getReservedInstanceCount() + 1);
                    }

                    slaveStatPerNullLabel.setTotalInstanceCount(slaveStatPerNullLabel.getTotalInstanceCount() + 1);
                }

                //Stat for each labels
                for (SlaveLabel slaveLabel : slaveInstance.getSlaveLabels()) {

                    SlaveStatPerLabel slaveStatPerLabel = labelBasedSlaveStatMap.get(slaveLabel);
                    
                    if (slaveStatPerLabel == null){
                        slaveStatPerLabel = new SlaveStatPerLabel();
                        slaveStatPerLabel.setProvisionTime(provisionTime);
                        slaveStatPerLabel.setSlaveLabel(slaveLabel);
                        slaveStatPerLabel.setSlavePool(slavePool);
                        labelBasedSlaveStatMap.put(slaveLabel, slaveStatPerLabel);                        
                    }

                    if (StringUtils.isNotEmpty(slaveInstance.getCurrentMaster())) {
                        slaveStatPerLabel.setReservedInstanceCount(slaveStatPerLabel.getReservedInstanceCount() + 1);
                    }

                    slaveStatPerLabel.setTotalInstanceCount(slaveStatPerLabel.getTotalInstanceCount() + 1);
                }

                //Stat for the whole pool
                if (StringUtils.isNotEmpty(slaveInstance.getCurrentMaster())) {
                    slaveStatPerPool.setReservedInstanceCount(slaveStatPerPool.getReservedInstanceCount() + 1);
                }

                slaveStatPerPool.setTotalInstanceCount(slaveStatPerPool.getTotalInstanceCount() + 1);

                //Stat for each macihnes
                SlaveStatPerMachine slaveStatPerMachine = machineBasedSlaveStatMap.get(slaveInstance.getSlaveMachine());

                if (slaveStatPerMachine == null) {
                    slaveStatPerMachine = new SlaveStatPerMachine();
                    slaveStatPerMachine.setProvisionTime(provisionTime);
                    slaveStatPerMachine.setSlaveMachine(slaveMachineEJB.read(slaveInstance.getSlaveMachine().getId()));
                    slaveStatPerMachine.setSlavePool(slavePool);
                    machineBasedSlaveStatMap.put(slaveInstance.getSlaveMachine(), slaveStatPerMachine);
                }

                if (StringUtils.isNotEmpty(slaveInstance.getCurrentMaster())) {
                    slaveStatPerMachine.setReservedInstanceCount(slaveStatPerMachine.getReservedInstanceCount() + 1);
                }

                slaveStatPerMachine.setTotalInstanceCount(slaveStatPerMachine.getTotalInstanceCount() + 1);
            }
            
            for (SlaveLabel slaveLabel : labelBasedSlaveStatMap.keySet()){
                SlaveStatPerLabel curSlaveStatPerLabel = labelBasedSlaveStatMap.get(slaveLabel);
                slaveLabelEJB.addSlaveStat(slaveLabel.getId(), curSlaveStatPerLabel);
            }
            
            for (SlaveMachine slaveMachine : machineBasedSlaveStatMap.keySet()){
                SlaveStatPerMachine curSlaveStatPerMachine = machineBasedSlaveStatMap.get(slaveMachine);
                slaveMachineEJB.addSlaveStat(slaveMachine.getId(), curSlaveStatPerMachine);
            }
            
            slavePoolEJB.addSlaveStat(slavePool.getId(), slaveStatPerPool);
            if (slaveStatPerPool.getReservedInstanceCount() == slaveStatPerPool.getTotalInstanceCount()){
                desc += "Pool: " + slavePoolName + " is full." + System.getProperty("line.separator");
            }
            
            slavePoolEJB.addSlaveStat(slavePool.getId(), slaveStatPerNullLabel);
            if (slaveStatPerNullLabel.getReservedInstanceCount() == slaveStatPerNullLabel.getTotalInstanceCount()){
                desc += "Pool: " + slavePoolName + " (without labels) is full." + System.getProperty("line.separator");
            }
                    
        }catch (Exception e) {
            log.error("Failed to log load balancer data for pool {}. Reason: {}.", slavePoolName, e);
        }finally{
            log.info("Finished logging load balancer data for pool {}. task done in {}ms", slavePoolName, System.currentTimeMillis() - startTime);
        }
        
        return desc;
    }
}
