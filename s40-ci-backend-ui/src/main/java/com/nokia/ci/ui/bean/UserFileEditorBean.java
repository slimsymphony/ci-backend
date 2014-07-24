package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.UserFileEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.AccessScope;
import com.nokia.ci.ejb.model.OwnershipScope;
import com.nokia.ci.ejb.model.UserFile;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for UserFile editing.
 *
 * @author larryang
 */
@Named
@ViewScoped
public class UserFileEditorBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(UserFileEditorBean.class);
    private String userFileId;
    private boolean ownThisFile = false;
    private UserFile editedUserFile = null;
    @Inject
    private UserFileEJB userFileEJB;
    @Inject
    private HttpSessionBean httpSessionBean;

    @Override
    public void init() throws NotFoundException {
        try{
            log.debug("UserFileEditorBean.init()");
            userFileId = getMandatoryQueryParam("userFileId");
            editedUserFile = userFileEJB.read(Long.parseLong(userFileId));
            ownThisFile = editedUserFile.getOwner().equals(httpSessionBean.getSysUser());
        }catch(Exception e){
            log.error("Exception when init UserFileEditorBean. {}", e.getMessage() + e.getStackTrace());
        }
    }
    
    public String saveEditedUserFile() {
        log.debug("saveEditedUserFile triggered!");
        try {
            // Edit existing userFile
            if (editedUserFile != null) {
                log.debug("Updating existing userFile {}", editedUserFile.getId());
                userFileEJB.update(editedUserFile);
                addMessage(FacesMessage.SEVERITY_INFO,
                            "Operation successful.", "User file " + editedUserFile.getName() + " was updated.");
                return "userSettings?faces-redirect=true";
            }else{
                log.error("Failed to save UserFile, current edited file is null.");
                addMessage(FacesMessage.SEVERITY_ERROR,
                            "Operation fail.", "User file is null and not updated.");                
            }
        } catch (Exception e) {
            log.warn("Can not save userFile {}! Cause: {}", editedUserFile, e.getMessage() + e.getStackTrace());
            addMessage(FacesMessage.SEVERITY_ERROR, "UserFile could not be saved!", "");
        }
        return null;
    }

    public String cancelEdit() {
        return "userSettings?faces-redirect=true";
    }

    public UserFile getEditedUserFile() {
        return editedUserFile;
    }

    public void setEditedUserFile(UserFile editedUserFile) {
        this.editedUserFile = editedUserFile;
    }

    public AccessScope[] getAccessScopeValues() {
        return AccessScope.values();
    }

    public OwnershipScope[] getOwnershipScopeValues() {
        return OwnershipScope.values();
    }

    public boolean isOwnThisFile() {
        return ownThisFile;
    }

    public void setOwnThisFile(boolean ownThisFile) {
        this.ownThisFile = ownThisFile;
    }
}