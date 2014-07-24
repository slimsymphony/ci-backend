/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.model.BuildStatus;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jajuutin
 */
public class MetricsVerificationGroup extends MetricsGroup<MetricsVerification> {
    private Long runtimeAverage = null; // in ms.
    private long summarizedRuntime = 0L; // in ms.
    private long passedCount = 0L;
    private long failedCount = 0L;
    private Double passrate = null; // in %
    private Integer testTriggeringBuildCount = 0;

    /**
     * Append a metrics build into group. Use only this method to change
     * contents of underlying list of metricsBuilds.
     *
     * @param mv
     */
    public void add(MetricsVerification mv) {
        getItems().add(mv);

        final Long individualRuntime = mv.getRuntime();
        if (individualRuntime != null) {
            summarizedRuntime += individualRuntime;
        }

        runtimeAverage = null;
        passrate = null;

        if (mv.getResult() == MetricsVerificationResult.PASSED) {
            passedCount++;
        } else {
            failedCount++;
        }
        
        if (mv.isTriggerTest()){
            testTriggeringBuildCount++;
        }
    }

    public Map<BuildStatus, Long> getStatusMap() {
        Map<BuildStatus, Long> map = new HashMap<BuildStatus, Long>();
        
        for (MetricsVerification mv : getItems()) {
            BuildStatus buildStatus = mv.getStatus();

            Long currentCount = map.get(buildStatus);
            if (currentCount == null) {
                currentCount = new Long(0L);
            }
            currentCount++;
            map.put(buildStatus, currentCount);
        }

        return map;
    }

    /**
     * @return the runtimeAverage
     */
    public long getRuntimeAverage() {
        if (getItems().isEmpty()) {
            return 0L;
        }

        if (runtimeAverage == null) {
            runtimeAverage = summarizedRuntime / getItems().size();
        }

        return runtimeAverage;
    }

    /**
     * @param runtimeAverage the runtimeAverage to set
     */
    public void setRuntimeAverage(long runtimeAverage) {
        this.runtimeAverage = Long.valueOf(runtimeAverage);
    }

    /**
     * @return the summarizedRuntime
     */
    public long getSummarizedRuntime() {
        return summarizedRuntime;
    }

    /**
     * @param summarizedRuntime the summarizedRuntime to set
     */
    public void setSummarizedRuntime(long summarizedRuntime) {
        this.summarizedRuntime = summarizedRuntime;
    }

    /**
     * @return the passedCount
     */
    public long getPassedCount() {
        return passedCount;
    }

    /**
     * @param passedCount the passedCount to set
     */
    public void setPassedCount(long passedCount) {
        this.passedCount = passedCount;
    }

    /**
     * @return the failedCount
     */
    public long getFailedCount() {
        return failedCount;
    }

    /**
     * @param failedCount the failedCount to set
     */
    public void setFailedCount(long failedCount) {
        this.failedCount = failedCount;
    }

    /**
     * @return the passrate
     */
    public double getPassrate() {
        if (getItems().isEmpty()) {
            return 100D;
        }

        if (passrate == null) {
            double tmpPassedCount = passedCount;
            double tmpMetricsBuildsSize = getItems().size();
            passrate = (tmpPassedCount / tmpMetricsBuildsSize) * 100D;
        }

        return passrate;
    }

    /**
     * @param passrate the passrate to set
     */
    public void setPassrate(double passrate) {
        this.passrate = Double.valueOf(passrate);
    }

    public Integer getTestTriggeringBuildCount() {
        return testTriggeringBuildCount;
    }

    public void setTestTriggeringBuildCount(Integer testTriggeringBuildCount) {
        this.testTriggeringBuildCount = testTriggeringBuildCount;
    }

}
