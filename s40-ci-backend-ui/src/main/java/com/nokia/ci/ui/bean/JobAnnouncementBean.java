package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.JobAnnouncementEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.AnnouncementType;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobAnnouncement;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 9/27/12 Time: 10:10 AM To change
 * this template use File | Settings | File Templates.
 */
@Named
@ViewScoped
public class JobAnnouncementBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(JobAnnouncementBean.class);
    private JobAnnouncement announcement;
    @Inject
    private JobAnnouncementEJB jobAnnouncementEJB;
    @Inject
    private JobEJB jobEJB;
    private List<Job> jobs;
    private String id;

    @Override
    protected void init() throws NotFoundException {
        id = getQueryParam("announcementId");
        if (id == null) {
            announcement = new JobAnnouncement();
            announcement.setType(AnnouncementType.INFO);
            return;
        }
        log.debug("Finding jobAnnouncement {} for editing!", id);
        announcement = jobAnnouncementEJB.read(Long.parseLong(id));
        jobs = jobEJB.readAll();
    }

    public JobAnnouncement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(JobAnnouncement announcement) {
        this.announcement = announcement;
    }

    public String getId() {
        return id;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public String save() {
        log.debug("Save triggered!");

        String action = null;

        try {
            if (announcement.getId() != null) {
                log.debug("Updating existing jobAnnouncement {}", announcement);
                jobAnnouncementEJB.update(announcement);
            } else {
                log.debug("Saving new jobAnnouncement!");
                jobAnnouncementEJB.create(announcement);
            }
            action = "announcements?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save jobAnnouncement {}! Cause: {}", announcement, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "JobAnnouncement could not be saved!", "");
        }

        return action;
    }

    public String cancelEdit() {
        return "announcements?faces-redirect=true";
    }
}
