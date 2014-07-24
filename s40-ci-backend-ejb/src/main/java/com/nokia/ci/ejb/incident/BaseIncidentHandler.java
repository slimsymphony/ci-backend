package com.nokia.ci.ejb.incident;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.event.IncidentEventContent;
import com.nokia.ci.ejb.exception.BackendMessagingException;
import com.nokia.ci.ejb.mail.MailSenderEJB;
import com.nokia.ci.ejb.mail.MailSenderEJB.EmailPriority;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUser;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 3/22/13 Time: 3:02 PM To change
 * this template use File | Settings | File Templates.
 */
public abstract class BaseIncidentHandler implements Serializable {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BaseIncidentHandler.class);
    private static final String EMAIL_NEWLINE_CHAR_DEFAULT = "\n";
    public static final String INCIDENT_EMAIL_SUBJECT = "New CI Backend incident!";
    @EJB
    private IncidentEventHandlerEJB incidentEventHandlerEJB;
    @EJB
    private MailSenderEJB mailSenderEJB;
    @EJB
    private SysUserEJB sysUserEJB;
    @EJB
    private SysConfigEJB sysConfigEJB;

    protected void createIncident(IncidentType type, String description) {
        incidentEventHandlerEJB.persist(new IncidentEventContent(type, description));
        sendIncidentEmail(type, description);
    }

    private void sendIncidentEmail(IncidentType type, String description) {
        String emailNewline = sysConfigEJB.getValue(SysConfigKey.EMAIL_NEWLINE_CHAR, EMAIL_NEWLINE_CHAR_DEFAULT);
        try {
            String message = "CI Backend has detected new incident in the system. Please see the followed description for further information:";
            message += emailNewline + emailNewline;
            message += type.toString() + ": " + description;
            message += emailNewline + emailNewline;
            message += "Please visit " + sysConfigEJB.getValue(SysConfigKey.BASE_URL, "CI UI") + " for more information";
            List<SysUser> admins = sysUserEJB.getSysAdmins();
            mailSenderEJB.sendMailSysUsers(admins, INCIDENT_EMAIL_SUBJECT, message, EmailPriority.PRIORITY_HIGH);
        } catch (BackendMessagingException ex) {
            log.error("Could not send incident emails: {}", ex);
        }
    }
}
