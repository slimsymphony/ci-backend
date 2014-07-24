/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author stirrone
 */
@Entity
@Table(name = "NOTIFICATION_REPORT_ACTION")
public class NotificationReportAction extends ReportAction {

    private String recipients;
    private String subject;
    private String message;
    private Boolean useChangeAuthors = false;
    
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
    
    public Boolean getUseChangeAuthors() {
        return useChangeAuthors;
    }
    
    public void setUseChangeAuthors(Boolean useChangeAuthors) {
        this.useChangeAuthors = useChangeAuthors;
    }
}
