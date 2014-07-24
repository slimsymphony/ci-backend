package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.SysUser;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for editing Job.
 *
 * @author jajuutin
 */
@Named
@ViewScoped
public class AdminJobEditorBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(AdminJobEditorBean.class);
    private Job job;
    @Inject
    private JobEJB jobEJB;
    @Inject
    private SysUserEJB sysUserEJB;
    private SysUser owner;
    private List<SysUser> sysUsers;

    /**
     * Creates a new instance of AdminJobEditorBean
     */
    public AdminJobEditorBean() {
    }

    @Override
    protected void init() throws NotFoundException {
        String jobId = getQueryParam("jobId");
        if (jobId == null) {
            job = new Job();
        } else {
            job = jobEJB.read(Long.parseLong(jobId));
        }

        owner = job.getOwner();
        sysUsers = sysUserEJB.readAll();
    }
    
    public Job getJob() {
        return job;
    }

    public String save() {
        log.debug("Save triggered!");

        String action = null;

        try {
            if (job.getId() != null) {
                log.debug("Updating existing job {}", job);
                jobEJB.update(job);
            } else {
                log.debug("Saving new job!");
                jobEJB.create(job);
            }
            jobEJB.setOwner(job.getId(), owner);
            action = "jobs?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save job {}! Cause: {}", job, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Job could not be saved!", "");
        }

        return action;
    }

    public String cancelEdit() {
        return "jobs?faces-redirect=true";
    }

    public SysUser getOwner() {
        return owner;
    }

    public void setOwner(SysUser owner) {
        this.owner = owner;
    }

    public List<SysUser> getSysUsers() {
        return sysUsers;
    }

    public void setSysUsers(List<SysUser> sysUsers) {
        this.sysUsers = sysUsers;
    }
}
