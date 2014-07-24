package com.nokia.ci.ejb;

import com.nokia.ci.ejb.annotation.ReportActionExecutorType;
import com.nokia.ci.ejb.exception.BackendMessagingException;
import com.nokia.ci.ejb.mail.MailSenderEJB;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.EmailReportAction;
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

@LocalBean
@Stateless
@ReportActionExecutorType(type = EmailReportAction.class)
public class EmailReportActionExecutor implements ReportActionExecutor<EmailReportAction>, Serializable {

    private static final Logger log = LoggerFactory.getLogger(EmailReportActionExecutor.class);
    private static final String EMAIL_NEWLINE_CHAR_DEFAULT = "\n";
    @EJB
    private MailSenderEJB mailSenderEJB;
    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private JobEJB jobEJB;
    @EJB
    private BuildGroupEJB buildGroupEJB;

    @Override
    public void execute(EmailReportAction action, BuildGroup buildGroup) {
        if (action == null || buildGroup == null) {
            return;
        }

        if (Boolean.TRUE.equals(action.getSendOnlyIfStatusChanged())) {
            BuildGroup previousBuildGroup = jobEJB.getPreviousBuildGroup(buildGroup.getJob().getId(), buildGroup.getId());
            if (previousBuildGroup != null && buildGroup.getStatus() == previousBuildGroup.getStatus()) {
                // Previous status equals with current status. No need to send emails.
                log.info("No need to send emails because previous buildGroup status matches to current buildGroup ({}) status", buildGroup);
                return;
            }
        }

        String emailNewline = sysConfigEJB.getValue(SysConfigKey.EMAIL_NEWLINE_CHAR, EMAIL_NEWLINE_CHAR_DEFAULT);

        String recipients = action.getRecipients();

        if (action.getUseCommitAuthors()) {
            recipients = ReportingUtil.appendAuthors(recipients, buildGroup);
        }
        
        if (StringUtils.isEmpty(recipients)) {
            log.warn("No recipients configured for notification {}, skipping message sending.", action.getId());
            return;
        }

        String baseURL = sysConfigEJB.getValue(SysConfigKey.BASE_URL, "");

        StringBuilder message = new StringBuilder();
        if (action.getMessage() != null) {
            message.append(action.getMessage());
        }

        if (!StringUtils.isEmpty(baseURL)) {
            message.append(emailNewline).append(emailNewline)
                    .append("Build URL: ")
                    .append(baseURL).append("/page/build/").append(buildGroup.getId());
        }

        // Add build results
        appendBuildResults(buildGroup, message, emailNewline);
        // Add changes
        appendChanges(buildGroup, message, emailNewline);
        // Add build result details
        appendBuildResultDetails(buildGroup, message, emailNewline);
        
        try {
            mailSenderEJB.sendMail(recipients, action.getSubject(), message.toString());
        } catch (BackendMessagingException bme) {
            log.error("Can not perform email action!", bme);
        }
    }

    @Override
    public void execute(EmailReportAction action, Build build) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void execute(EmailReportAction action, BuildGroup buildGroup, List<Change> changes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void appendBuildResults(BuildGroup buildGroup, StringBuilder message, String emailNewline) {
        List<String> buildResults = ReportingUtil.formatBuildResults(buildGroup.getBuilds());
        if (buildResults != null && !buildResults.isEmpty()) {
            message.append(emailNewline).append(emailNewline).append("Verification Results:");
            for (String result : buildResults) {
                message.append(emailNewline).append(result);
            }
        }
    }

    private void appendChanges(BuildGroup buildGroup, StringBuilder message, String emailNewline) {
        List<String> changes = ReportingUtil.formatChanges(buildGroup.getChanges());
        if (!changes.isEmpty()) {
            message.append(emailNewline).append(emailNewline).append("Changes:");
            for (String change : changes) {
                message.append(emailNewline).append(change);
            }
        } else {
            log.warn("No changes found for build group {}", buildGroup);
        }
    }

    private void appendBuildResultDetails(BuildGroup buildGroup, StringBuilder message, String emailNewline) {
        List<String> buildResultDetails = ReportingUtil.formatBuildResultDetailsParams(buildGroupEJB.getBuildResultDetailsParams((buildGroup.getId())));
        if (buildResultDetails != null && !buildResultDetails.isEmpty()) {
            message.append(emailNewline).append(emailNewline).append("Build Result Details:");
            for (String detail : buildResultDetails) {
                message.append(emailNewline).append(detail);
            }
        } else {
            log.warn("No build result details found for build group {}", buildGroup);
        }
    }
}
