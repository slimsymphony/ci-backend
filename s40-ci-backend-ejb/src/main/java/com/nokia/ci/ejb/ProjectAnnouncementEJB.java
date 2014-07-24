package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.ProjectAnnouncement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 10/1/12
 * Time: 10:33 AM
 * To change this template use File | Settings | File Templates.
 */

@Stateless
@LocalBean
public class ProjectAnnouncementEJB extends CrudFunctionality<ProjectAnnouncement> {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ProjectAnnouncementEJB.class);

    public ProjectAnnouncementEJB() {
        super(ProjectAnnouncement.class);
    }
}
