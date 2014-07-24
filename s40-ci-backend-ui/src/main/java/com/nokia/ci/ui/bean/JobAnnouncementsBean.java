package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.JobAnnouncementEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.JobAnnouncement;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 11/9/12 Time: 2:23 PM To change
 * this template use File | Settings | File Templates.
 */
@Named
@ViewScoped
public class JobAnnouncementsBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(JobAnnouncementsBean.class);
    private List<JobAnnouncement> announcements;
    @Inject
    private JobAnnouncementEJB jobAnnouncementEJB;

    @Override
    protected void init() {
        announcements = jobAnnouncementEJB.readAll();
    }

    public List<JobAnnouncement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<JobAnnouncement> announcements) {
        this.announcements = announcements;
    }

    public void delete(JobAnnouncement jobAnnouncement) {
        log.info("Deleting jobAnnouncement {}", jobAnnouncement);
        try {
            jobAnnouncementEJB.delete(jobAnnouncement);
            announcements.remove(jobAnnouncement);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "JobAnnouncement was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting JobAnnouncement {} failed! Cause: {}", jobAnnouncement, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete JobAnnouncement failed!", "Selected JobAnnouncement could not be deleted!");
        }
    }
}
