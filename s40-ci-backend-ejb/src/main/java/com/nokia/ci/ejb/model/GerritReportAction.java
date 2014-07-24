package com.nokia.ci.ejb.model;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Entity class for gerrit report action.
 * 
 * @author vrouvine
 */
@Entity
@Table(name = "GERRIT_REPORT_ACTION")
public class GerritReportAction extends ReportAction {

    private Integer verifiedScore;
    private Integer reviewScore;
    @Lob
    private String message;
    private Boolean abandon = false;

    public Integer getVerifiedScore() {
        return verifiedScore;
    }

    public void setVerifiedScore(Integer verifiedScore) {
        this.verifiedScore = verifiedScore;
    }

    public Integer getReviewScore() {
        return reviewScore;
    }

    public void setReviewScore(Integer reviewScore) {
        this.reviewScore = reviewScore;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getAbandon() {
        return abandon;
    }

    public void setAbandon(Boolean abandon) {
        this.abandon = abandon;
    }
}
