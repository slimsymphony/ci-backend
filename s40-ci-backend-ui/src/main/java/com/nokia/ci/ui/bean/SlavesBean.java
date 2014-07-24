package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.SlaveInstanceEJB;
import com.nokia.ci.ejb.SlaveLabelEJB;
import com.nokia.ci.ejb.SlaveMachineEJB;
import com.nokia.ci.ejb.SlavePoolEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SlaveInstance;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.SlaveMachine;
import com.nokia.ci.ejb.model.SlavePool;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SlavesBean class
 *
 * @author jaakkpaa
 */
@Named
public class SlavesBean extends DataFilterBean<SlaveMachine> {

    private static Logger log = LoggerFactory.getLogger(SlavesBean.class);
    private SlavePool addedSlavePool;
    private SlaveLabel addedSlaveLabel;
    private List<SlaveMachine> slaveMachines;
    private List<SlavePool> slavePools;
    private List<SlaveLabel> slaveLabels;
    private String selectedMaster;
    private String selectedSlave;
    private SlaveInstance selectedSlaveInstance;
    private int createInstanceCopies;
    private Map<SlaveMachine, List<SlaveInstance>> expandedSlaveInstancesMap = new HashMap<SlaveMachine, List<SlaveInstance>>();
    @Inject
    private SlaveMachineEJB slaveMachineEJB;
    @Inject
    private SlaveInstanceEJB slaveInstanceEJB;
    @Inject
    private SlavePoolEJB slavePoolEJB;
    @Inject
    private SlaveLabelEJB slaveLabelEJB;

    @Override
    protected void init() throws NotFoundException {
        log.debug("SlavesBean.init");

        querySlaveMachines();
    }

    public List<SlaveMachine> getSlaveMachines() {
        return slaveMachines;
    }

    public void querySlaveMachines() {
        slaveMachines = slaveMachineEJB.readAll();
    }

    public int getCreateInstanceCopies() {
        return createInstanceCopies;
    }

    public void setCreateInstanceCopies(int createInstanceCopies) {
        this.createInstanceCopies = createInstanceCopies;
    }

    public SlaveInstance getSelectedSlaveInstance() {
        return selectedSlaveInstance;
    }

    public void setSelectedSlaveInstance(SlaveInstance selectedSlaveInstance) {
        this.selectedSlaveInstance = selectedSlaveInstance;
        this.createInstanceCopies = 0;
    }

    public void copySlaveInstances() {
        if (selectedSlaveInstance == null || createInstanceCopies <= 0) {
            return;
        }
        log.debug("Copy slave instance id: {}", selectedSlaveInstance.getId());

        try {
            SlaveMachine slaveMachine = slaveMachineEJB.read(selectedSlaveInstance.getSlaveMachine().getId());
            Long currentInstanceCount = slaveMachineEJB.getSlaveInstanceCount(slaveMachine.getId());
            Long maxInstanceAmount = slaveMachine.getMaxSlaveInstanceAmount();
            boolean maximumReached = false;

            for (int i = 0; i < createInstanceCopies; i++) {
                if (currentInstanceCount < maxInstanceAmount) {
                    SlaveInstance copiedSlaveInstance = new SlaveInstance();
                    copiedSlaveInstance.setCurrentMaster(selectedSlaveInstance.getCurrentMaster());
                    copiedSlaveInstance.setSlaveMachine(selectedSlaveInstance.getSlaveMachine());
                    copiedSlaveInstance.setUrl(selectedSlaveInstance.getUrl());
                    copiedSlaveInstance.setSlaveLabels(selectedSlaveInstance.getSlaveLabels());
                    copiedSlaveInstance.setSlavePools(selectedSlaveInstance.getSlavePools());
                    copiedSlaveInstance.setId(null);
                    copiedSlaveInstance.setReserved(null);

                    log.debug("Saving new slaveInstance");
                    slaveMachineEJB.addSlaveInstance(slaveMachine.getId(), copiedSlaveInstance);

                    addMessage(FacesMessage.SEVERITY_INFO,
                            "Operation successful.", "Slave instance " + copiedSlaveInstance.getId() + " was added to slave machine " + slaveMachine.getUrl());

                    currentInstanceCount = slaveMachineEJB.getSlaveInstanceCount(slaveMachine.getId());
                } else {
                    maximumReached = true;
                }
            }

            if (maximumReached) {
                log.debug("All slaveInstance slots full!");
                addMessage(FacesMessage.SEVERITY_ERROR, "Slave machine " + slaveMachine.getUrl() + " cannot host more instances! (maximum is " + maxInstanceAmount + ")", "");
            }

        } catch (NotFoundException nfe) {
            log.error("Can not copy slaveInstance! Cause: {}", nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "SlaveInstance could not be copied!", "");
        }
    }

    public Long slaveInstanceCount(SlaveMachine slaveMachine) throws NotFoundException {
        Long count = 0L;
        if (slaveMachine.getId() != null) {
            count = slaveMachineEJB.getSlaveInstanceCount(slaveMachine.getId());
        }
        log.debug("Could not find slaveInstance count for slaveMachine {}", slaveMachine);
        return count;
    }

    public List<SlaveInstance> querySlaveInstances(SlaveMachine slaveMachine) {
        return expandedSlaveInstancesMap.get(slaveMachine);
    }

    public void initExpandedSlaveInstances(SlaveMachine slaveMachine) throws NotFoundException {
        List<SlaveInstance> slaveInstances = slaveMachineEJB.getSlaveInstances(slaveMachine.getId());

        for (SlaveInstance si : slaveInstances) {
            querySlavePools(si);
            querySlaveLabels(si);
            si.setSlavePools(slavePools);
            si.setSlaveLabels(slaveLabels);
        }
        expandedSlaveInstancesMap.put(slaveMachine, slaveInstances);
    }

    public List<String> getAllCurrentMasters() {
        return slaveInstanceEJB.getAllCurrentMasters();
    }

    public List<String> querySlaveMachinesList() {
        return slaveMachineEJB.querySlaveMachinesList();
    }

    /**
     * Disable selected SlaveMachine so it is skipped by LoadBalancer.
     *
     * @return
     */
    public String disableSlaveMachine() {
        log.info("Disabling a slave: {}", selectedSlave);

        String action = null;

        try {
            if (StringUtils.isEmpty(selectedSlave)) {
                log.warn("Disabling slave failed! Cause: selected slave was empty.");
                addMessage(FacesMessage.SEVERITY_WARN,
                        "No slave was selected!", "Cannot disable Slave!");
            } else {
                slaveMachineEJB.disableSlaveMachine(selectedSlave);
                slaveMachineEJB.update(slaveMachineEJB.getSlaveMachineByURL(selectedSlave));

                log.info("Disabled slave: {}", selectedSlave);
                addMessage(FacesMessage.SEVERITY_INFO,
                        "Operation successful.", "Slave was disabled: " + selectedSlave + ".");

                action = "slaves?faces-redirect=true";
            }
        } catch (NotFoundException ex) {
            log.warn("Disabling Slave {} failed! Cause: {}", selectedSlave, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Disabling Slave failed!", "Cannot disable Slave!");
        }
        return action;
    }

    public String detachAllSlaveInstancesFromMaster() {
        log.info("Detaching all slave instances from master: {}", selectedMaster);

        if (StringUtils.isEmpty(selectedMaster)) {
            log.warn("Detaching Slave instances from master failed! Cause: selected master was empty.");
            addMessage(FacesMessage.SEVERITY_WARN,
                    "No master was selected!", "Cannot detach Slave instances!");
        } else {
            slaveInstanceEJB.detachAllSlaveInstancesFromMaster(selectedMaster);
            log.info("Detached all slave instances from master: {}", selectedMaster);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Slave instances were detached from master: " + selectedMaster + ".");
        }
        return "slaves?faces-redirect=true";
    }

    public String getSelectedMaster() {
        return selectedMaster;
    }

    public void setSelectedMaster(String selectedMaster) {
        this.selectedMaster = selectedMaster;
    }

    public String getSelectedSlave() {
        return selectedSlave;
    }

    public void setSelectedSlave(String selectedSlave) {
        this.selectedSlave = selectedSlave;
    }

    public List<SlavePool> getSlavePools() {
        return slavePools;
    }

    public void querySlavePools(SlaveInstance slaveInstance) throws NotFoundException {
        slavePools = slaveInstanceEJB.getSlavePools(slaveInstance.getId());
    }

    public List<SlaveLabel> getSlaveLabels() {
        return slaveLabels;
    }

    public void querySlaveLabels(SlaveInstance slaveInstance) throws NotFoundException {
        slaveLabels = slaveInstanceEJB.getSlaveLabels(slaveInstance.getId());
    }

    public SlavePool getAddedSlavePool() {
        return addedSlavePool;
    }

    public void setAddedSlavePool(SlavePool slavePool) {
        this.addedSlavePool = slavePool;
    }

    public List<SlavePool> getAllSlavePools() {
        return slavePoolEJB.readAll();
    }

    public SlaveLabel getAddedSlaveLabel() {
        return (this.addedSlaveLabel);
    }

    public void setAddedSlaveLabel(SlaveLabel slaveLabel) {
        this.addedSlaveLabel = slaveLabel;
    }

    public List<SlaveLabel> getAllSlaveLabels() {
        return slaveLabelEJB.getSlaveLabelList();
    }

    public void delete(SlaveMachine slaveMachine) {
        log.info("Deleting slaveMachine {}, id: {}", slaveMachine.getUrl(), slaveMachine.getId());
        try {
            slaveMachineEJB.delete(slaveMachine);
            slaveMachines.remove(slaveMachine);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Slave machine " + slaveMachine.getUrl() + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting Slave machine {} failed! Cause: {}", slaveMachine.getUrl(), ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete Slave machine failed!", "Selected Slave machine could not be deleted!");
        }
    }

    public void deleteSlaveInstance(Long id) throws NotFoundException {
        SlaveInstance slaveInstance = slaveInstanceEJB.read(id);
        SlaveMachine slaveMachine = slaveMachineEJB.read(slaveInstance.getSlaveMachine().getId());
        log.debug("SlavesBean.deleteSlaveInstance({})", slaveInstance);
        try {
            slaveInstanceEJB.delete(slaveInstance);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Slave instance " + slaveInstance.getId() + " was deleted from Slave machine " + slaveMachine.getUrl());
        } catch (NotFoundException nfe) {
            log.warn("Deleting Slave instance {} failed! Cause: {}", slaveInstance.getId(), nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Detaching Slave instance failed!", "Selected Slave instance could not be deleted!");
        }
    }

    public void detachCurrentMaster(SlaveInstance slaveInstance) {
        log.debug("SlavesBean.detachCurrentMaster({})", slaveInstance);
        String master = slaveInstance.getCurrentMaster();
        try {
            slaveInstanceEJB.detach(slaveInstance.getId());
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Slave instance " + slaveInstance.getId() + " was released from master " + master);
        } catch (NotFoundException nfe) {
            log.warn("Releasing Slave instance {} failed! Cause: {}", slaveInstance, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Releasing Slave instance failed!", "Selected Slave instance could not be released!");
        }
    }
}
