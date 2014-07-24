/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import com.nokia.ci.ejb.reportaction.ReportActionStatus;

/**
 * @author jajuutin
 *
 * Note: ordering of enumerations is crucial to correct behavior of CI 2.0 BE.
 * Do no not change order without a good reason.
 */
public enum BuildStatus {

    SUCCESS, UNSTABLE, FAILURE, NOT_BUILT, ABORTED;

    public BuildStatus combine(BuildStatus buildStatus) {
        if (this.ordinal() < buildStatus.ordinal()) {
            return buildStatus;
        }
        return this;
    }

    public boolean worstThan(BuildStatus buildStatus) {
        return (this.ordinal() > buildStatus.ordinal());
    }

    public boolean betterThan(BuildStatus buildStatus) {
        return (this.ordinal() < buildStatus.ordinal());
    }
    
    /*
     * Failure reporting applies also in case of NOT_BUILT or ABORTED.
     */
    public ReportActionStatus getCorrespondingReportActionStatus() {

        if (this == BuildStatus.SUCCESS) {
            return ReportActionStatus.SUCCESS;
        } else if (this == BuildStatus.UNSTABLE) {
            return ReportActionStatus.UNSTABLE;
        } else {
            return ReportActionStatus.FAILURE;
        }
    }
}
