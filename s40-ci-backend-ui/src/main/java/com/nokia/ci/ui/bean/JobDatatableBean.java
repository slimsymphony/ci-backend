/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.Job;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@RequestScoped
public class JobDatatableBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(JobDatatableBean.class);
    @Inject
    JobEJB jobEJB;
    @Inject
    private HttpSessionBean httpSessionBean;

    public void delete(Job j) {
        log.debug("Deleting job {}", j);
        try {
            String name = j.getDisplayName();
            jobEJB.delete(j, httpSessionBean.getSysUserId());
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Verification " + name
                    + " was deleted.");
        } catch (UnauthorizedException ex) {
            log.warn("Deleting job {} failed! Cause: {}", j, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Operation failed.",
                    "Current user is not authorized to perform operation.");
        } catch (NotFoundException ex) {
            log.warn("Deleting job {} failed! Cause: {}", j, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Operation failed.",
                    "Selected verification could not be deleted.");
        }
    }

    @Override
    protected void init() throws Exception {
    }
}
