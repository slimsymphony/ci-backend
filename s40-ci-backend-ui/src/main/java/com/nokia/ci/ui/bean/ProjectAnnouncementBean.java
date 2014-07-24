package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.ProjectAnnouncementEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.AnnouncementType;
import com.nokia.ci.ejb.model.ProjectAnnouncement;
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
public class ProjectAnnouncementBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(ProjectAnnouncementBean.class);
    private ProjectAnnouncement announcement;
    @Inject
    private ProjectAnnouncementEJB projectAnnouncementEJB;
    private String id;

    @Override
    protected void init() throws NotFoundException {
        id = getQueryParam("announcementId");
        if (id == null) {
            announcement = new ProjectAnnouncement();
            announcement.setType(AnnouncementType.INFO);
            return;
        }
        log.debug("Finding projectAnnouncement {} for editing!", id);
        announcement = projectAnnouncementEJB.read(Long.parseLong(id));
    }

    public ProjectAnnouncement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(ProjectAnnouncement announcement) {
        this.announcement = announcement;
    }

    public String getId() {
        return id;
    }

    public String save() {
        log.debug("Save triggered!");

        String action = null;

        try {
            if (announcement.getId() != null) {
                log.debug("Updating existing projectAnnouncement {}", announcement);
                projectAnnouncementEJB.update(announcement);
            } else {
                log.debug("Saving new projectAnnouncement!");
                projectAnnouncementEJB.create(announcement);
            }
            action = "announcements?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save projectAnnouncement {}! Cause: {}", announcement, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "ProjectAnnouncement could not be saved!", "");
        }

        return action;
    }

    public String cancelEdit() {
        return "announcements?faces-redirect=true";
    }
}
