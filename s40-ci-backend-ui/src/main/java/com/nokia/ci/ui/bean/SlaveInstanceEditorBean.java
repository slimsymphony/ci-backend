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
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for SlaveInstance editing.
 *
 * @author aklappal
 */
@Named
@ViewScoped
public class SlaveInstanceEditorBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(SlaveInstanceEditorBean.class);
    private String editFromSlaveInstance;
    private String addSlaveInstance;
    private SlavePool addedSlavePool;
    private SlavePool editedSlavePool;
    private SlaveLabel addedSlaveLabel;
    private SlaveLabel editedSlaveLabel;
    private List<SlavePool> slavePools;
    private List<SlaveLabel> slaveLabels;
    private String attachedSlaveMachine;
    private SlaveInstance editedSlaveInstance;
    private boolean editHostDisabled;
    private boolean editMasterDisabled;
    private boolean slaveInstanceSlotsFull;
    private String selectedInstanceActionTxt = "";
    private Long amountOfCopiesToCreate = 1L;
    private DualListModel<SlavePool> slavePoolsDualList;
    private DualListModel<SlaveLabel> slaveLabelsDualList;
    private List<SlavePool> availableSlavePools;
    private List<SlavePool> selectedSlavePools;
    private List<SlaveLabel> availableSlaveLabels;
    private List<SlaveLabel> selectedSlaveLabels;
    @Inject
    private SlaveMachineEJB slaveMachineEJB;
    @Inject
    private SlaveInstanceEJB slaveInstanceEJB;
    @Inject
    private SlavePoolEJB slavePoolEJB;
    @Inject
    private SlaveLabelEJB slaveLabelEJB;
    private SlavePool lastDeletedSlavePool;
    private SlaveLabel lastDeletedSlaveLabel;

    @Override
    protected void init() throws NotFoundException {
        log.debug("SlaveInstanceEditorBean.init()");

        editFromSlaveInstance = getQueryParam("editFromSlaveInstanceId");
        addSlaveInstance = getQueryParam("addSlaveInstanceId");

        addedSlavePool = new SlavePool();
        editedSlavePool = new SlavePool();
        slavePools = getAllSlavePools();

        addedSlaveLabel = new SlaveLabel();
        editedSlaveLabel = new SlaveLabel();
        slaveLabels = getAllSlaveLabels();

        initSlaveInstanceAction();
        initSlavePoolsPickList();
        initSlaveLabelsPickList();
    }

    private void initSlaveInstanceAction() throws NotFoundException {

        Long existingSlaveId = null;
        Long existingSlaveMachineId = null;

        if (editFromSlaveInstance != null) {
            String[] str = editFromSlaveInstance.split("_");
            attachedSlaveMachine = str[0];
            editFromSlaveInstance = str[1];

            setSelectedInstanceActionTxt("Edit slave instance");
            existingSlaveId = Long.parseLong(editFromSlaveInstance);

            // Init editedSlave object with existing slave's stuff
            editedSlaveInstance = slaveInstanceEJB.read(existingSlaveId);

            List<SlavePool> existingSlavePools = slaveInstanceEJB.getSlavePools(existingSlaveId);
            List<SlaveLabel> existingSlaveLabels = slaveInstanceEJB.getSlaveLabels(existingSlaveId);

            // transfer existing slave pools/labels info to editedSlave
            this.editedSlaveInstance.setSlavePools(existingSlavePools);
            this.editedSlaveInstance.setSlaveLabels(existingSlaveLabels);

            existingSlaveMachineId = Long.parseLong(attachedSlaveMachine);
            this.editedSlaveInstance.setSlaveMachine(slaveMachineEJB.read(existingSlaveMachineId));

            editHostDisabled = true;
            editMasterDisabled = true;

        } else {
            setSelectedInstanceActionTxt("Add new slave instance");
            this.editedSlaveInstance = new SlaveInstance();
            editHostDisabled = true;
            editMasterDisabled = true;
            this.editedSlaveInstance.setSlaveMachine(slaveMachineEJB.read(Long.parseLong(addSlaveInstance)));
        }
    }

    public String getEditFromSlaveInstance() {
        return editFromSlaveInstance;
    }

    public String getAddSlaveInstance() {
        return addSlaveInstance;
    }

    private void initSlavePoolsPickList() throws NotFoundException {
        slavePoolsDualList = new DualListModel<SlavePool>();
        availableSlavePools = getAllSlavePools();

        if (editedSlaveInstance.getId() == null) {
            selectedSlavePools = editedSlaveInstance.getSlavePools();
            availableSlavePools.removeAll(selectedSlavePools);
            slavePoolsDualList.setSource(availableSlavePools);
            slavePoolsDualList.setTarget(selectedSlavePools);
            return;
        }

        selectedSlavePools = slaveInstanceEJB.getSlavePools(editedSlaveInstance.getId());
        availableSlavePools.removeAll(selectedSlavePools);
        slavePoolsDualList.setSource(availableSlavePools);
        slavePoolsDualList.setTarget(selectedSlavePools);
    }

    private void initSlaveLabelsPickList() throws NotFoundException {
        slaveLabelsDualList = new DualListModel<SlaveLabel>();
        availableSlaveLabels = getAllSlaveLabels();

        if (editedSlaveInstance.getId() == null) {
            selectedSlaveLabels = editedSlaveInstance.getSlaveLabels();
            availableSlaveLabels.removeAll(selectedSlaveLabels);
            slaveLabelsDualList.setSource(availableSlaveLabels);
            slaveLabelsDualList.setTarget(selectedSlaveLabels);
            return;
        }

        selectedSlaveLabels = slaveInstanceEJB.getSlaveLabels(editedSlaveInstance.getId());
        availableSlaveLabels.removeAll(selectedSlaveLabels);
        slaveLabelsDualList.setSource(availableSlaveLabels);
        slaveLabelsDualList.setTarget(selectedSlaveLabels);
    }

    public List<SlavePool> getAllSlavePools() {
        return slavePoolEJB.readAll();
    }

    public SlavePool getAddedSlavePool() {
        return addedSlavePool;
    }

    public void setAddedSlavePool(SlavePool slavePool) {
        this.addedSlavePool = slavePool;
    }

    public Long getAmountOfCopiesToCreate() {
        return amountOfCopiesToCreate;
    }

    public void setAmountOfCopiesToCreate(Long amountOfCopiesToCreate) {
        this.amountOfCopiesToCreate = amountOfCopiesToCreate;
    }

    public void saveAddedSlavePool() throws NotFoundException {
        log.debug("SlaveInstanceEditorBean.saveAddedSlavePool()");

        if (this.addedSlavePool.getName().isEmpty()) {
            return;
        }

        List<SlavePool> existingSlavePools = slavePoolEJB.readAll();
        for (SlavePool sp : existingSlavePools) {
            if (addedSlavePool.getName().equals(sp.getName())) {
                log.debug("Cannot save slave pool, same pool already exist! {}", addedSlavePool.getName());
                addMessage(FacesMessage.SEVERITY_ERROR, "Slave pool " + addedSlavePool.getName() + " already exists!", "");
                return;
            }
        }

        slavePoolEJB.create(this.addedSlavePool);
        editedSlaveInstance.getSlavePools().add(this.addedSlavePool);
        this.addedSlavePool = new SlavePool();
        initSlavePoolsPickList();
        log.debug("Added new slavePool: {}", addedSlavePool.getName());
    }

    public SlavePool getEditedSlavePool() {
        return editedSlavePool;
    }

    public void setEditedSlavePool(SlavePool editedSlavePool) {
        this.editedSlavePool = editedSlavePool;
    }

    public void editSelectedSlavePool() {
        log.debug("SlaveInstanceEditorBean.editSelectedSlavePool()");
        Long editFromSlavePool = Long.parseLong(getQueryParam("editFromSlavePoolId"));
        try {
            editedSlavePool = slavePoolEJB.read(editFromSlavePool);
            log.debug("SlaveInstanceEditorBean.editSelectedSlavePool(): salvePool was edited: {}", editedSlavePool.getName());
        } catch (NotFoundException ex) {
            log.warn("Couldn't read Slavepool, Cause: {}", ex.getMessage());
        }
    }

    public void saveEditedSlavePool() {
        log.debug("SlaveInstanceEditorBean.saveEditedSlavePool()");

        List<SlavePool> existingSlavePools = slavePoolEJB.readAll();

        for (SlavePool sp : existingSlavePools) {
            if (!sp.equals(editedSlavePool) && editedSlavePool.getName().equals(sp.getName())) {
                log.debug("Cannot save slave pool, same pool already exist!");
                addMessage(FacesMessage.SEVERITY_ERROR, "Slave pool " + editedSlavePool.getName() + " already exists!", "");
                return;
            }
        }

        try {
            slavePoolEJB.update(editedSlavePool);
            initSlavePoolsPickList();

            log.debug("Saved edited slavePool: {}", editedSlavePool.getName());
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Slavepool was edited to " + editedSlavePool.getName());
        } catch (NotFoundException e) {
            log.warn("Can not edit slavePool! Cause: {}", e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "SlavePool " + editedSlavePool.getName() + " could not be edited!", "");
        }
    }

    public List<SlaveLabel> getAllSlaveLabels() {
        return slaveLabelEJB.getSlaveLabelList();
    }

    public SlaveLabel getAddedSlaveLabel() {
        return this.addedSlaveLabel;
    }

    public void setAddedSlaveLabel(SlaveLabel slaveLabel) {
        this.addedSlaveLabel = slaveLabel;
    }

    public void saveAddedSlaveLabel() throws NotFoundException {
        log.debug("SlaveInstanceEditorBean.saveAddedSlaveLabel()");
        if (this.addedSlaveLabel.getName().isEmpty()) {
            return;
        }

        List<SlaveLabel> existingSlaveLabels = slaveLabelEJB.readAll();

        for (SlaveLabel sl : existingSlaveLabels) {
            if (addedSlaveLabel.getName().equals(sl.getName())) {
                log.debug("Cannot save slave label, same label already exist! {}", addedSlaveLabel.getName());
                addMessage(FacesMessage.SEVERITY_ERROR, "Slave label " + addedSlaveLabel.getName() + " already exists!", "");
                return;
            }
        }

        slaveLabelEJB.create(this.addedSlaveLabel);
        editedSlaveInstance.getSlaveLabels().add(this.addedSlaveLabel);
        this.addedSlaveLabel = new SlaveLabel();
        initSlaveLabelsPickList();
        log.debug("Added new slaveLabel: {}", addedSlaveLabel.getName());
    }

    public SlaveLabel getEditedSlaveLabel() {
        return editedSlaveLabel;
    }

    public void setEditedSlaveLabel(SlaveLabel editedSlaveLabel) {
        this.editedSlaveLabel = editedSlaveLabel;
    }

    public void editSelectedSlaveLabel() {
        log.debug("SlaveInstanceEditorBean.editSelectedSlaveLabel()");
        Long editFromSlaveLabel = Long.parseLong(getQueryParam("editFromSlaveLabelId"));
        try {
            editedSlaveLabel = slaveLabelEJB.read(editFromSlaveLabel);
            log.debug("SlaveInstanceEditorBean.editSelectedSlaveLabel(): slaveLabel was edited: {}", editedSlaveLabel.getName());
        } catch (NotFoundException ex) {
            log.warn("Couldn't read Slavelabel, Cause: {}", ex.getMessage());
        }
    }

    public void saveEditedSlaveLabel() {
        log.debug("SlaveInstanceEditorBean.saveEditedSlaveLabel()");

        List<SlaveLabel> existingSlaveLabels = slaveLabelEJB.readAll();

        for (SlaveLabel sl : existingSlaveLabels) {
            if (!sl.equals(editedSlaveLabel) && editedSlaveLabel.getName().equals(sl.getName())) {
                log.debug("Cannot save slave label, same label already exist!");
                addMessage(FacesMessage.SEVERITY_ERROR, "Slave label " + editedSlaveLabel.getName() + " already exists!", "");
                return;
            }
        }

        try {
            slaveLabelEJB.update(editedSlaveLabel);
            initSlaveLabelsPickList();

            log.debug("Saved edited slaveLabel: {}", editedSlaveLabel.getName());
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Slavelabel was edited to " + editedSlaveLabel.getName());
        } catch (NotFoundException e) {
            log.warn("Can not edit slaveLabel! Cause: {}", e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "SlaveLabel " + editedSlaveLabel.getName() + " could not be edited!", "");
        }
    }

    public SlaveInstance getEditedSlaveInstance() {
        return editedSlaveInstance;
    }

    public void setEditedSlaveInstance(SlaveInstance slaveInstance) {
        editedSlaveInstance = slaveInstance;
    }

    public String saveEditedSlaveInstance() {
        log.debug("saveEditedSlaveInstance triggered!");

        editedSlaveInstance.setSlavePools(slavePoolsDualList.getTarget());
        editedSlaveInstance.setSlaveLabels(slaveLabelsDualList.getTarget());

        try {
            if (editedSlaveInstance.getId() != null) {
                log.debug("Updating existing slaveInstance {}", editedSlaveInstance);

                slaveInstanceEJB.update(editedSlaveInstance);

                addMessage(FacesMessage.SEVERITY_INFO,
                        "Operation successful.", "Slave instance " + editedSlaveInstance.getId()
                        + " attached to machine " + editedSlaveInstance.getSlaveMachine().getUrl() + " was updated.");
            } else {
                SlaveMachine slaveMachine = editedSlaveInstance.getSlaveMachine();
                Long currentInstanceCount = slaveMachineEJB.getSlaveInstanceCount(slaveMachine.getId());
                Long maxInstanceAmount = slaveMachine.getMaxSlaveInstanceAmount();

                for (Long num = 0L; num < amountOfCopiesToCreate; num++) {
                    if (currentInstanceCount < maxInstanceAmount) {
                        log.debug("Saving new slaveInstance");
                        SlaveInstance si = new SlaveInstance();
                        si.setCurrentMaster(editedSlaveInstance.getCurrentMaster());
                        si.setSlaveMachine(editedSlaveInstance.getSlaveMachine());
                        si.setUrl(editedSlaveInstance.getUrl());
                        si.setSlaveLabels(editedSlaveInstance.getSlaveLabels());
                        si.setSlavePools(editedSlaveInstance.getSlavePools());
                        si.setId(null);
                        si.setReserved(null);

                        slaveInstanceEJB.create(si);

                        List<SlaveInstance> instancesList = slaveMachineEJB.getSlaveInstances(slaveMachine.getId());
                        instancesList.add(si);
                        slaveMachine.setSlaveInstances(instancesList);

                        addMessage(FacesMessage.SEVERITY_INFO,
                                "Operation successful.", "Created new slave instance: " + si.getId());

                        currentInstanceCount = slaveMachineEJB.getSlaveInstanceCount(slaveMachine.getId());
                        maxInstanceAmount = slaveMachine.getMaxSlaveInstanceAmount();
                    } else {
                        slaveInstanceSlotsFull = true;
                    }
                }

                if (slaveInstanceSlotsFull) {
                    log.debug("All slaveInstance slots full!");
                    addMessage(FacesMessage.SEVERITY_ERROR, "Slave machine " + slaveMachine.getUrl() + " cannot host more instances!", "");
                }

            }

            return "slaves?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save slaveInstance {}! Cause: {}", editedSlaveInstance, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "SlaveInstance could not be saved!", "");
        }
        return null;
    }

    public String cancelEdit() {
        return "slaves?faces-redirect=true";
    }

    public List<SlavePool> getSlavePools() {
        return slavePools;
    }

    public void setSlavePools(List<SlavePool> slavePools) {
        this.slavePools = slavePools;
    }

    public void deleteSlavePool() {
        log.debug("SlaveInstanceEditorBean: deleteSlavePool triggered!");
        Long slavePoolId = Long.valueOf(getQueryParam("slavePoolId"));
        SlavePool slavePool;

        if (lastDeletedSlavePool != null) {
            lastDeletedSlavePool = null;
            return;
        }

        try {
            slavePool = slavePoolEJB.read(slavePoolId);
            slavePoolEJB.delete(slavePool);
            lastDeletedSlavePool = slavePool;

            slavePoolsDualList.getSource().remove(slavePool);
            slavePoolsDualList.getTarget().remove(slavePool);

            log.debug("Deleted slavePool: {}", slavePool.getName());
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "SlavePool " + slavePool.getName() + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Can not delete slavePool! Cause: {}", ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete SlavePool failed!", "Selected SlavePool could not be deleted!");
        }
    }

    public List<SlaveLabel> getSlaveLabels() {
        return slaveLabels;
    }

    public void setSlaveLabels(List<SlaveLabel> slaveLabels) {
        this.slaveLabels = slaveLabels;
    }

    public void deleteSlaveLabel() {
        log.debug("SlaveInstanceEditorBean: deleteSlaveLabel triggered!");
        Long slaveLabelId = Long.valueOf(getQueryParam("slaveLabelId"));
        SlaveLabel slaveLabel;

        if (lastDeletedSlaveLabel != null) {
            lastDeletedSlaveLabel = null;
            return;
        }

        try {
            slaveLabel = slaveLabelEJB.read(slaveLabelId);
            slaveLabelEJB.delete(slaveLabel);
            lastDeletedSlaveLabel = slaveLabel;

            slaveLabelsDualList.getSource().remove(slaveLabel);
            slaveLabelsDualList.getTarget().remove(slaveLabel);

            log.debug("Deleted slaveLabel: {}", slaveLabel.getName());
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "SlaveLabel " + slaveLabel.getName() + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Can not delete slaveLabel! Cause: {}", ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete SlaveLabel failed!", "Selected SlaveLabel could not be deleted!");
        }
    }

    public String getUrl() {
        if (editedSlaveInstance == null) {
            return "";
        }
        return editedSlaveInstance.getUrl();
    }

    public void setUrl(String url) {
        if (editedSlaveInstance != null) {
            editedSlaveInstance.setUrl(url);
        }
    }

    public DualListModel<SlavePool> getSlavePoolsDualList() {
        return slavePoolsDualList;
    }

    public void setSlavePoolsDualList(DualListModel<SlavePool> slavePoolsDualList) {
        this.slavePoolsDualList = slavePoolsDualList;
    }

    public DualListModel<SlaveLabel> getSlaveLabelsDualList() {
        return slaveLabelsDualList;
    }

    public void setSlaveLabelsDualList(DualListModel<SlaveLabel> slaveLabelsDualList) {
        this.slaveLabelsDualList = slaveLabelsDualList;
    }

    public String getSelectedInstanceActionTxt() {
        return selectedInstanceActionTxt;
    }

    public void setSelectedInstanceActionTxt(String selectedInstanceActionTxt) {
        this.selectedInstanceActionTxt = selectedInstanceActionTxt;
    }

    public boolean isEditHostDisabled() {
        return editHostDisabled;
    }

    public boolean isEditMasterDisabled() {
        return editMasterDisabled;
    }
}
