/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.model.BuildStatus;
import java.util.Date;

/**
 * Metrics specific build class with additional information required for metrics.
 * Data that is not used by metrics is not included.
 *
 * @author jajuutin
 */
public class MetricsVerification {
    // Members from actual BUILD or BUILD_GROUP table.
    private Long id;
    private BuildStatus status;
    private Date startTime;
    private Date endTime;
    private boolean triggerTest;

    // Additional information metrics specific information.
    private long runtime = 0; // in ms.
    private MetricsVerificationResult result;
    
    /**
     * This constructor is provided to enable partial table loading
     * with criteria builder. Partial table loading will remove a lot of database
     * and cpu usage.
     * 
     * @param id
     * @param status
     * @param startTime
     * @param endTime 
     */
    public MetricsVerification (Long id, BuildStatus status, Date startTime, Date endTime) {
        // Members from actual build table.
        this.id = id;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.triggerTest = false;
        
        if (endTime != null && startTime != null) {
            runtime = endTime.getTime() - startTime.getTime();
        }
        
        result = MetricsVerificationResult.getResult(status);
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * @return the status
     */
    public BuildStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(BuildStatus status) {
        this.status = status;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the runtime
     */
    public long getRuntime() {
        return runtime;
    }

    /**
     * @param runtime the runtime to set
     */
    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    /**
     * @return the result
     */
    public MetricsVerificationResult getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(MetricsVerificationResult result) {
        this.result = result;
    }

    public boolean isTriggerTest() {
        return triggerTest;
    }

    public void setTriggerTest(boolean triggerTest) {
        this.triggerTest = triggerTest;
    }

}
