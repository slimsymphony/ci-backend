package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Job;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for jobs.
 *
 * @author jajuutin
 */
@Named
public class JobsBean extends DataFilterBean<Job> {

    private static Logger log = LoggerFactory.getLogger(JobsBean.class);
    private List<Job> jobs;
    @Inject
    private JobEJB jobEJB;
    @Inject
    private HttpSessionBean httpSessionBean;

    @Override
    protected void init() {
        initJobs();
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void delete(Job job) {
        log.info("Deleting job {}", job);
        try {
            String name = job.getName();
            jobEJB.delete(job);
            jobs.remove(job);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Job " + name + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting job {} failed! Cause: {}", job, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete job failed!", "Selected job could not be deleted!");
        }
    }

    private void initJobs() {
        jobs = new ArrayList<Job>();
        if (httpSessionBean.isAdmin()) {
            jobs = jobEJB.readAll();
            return;
        }

        List<Job> allJobs = jobEJB.readAll();
        for (Job j : allJobs) {
            if (httpSessionBean.hasAdminAccessToProject(j.getBranch().getProject().getId())) {
                jobs.add(j);
            }
        }
    }
}
