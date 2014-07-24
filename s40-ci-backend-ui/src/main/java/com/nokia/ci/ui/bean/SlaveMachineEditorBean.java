package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.SlaveMachineEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SlaveMachine;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for SlaveMachine editing.
 *
 * @author aklappal
 */
@Named
@ViewScoped
public class SlaveMachineEditorBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(SlaveMachineEditorBean.class);
    private SlaveMachine editedSlaveMachine;
    private String copyFromSlaveMachine;
    private String editFromSlaveMachine;
    private String selectedActionTxt = "";
    private Long maxSlaveInstanceAmount;
    @Inject
    private SlaveMachineEJB slaveMachineEJB;
    private long slaveInstanceCount;

    @Override
    protected void init() throws NotFoundException {
        log.debug("SlaveMachineEditorBean.init()");

        copyFromSlaveMachine = getQueryParam("copyFromSlaveMachineId");
        editFromSlaveMachine = getQueryParam("editFromSlaveMachineId");

        initSlaveMachineAction();
        initSlaveInstanceCount();
    }

    private void initSlaveMachineAction() throws NotFoundException {

        Long existingSlaveMachineId = null;

        // for existing slave
        if (copyFromSlaveMachine != null || editFromSlaveMachine != null) {
            if (copyFromSlaveMachine != null) { // Copy from existing
                setSelectedActionTxt("Copy slave machine");
                existingSlaveMachineId = Long.parseLong(copyFromSlaveMachine);
            } else if (editFromSlaveMachine != null) // Edit existing
            {
                setSelectedActionTxt("Edit slave machine");
                existingSlaveMachineId = Long.parseLong(editFromSlaveMachine);
            }
            // Init editedSlaveMachine object with existing slave's stuff
            editedSlaveMachine = slaveMachineEJB.read(existingSlaveMachineId);

            // COPY was selected so copied new slave's id set to null
            if (editFromSlaveMachine == null) // Edit existing
            {
                //this.editedSlaveMachine.setSlaveInstances(null);
                editedSlaveMachine.setId(null);
            }

        } // We have new slaveMachine
        else {
            setSelectedActionTxt("Add new slave machine");
            editedSlaveMachine = new SlaveMachine();
        }
    }

    public String getCopyFromSlaveMachine() {
        return copyFromSlaveMachine;
    }

    public String getEditFromSlaveMachine() {
        return editFromSlaveMachine;
    }

    public boolean isDisabled() {
        if (editedSlaveMachine == null) {
            return false;
        }
        return Boolean.TRUE.equals(editedSlaveMachine.getDisabled());
    }

    public void setDisabled(boolean disabled) {
        if (editedSlaveMachine != null) {
            editedSlaveMachine.setDisabled(Boolean.valueOf(disabled));

            addMessage(FacesMessage.SEVERITY_INFO,
                    "SlaveMachine is disabled.", "SlaveMachine " + editedSlaveMachine.getUrl() + " is currently disabled.");
        }
    }

    /**
     * Slave machine is disabled or enabled, returns status accordingly
     *
     * @param slaveMachine
     * @return
     */
    public String statusText(SlaveMachine slaveMachine) {
        if (slaveMachine == null || slaveMachine.getDisabled() == null || !slaveMachine.getDisabled()) {
            return "";
        }

        return "(Disabled)";
    }

    public SlaveMachine getEditedSlaveMachine() {
        return editedSlaveMachine;
    }

    public void setEditedSlaveMachine(SlaveMachine editedSlaveMachine) {
        this.editedSlaveMachine = editedSlaveMachine;
    }

    public String saveEditedSlaveMachine() {
        log.debug("saveEditedSlaveMachine triggered!");
        try {
            // existing slaveMachine (EDIT)
            if (editedSlaveMachine.getId() != null) {
                log.debug("Updating existing slaveMachine {}", editedSlaveMachine);

                if (slaveMachineEJB.getSlaveInstanceCount(editedSlaveMachine.getId()) <= editedSlaveMachine.getMaxSlaveInstanceAmount()) {
                    slaveMachineEJB.update(editedSlaveMachine);
                    addMessage(FacesMessage.SEVERITY_INFO,
                            "Operation successful.", "Slave machine " + editedSlaveMachine.getUrl() + " was updated.");
                } else {
                    log.debug("Cannot have more than max instances, detach some instances first!");
                    addMessage(FacesMessage.SEVERITY_ERROR, "Cannot have more than max instances, detach some instances first!", "");
                }


            } // new slave (COPY, ADD)
            else {
                List<SlaveMachine> slaveMachines = slaveMachineEJB.readAll();
                boolean alreadyExists = false;

                for (SlaveMachine s : slaveMachines) {
                    if (editedSlaveMachine.getUrl().equals(s.getUrl())) {
                        alreadyExists = true;
                    }
                }

                if (!alreadyExists) {
                    log.debug("Saving new slaveMachine");
                    slaveMachineEJB.create(editedSlaveMachine);
                    addMessage(FacesMessage.SEVERITY_INFO,
                            "Operation successful.", "Created new slave machine.");
                } else {
                    log.debug("Cannot save slave machine, same machine already exist!");
                    addMessage(FacesMessage.SEVERITY_ERROR, "Slave machine already exists!", "");
                }

            }
            return "slaves?faces-redirect=true";
        } catch (Exception e) {
            log.warn("Can not save slaveMachine {}! Cause: {}", editedSlaveMachine, e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "SlaveMachine could not be saved!", "");
        }
        return null;
    }

    public String cancelEdit() {
        return "slaves?faces-redirect=true";
    }

    private void initSlaveInstanceCount() {
        if (editedSlaveMachine.getId() == null || editedSlaveMachine.getId() == 0) {
            slaveInstanceCount = 0L;
            return;
        }
        slaveInstanceCount = slaveMachineEJB.getSlaveInstanceCount(editedSlaveMachine.getId());
    }

    public Long getSlaveInstanceCount() {
        return slaveInstanceCount;
    }

    public void setSlaveInstanceCount(Long slaveInstanceCount) {
        this.slaveInstanceCount = slaveInstanceCount;
    }

    public Long getMaxSlaveInstanceAmount() {
        return maxSlaveInstanceAmount;
    }

    public void setMaxSlaveInstanceAmount(Long maxSlaveInstanceAmount) {
        this.maxSlaveInstanceAmount = maxSlaveInstanceAmount;
    }

    public String getSelectedActionTxt() {
        return selectedActionTxt;
    }

    public void setSelectedActionTxt(String selectedActionTxt) {
        this.selectedActionTxt = selectedActionTxt;
    }
}
