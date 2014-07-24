package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.GlobalAnnouncementEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.GlobalAnnouncement;
import com.nokia.ci.ui.model.AnnouncementModel;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 11/9/12 Time: 1:02 PM To change
 * this template use File | Settings | File Templates.
 */
@Named
@ViewScoped
public class GlobalAnnouncementsBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(GlobalAnnouncementsBean.class);
    private List<GlobalAnnouncement> announcements;
    private AnnouncementModel announcementModel;
    @Inject
    private GlobalAnnouncementEJB globalAnnouncementEJB;

    @Override
    protected void init() {
        announcements = globalAnnouncementEJB.readAll();
        announcementModel = new AnnouncementModel(announcements);
    }

    public List<GlobalAnnouncement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<GlobalAnnouncement> announcements) {
        this.announcements = announcements;
    }

    public AnnouncementModel getAnnouncementModel() {
        return announcementModel;
    }

    public void delete(GlobalAnnouncement globalAnnouncement) {
        log.info("Deleting globalAnnouncement {}", globalAnnouncement);
        try {
            globalAnnouncementEJB.delete(globalAnnouncement);
            announcements.remove(globalAnnouncement);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "GlobalAnnouncement was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting GlobalAnnouncement {} failed! Cause: {}", globalAnnouncement, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete GlobalAnnouncement failed!", "Selected GlobalAnnouncement could not be deleted!");
        }
    }
}
