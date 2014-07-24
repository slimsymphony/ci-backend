package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.util.LDAPUser;
import com.nokia.ci.ejb.util.LDAPUtil;
import com.nokia.ci.ejb.util.TimezoneUtil;
import com.nokia.ci.ui.exception.QueryParamException;
import com.unboundid.ldap.sdk.LDAPException;
import java.security.GeneralSecurityException;
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
public class JobShareBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(JobShareBean.class);
    private Job job;
    private String jobDisplayName;
    private Boolean createCopy;
    private LDAPUser userToShare;
    private Project project;
    @Inject
    private JobEJB jobEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    private ProjectEJB projectEJB;
    private LDAPUtil ldapUtil;
    @Inject
    private SysUserEJB sysUserEJB;

    public JobShareBean() {
        createCopy = true;
        ldapUtil = new LDAPUtil();
    }

    @Override
    protected void init() throws QueryParamException, BackendAppException {
        String jobId = getMandatoryQueryParam("verificationId");
        log.debug("Finding job {} for sharing!", jobId);
        job = jobEJB.readSecure(Long.parseLong(jobId));
        project = projectEJB.read(job.getProjectId());
        jobDisplayName = job.getDisplayName();
    }

    public Boolean getCreateCopy() {
        return createCopy;
    }

    public void setCreateCopy(Boolean createCopy) {
        this.createCopy = createCopy;
    }

    public Job getJob() {
        return job;
    }

    public String getJobDisplayName() {
        return jobDisplayName;
    }

    public void setJobDisplayName(String jobDisplayName) {
        this.jobDisplayName = jobDisplayName;
    }

    public LDAPUser getUserToShare() {
        return userToShare;
    }

    public void setUserToShare(LDAPUser userToShare) {
        this.userToShare = userToShare;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String share() throws NotFoundException {
        String action = null;
        Job newJob = null;

        log.debug("Share triggered!");

        if (!httpSessionBean.isLoggedIn()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Job share failed.", "You have to login first!");
            return "login?faces-redirect=true";
        }

        if (userToShare == null) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Job share failed.", "Please specify the user to share the job!");
            return null;
        }

        SysUser sysUser;
        try {
            sysUser = sysUserEJB.getSysUser(userToShare.getUsername());
            sysUser.setEmail(userToShare.getEmail());
            sysUser.setRealName(userToShare.getRealname());
            if (sysUser.getTimezone() == null) {
                sysUser.setTimezone(TimezoneUtil.getTimezoneByNokiaSite(userToShare.getNokiaSite()));
            }
            sysUserEJB.update(sysUser);
        } catch (NotFoundException ex) {
            sysUser = sysUserEJB.createUser(userToShare);
        }

        if (createCopy == true) {
            newJob = jobEJB.copyJob(job.getId(), sysUser, jobDisplayName);
        } else {
            // Should be triggered if we are to implement the master-slave sharing
        }

        if (newJob != null && newJob.getId() != null) {
            action = "verificationDetails?faces-redirect=true&verificationId="
                    + job.getId();
        }

        return action;
    }

    public String cancelEdit() {
        String action = null;

        if (job != null && job.getId() != null) {
            action = "verificationDetails?faces-redirect=true&verificationId="
                    + job.getId();
        }

        return action;
    }

    public boolean isShareableJob() {
        if (job == null || job.getBranch() == null) {
            return false;
        }

        if (job.getBranch().getType() == BranchType.TOOLBOX || job.getBranch().getType() == BranchType.DRAFT) {
            return true;
        }

        return false;
    }

    public List<LDAPUser> searchUser(String query) {
        List<LDAPUser> ret = new ArrayList<LDAPUser>();
        try {
            ret = ldapUtil.search(query);
        } catch (LDAPException e) {
            log.warn("LDAP Exception when searching for {}", query);
        } catch (GeneralSecurityException e) {
            log.warn("General Security Exception when searching for {}", query);
        }
        return ret;
    }
}
