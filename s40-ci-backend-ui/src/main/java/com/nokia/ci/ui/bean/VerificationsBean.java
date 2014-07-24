package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.VerificationEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.Verification;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for verifications.
 *
 * @author vrouvine
 */
@Named
public class VerificationsBean extends DataFilterBean<Verification> {

    private static Logger log = LoggerFactory.getLogger(VerificationsBean.class);
    private List<Verification> verifications;
    @Inject
    private VerificationEJB verificationEJB;

    @Override
    protected void init() {
        initVerifications();
    }

    public List<Verification> getVerifications() {
        return verifications;
    }

    public void delete(Verification verification) {
        log.info("Deleting verification {}", verification);
        boolean deleteAllowed = true;
        List<Verification> childVerifications = verificationEJB.getChildVerifications(verification.getId());
        if (childVerifications != null && !childVerifications.isEmpty()) {
            log.warn("Deleting verification {} failed! Cause: Verification has child verifications that depend on it.", verification);
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete verification failed!", "Selected verification could not be deleted! Verification has child verifications that depend on it.");
            deleteAllowed = false;
        }
        List<Project> projects = verificationEJB.getProjects(verification.getId());
        if (projects != null && !projects.isEmpty()) {
            log.warn("Deleting verification {} failed! Cause: Verification is still included in some project.", verification);
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete verification failed!", "Selected verification could not be deleted! Please check that the verification is not included in any project before deleting.");
            deleteAllowed = false;
        }
        if (!deleteAllowed) {
            return;
        }
        try {
            String name = verification.getName();
            verificationEJB.delete(verification);
            verifications.remove(verification);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Verification " + name + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting verification {} failed! Cause: {}", verification, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete verification failed!", "Selected verification could not be deleted!");
        }
    }

    private void initVerifications() {
        verifications = verificationEJB.readAll();
    }
}
