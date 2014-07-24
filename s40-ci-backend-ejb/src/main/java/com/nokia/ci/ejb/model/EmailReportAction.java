package com.nokia.ci.ejb.model;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Entity class for email report action.
 * 
 * @author vrouvine
 */
@Entity
@Table(name = "EMAIL_REPORT_ACTION")
public class EmailReportAction extends ReportAction {
    
    @Lob
    private String recipients;
    private String subject;
    @Lob
    private String message;
    private Boolean useCommitAuthors = false;
    private Boolean sendOnlyIfStatusChanged = false;

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getUseCommitAuthors() {
        return useCommitAuthors;
    }

    public void setUseCommitAuthors(Boolean useCommitAuthors) {
        this.useCommitAuthors = useCommitAuthors;
    }

    public Boolean getSendOnlyIfStatusChanged() {
        return sendOnlyIfStatusChanged;
    }

    public void setSendOnlyIfStatusChanged(Boolean sendOnlyIfStatusChanged) {
        this.sendOnlyIfStatusChanged = sendOnlyIfStatusChanged;
    }
}
