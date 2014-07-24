package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.GlobalAnnouncementEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.AnnouncementType;
import com.nokia.ci.ejb.model.GlobalAnnouncement;
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
public class GlobalAnnouncementBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(GlobalAnnouncementBean.class);
    private GlobalAnnouncement announcement;
    @Inject
    private GlobalAnnouncementEJB globalAnnouncementEJB;
    private String id;

    @Override
    protected void init() throws NotFoundException {
        id = getQueryParam("announcementId");
        if (id == null) {
            announcement = new GlobalAnnouncement();
            announcement.setType(AnnouncementType.INFO);
            return;
        }
        log.debug("Finding globalAnnouncement {} for editing!", id);
        announcement = globalAnnouncementEJB.read(Long.parseLong(id));
    }

    public GlobalAnnouncement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(GlobalAnnouncement announcement) {
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
                log.debug("Updating existing globalAnnouncement {}", announcement);
                globalAnnouncementEJB.update(announcement);
            } else {
                log.debug("Saving new globalAnnouncement!");
                globalAnnouncementEJB.create(announcement);
            }
            action = "announcements?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save globalAnnouncement {}! Cause: {}", announcement, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "GlobalAnnouncement could not be saved!", "");
        }

        return action;
    }

    public String cancelEdit() {
        return "announcements?faces-redirect=true";
    }
}
