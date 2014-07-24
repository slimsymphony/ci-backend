package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.GlobalAnnouncement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 9/27/12
 * Time: 10:49 AM
 * To change this template use File | Settings | File Templates.
 */

@Stateless
@LocalBean
public class GlobalAnnouncementEJB extends CrudFunctionality<GlobalAnnouncement> {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(GlobalAnnouncementEJB.class);

    public GlobalAnnouncementEJB() {
        super(GlobalAnnouncement.class);
    }

}
