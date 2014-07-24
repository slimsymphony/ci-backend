/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.ChangeEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.jms.JobStartProducer;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeFile;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ui.exception.QueryParamException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@ViewScoped
public class ChangeBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(ChangeBean.class);
    private Change change;
    @Inject
    private ChangeEJB changeEJB;
    @Inject
    private SysUserEJB sysUserEJB;
    @Inject
    private JobStartProducer producer;
    @Inject
    private HttpSessionBean httpSessionBean;
    private List<ChangeFile> changeFiles;
    private List<Change> parentChanges;
    private List<Change> childChanges;
    private List<Job> toolboxJobs;
    private Long selectedToolboxJob;
    private BuildGroup selectedBuildGroup;
    private String refererUrl;

    @Override
    protected void init() throws BackendAppException, InvalidArgumentException, QueryParamException {
        String commitId = getMandatoryQueryParam("commitId");
        refererUrl = getBackUrl();
        change = changeEJB.getChangeByCommitIdSecure(commitId);
        changeFiles = changeEJB.getChangeFiles(change.getId());
        parentChanges = changeEJB.getParentChanges(change.getId());
        childChanges = changeEJB.getChildChanges(change.getId());
    }

    public Change getChange() {
        return change;
    }

    public String getRefererUrl() {
        return refererUrl;
    }

    public List<BuildGroup> getBuildGroups() {
        if (change == null) {
            return new ArrayList<BuildGroup>();
        }

        return changeEJB.getBuildGroups(change.getId());
    }

    public List<ChangeFile> getChangeFiles() {
        return changeFiles;
    }

    public List<Change> getParentChanges() {
        return parentChanges;
    }

    public List<Change> getChildChanges() {
        return childChanges;
    }

    public Long getSelectedToolboxJob() {
        return selectedToolboxJob;
    }

    public void setSelectedToolboxJob(Long selectedToolboxJob) {
        this.selectedToolboxJob = selectedToolboxJob;
    }

    public List<Job> getToolboxJobs() {
        if (toolboxJobs == null) {
            initUserToolboxJobs();
        }
        return toolboxJobs;
    }

    public void updateSelectedBuildGroup(BuildGroup buildGroup) {
        selectedBuildGroup = buildGroup;
    }

    public String triggerBuild() {
        if (!httpSessionBean.isLoggedIn()) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Build triggering failed.", "You have to login first!");
            return null;
        }
        if (selectedToolboxJob == null) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Build triggering failed.", "No toolbox job selected!");
            return null;
        }
        if (selectedBuildGroup == null) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Build triggering failed.", "There was a problem in the system. Please try again or contact support.");
            return null;
        }
        try {
            List<Change> changesToTrigger = new ArrayList<Change>();
            change.setChangeFiles(changeEJB.getChangeFiles(change.getId()));
            changesToTrigger.add(change);
            // Start new build on selected toolbox job using refspec from selected build group,
            // commit id of the change and the change itself
            producer.sendJobStart(selectedToolboxJob, selectedBuildGroup.getGerritRefSpec(), change.getCommitId(), changesToTrigger);
        } catch (JobStartException e) {
            log.warn("Error while trying to start job {}: {}", selectedToolboxJob, e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Build triggering failed.", "There was a problem in the system. Please try again or contact support.");
            return null;
        }
        addMessage(FacesMessage.SEVERITY_INFO, "Verification started", "Verification started successfully for toolbox job " + selectedToolboxJob);
        return "verificationDetails?faces-redirect=true&verificationId=" + selectedToolboxJob;
    }

    private void initUserToolboxJobs() {
        SysUser sysUser = httpSessionBean.getSysUser();
        toolboxJobs = sysUserEJB.getOwnedJobs(sysUser.getId());
    }
}
