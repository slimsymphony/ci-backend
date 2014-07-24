/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.annotation.ReportActionExecutorType;
import com.nokia.ci.ejb.exception.BackendMessagingException;
import com.nokia.ci.ejb.mail.MailSenderEJB;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.NotificationReportAction;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.util.ReportingUtil;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stirrone
 */

@LocalBean
@Stateless
@ReportActionExecutorType(type = NotificationReportAction.class)
public class NotificationReportActionExecutor implements ReportActionExecutor<NotificationReportAction>, Serializable {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationReportActionExecutor.class);
    private static final String EMAIL_NEWLINE_CHAR_DEFAULT = "\n";
    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private MailSenderEJB mailSenderEJB;
    
    @Override
    public void execute(NotificationReportAction action, BuildGroup buildGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void execute(NotificationReportAction action, Build build) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void execute(NotificationReportAction action, BuildGroup buildGroup, List<Change> changes) {
        
        if (action == null || changes == null || changes.isEmpty()) {
            return;
        }
        
        String emailNewline = sysConfigEJB.getValue(SysConfigKey.EMAIL_NEWLINE_CHAR, EMAIL_NEWLINE_CHAR_DEFAULT);
        
        //construct message
        String recipients = action.getRecipients();
        
        if (action.getUseChangeAuthors()) {
            recipients = ReportingUtil.appendAuthors(recipients, changes);
        }
        
        if (StringUtils.isEmpty(recipients)) {
            log.warn("No recipients configured for notification {}, skipping message sending.", action.getId());
            return;
        }
        
        StringBuilder message = new StringBuilder();
        if (action.getMessage() != null) {
            message.append(action.getMessage());
        }
        
        String baseURL = sysConfigEJB.getValue(SysConfigKey.BASE_URL, "");
        if (!StringUtils.isEmpty(baseURL)) {
            message.append(emailNewline).append(emailNewline).append("Build URL: ").append(baseURL).append("/page/build/").append(buildGroup.getId());
        }
        message.append(emailNewline).append(emailNewline).append("Unstable Changes:");
        List<String> formattedChanges = ReportingUtil.formatChanges(changes);
        for (String fc : formattedChanges) {
            message.append(emailNewline).append(fc);
        }
        
        //send message
        try {
            mailSenderEJB.sendMail(recipients, action.getSubject(), message.toString());
        } catch (BackendMessagingException bme) {
            log.error("Can not perform email action!", bme);
        }
    }
    
}
