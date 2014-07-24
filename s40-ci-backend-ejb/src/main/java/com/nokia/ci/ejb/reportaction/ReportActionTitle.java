/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.reportaction;

/**
 *
 * @author stirrone
 */
public enum ReportActionTitle {
    
    SUCCESS(ReportActionStatus.SUCCESS.toString()),
    UNSTABLE(ReportActionStatus.UNSTABLE.toString()),
    UNSTABLE_NO_BLOCKING(ReportActionStatus.UNSTABLE.toString() + " - NON-BLOCKING ERRORS ONLY"),
    FAILURE(ReportActionStatus.FAILURE.toString()),
    NOTIFY_UNSTABLE_MERGE("NOTIFY UNSTABLE MERGE");
    
    private String stringValue;
    
    private ReportActionTitle(String stringValue) {
        this.stringValue = stringValue;
    }
    
    public String getStringValue() {
        return stringValue;
    }
    
    @Override
    public String toString() {
        return stringValue;
    }
}
