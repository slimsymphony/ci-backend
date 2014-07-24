/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import java.util.Date;

/**
 *
 * @author jajuutin
 */
public class MetricsHangtime {
    private Date timestamp;
    private String commitId;
    private Long hangtime = 0L; // ms

    /**
     * @return the commitId
     */
    public String getCommitId() {
        return commitId;
    }

    /**
     * @param commitId the commitId to set
     */
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    /**
     * @return the hang time
     */
    public Long getHangtime() {
        return hangtime;
    }

    /**
     * @param time the time to set
     */
    public void setHangtime(Long time) {
        this.hangtime = time;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
