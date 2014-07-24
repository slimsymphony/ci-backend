package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.ProjectAnnouncementEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.ProjectAnnouncement;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 11/9/12 Time: 2:46 PM To change
 * this template use File | Settings | File Templates.
 */
@Named
@ViewScoped
public class ProjectAnnouncementsBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(ProjectAnnouncementsBean.class);
    private List<ProjectAnnouncement> announcements;
    @Inject
    private ProjectAnnouncementEJB projectAnnouncementEJB;

    @Override
    protected void init() {
        announcements = projectAnnouncementEJB.readAll();
    }

    public List<ProjectAnnouncement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<ProjectAnnouncement> announcements) {
        this.announcements = announcements;
    }

    public void delete(ProjectAnnouncement projectAnnouncement) {
        log.info("Deleting projectAnnouncement {}", projectAnnouncement);
        try {
            projectAnnouncementEJB.delete(projectAnnouncement);
            announcements.remove(projectAnnouncement);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "ProjectAnnouncement was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting ProjectAnnouncement {} failed! Cause: {}", projectAnnouncement, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete ProjectAnnouncement failed!", "Selected ProjectAnnouncement could not be deleted!");
        }
    }
}
