package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.gerrit.GerritJobManagerEJB;
import com.nokia.ci.ejb.gerrit.model.GerritDetail;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobVerificationConf;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ui.exception.QueryParamException;
import com.nokia.ci.ui.model.GerritDetailDataModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for starting job.
 *
 * @author vrouvine
 */
@Named
@ViewScoped
public class JobStartBean extends AbstractUIBaseBean implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(JobStartBean.class);
    private Job verification;
    @Inject
    JobEJB jobEJB;
    @Inject
    BranchEJB branchEJB;
    @Inject
    GerritEJB gerritEJB;
    @Inject
    GerritJobManagerEJB gerritJobManagerEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    private ProjectEJB projectEJB;
    private Gerrit selectedGerrit;
    private String query;
    private List<GerritDetail> details;
    private GerritDetail[] selectedDetails;
    private GerritDetailDataModel gerridDetailDataModel = null;
    private GerritDetail selectedGerritDetail = null;
    private Long projectId;
    private Project project;

    /**
     * Creates a new instance of JobStartBean
     */
    public JobStartBean() {
    }

    @Override
    protected void init() throws QueryParamException, BackendAppException {
        String jobId = getMandatoryQueryParam("verificationId");
        log.debug("Finding job {} for starting!", jobId);
        verification = jobEJB.readSecure(Long.parseLong(jobId));
        project = projectEJB.read(verification.getProjectId());
        projectId = project.getId();
        Branch branch = branchEJB.read(verification.getBranch().getId());
        query = "status:open " + httpSessionBean.getSysUserEmail();
        if (branch.getType() == BranchType.DEVELOPMENT || branch.getType() == BranchType.MASTER) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Operation not supported.", "Verification start is not enabled for Development and Master type jobs.");
        } else if (!httpSessionBean.isAdmin() && branch.getType() == BranchType.SINGLE_COMMIT) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Unauthorized operation.", "You do not have rights to start a single commit job.");
        } else {
            checkVerificationConfigs();
            if (httpSessionBean.isLoggedIn()) {
                selectedGerrit = jobEJB.getGerrit(verification.getId());
                populateGerritData();
            }
        }
    }

    public void search() {
        try {
            if (selectedGerrit != null && StringUtils.isNotEmpty(project.getGerritProject())) {
                query += " project:" + project.getGerritProject();
                details = gerritEJB.gerritQuery(query, selectedGerrit.getId());
            } else {
                log.info("Project {} does not have gerrit or gerritProject", projectId);
            }
        } catch (NotFoundException ex) {
            log.warn("Search '{}' failed from {}", query, selectedGerrit);
        }
    }

    public String trigger() {
        String action = "";

        if (!httpSessionBean.isLoggedIn()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Start failed.", "You have to login first!");
            return "login?faces-redirect=true";
        }

        if (selectedDetails.length == 0) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "No changes selected", "");
            return action;
        }

        if (verification == null) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Start failed.", "Verification start is not supported for this job.");
            return null;
        }

        log.info("Trigger hit for {} changes. Triggered by {}", selectedDetails.length, httpSessionBean.getSysUserLoginName());

        List<GerritDetail> start = Arrays.asList(selectedDetails);
        for (GerritDetail g : start) {
            gerritJobManagerEJB.startJob(verification, g);
            addMessage(FacesMessage.SEVERITY_INFO, "Verification started", "Verification started from gerrit change " + g.getId());
        }

        details.removeAll(start);
        selectedDetails = null;

        return action;
    }

    public String start(GerritDetail detail) {
        if (!httpSessionBean.isLoggedIn()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Start failed.", "You have to login first!");
            return "login";
        }

        if (detail == null) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Start failed.", "You must select gerrit refspec.");
            return null;
        }

        if (verification == null) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Start failed.", "Verification start is not supported for this job.");
            return null;
        }

        if (!httpSessionBean.isAdmin() && !httpSessionBean.getSysUserId().equals(verification.getOwner().getId())) {
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Unauthorized operation.", "This is not your verification!");
            return null;
        }
        Boolean jobStartOK = gerritJobManagerEJB.startJob(verification, detail);
        if (!jobStartOK) {
            log.warn("Starting toolbox job failed with detail: {}!", detail);
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Start failed.", "Could not start the job, please try again or contact support.");
            return null;
        }
        return "verificationDetails?faces-redirect=true&verificationId="
                + verification.getId();
    }

    public Job getVerification() {
        return this.verification;
    }

    public GerritDetailDataModel getGerritData() {
        return gerridDetailDataModel;
    }

    public GerritDetail getSelectedGerritDetail() {
        return selectedGerritDetail;
    }

    public void setSelectedGerritDetail(GerritDetail selectedGerritDetail) {
        this.selectedGerritDetail = selectedGerritDetail;
    }

    public boolean isJobEnabled() {
        return verification.getDisabled() == null || !verification.getDisabled().booleanValue();
    }

    public Gerrit getSelectedGerrit() {
        return selectedGerrit;
    }

    public void setSelectedGerrit(Gerrit selectedGerrit) {
        this.selectedGerrit = selectedGerrit;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<GerritDetail> getDetails() {
        return details;
    }

    public void setDetails(List<GerritDetail> details) {
        this.details = details;
    }

    public GerritDetail[] getSelectedDetails() {
        return selectedDetails;
    }

    public void setSelectedDetails(GerritDetail[] selectedDetails) {
        this.selectedDetails = selectedDetails;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Project getProject() {
        return project;
    }

    private void populateGerritData() throws NotFoundException {
        Gerrit gerrit = jobEJB.getGerrit(verification.getId());
        if (gerrit == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Gerrit configuration missing!",
                    "Please contact support to configure gerrit properly.");
            return;
        }

        Branch branch = verification.getBranch();
        if (branch == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Branch configuration missing!",
                    "Please contact support to configure branch properly.");
            return;
        }

        Project project = branch.getProject();
        if (project == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Project configuration missing!",
                    "Please contact support to configure project properly.");
            return;
        }

        String userEmail;
        if (httpSessionBean.isAdmin() && branch.getType() == BranchType.SINGLE_COMMIT) {
            // If SCV job and admin, show all open changes in the branch and project
            userEmail = null;
        } else {
            // Otherwise, show all user's open changes in the branch and project
            userEmail = httpSessionBean.getSysUserEmail();
        }

        List<GerritDetail> gerritDetails = gerritEJB.getOpenCommits(
                userEmail, gerrit.getId(),
                branch.getName(), project.getGerritProject());

        gerridDetailDataModel = new GerritDetailDataModel(
                gerritDetails);
    }

    private void checkVerificationConfigs() throws NotFoundException {
        List<JobVerificationConf> verificationConfs = jobEJB.getVerificationConfs(verification.getId());
        List<JobVerificationConf> enabledVerificationConfs = jobEJB.getEnabledJobVerificationConfs(verification.getId());
        if (enabledVerificationConfs.size() != verificationConfs.size()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Disabled verifications!",
                    "There is one or more disabled verifications in your job configuration. Disabled verifications will not be run until re-enabled on project level.");
        }
    }
}
