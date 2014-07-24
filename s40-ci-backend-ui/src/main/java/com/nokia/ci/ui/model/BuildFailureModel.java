/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.model.BuildFailure;
import com.nokia.ci.ejb.model.VerificationFailureReason;

/**
 *
 * @author hhellgre
 */
public class BuildFailureModel {

    private BuildFailure failure;
    private VerificationFailureReason reason;
    private String comment;
    private String url;

    public BuildFailure getFailure() {
        return failure;
    }

    public void setFailure(BuildFailure failure) {
        this.failure = failure;
    }

    public VerificationFailureReason getReason() {
        return reason;
    }

    public void setReason(VerificationFailureReason reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
