package com.nokia.ci.ui.model;

import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 *
 * @author larryang
 */
public class BuildEventTiming {

    private long scmStartTimestamp = 0L;
    private long scmEndTimestamp = 0L;
    private long buildStartTimestamp = 0L;
    private long buildEndTimestamp = 0L;
    private long testStartTimestamp = 0L;
    private long testEndTimestamp = 0L;
    private String executor = "";
    private int scmPercentage = 0;
    private int buildPercentage = 0;
    private int testPercentage = 0;
    private int totalPercentage = 0;
    private String jobName = "";

    public long getBuildEndTimestamp() {
        return buildEndTimestamp;
    }

    public void setBuildEndTimestamp(long buildEndTimestamp) {
        this.buildEndTimestamp = buildEndTimestamp;
    }

    public long getBuildStartTimestamp() {
        return buildStartTimestamp;
    }

    public void setBuildStartTimestamp(long buildStartTimestamp) {
        this.buildStartTimestamp = buildStartTimestamp;
    }

    public long getScmEndTimestamp() {
        return scmEndTimestamp;
    }

    public void setScmEndTimestamp(long scmEndTimestamp) {
        this.scmEndTimestamp = scmEndTimestamp;
    }

    public long getScmStartTimestamp() {
        return scmStartTimestamp;
    }

    public void setScmStartTimestamp(long scmStartTimestamp) {
        this.scmStartTimestamp = scmStartTimestamp;
    }

    public long getTestEndTimestamp() {
        return testEndTimestamp;
    }

    public void setTestEndTimestamp(long testEndTimestamp) {
        this.testEndTimestamp = testEndTimestamp;
    }

    public long getTestStartTimestamp() {
        return testStartTimestamp;
    }

    public void setTestStartTimestamp(long testStartTimestamp) {
        this.testStartTimestamp = testStartTimestamp;
    }

    public long getBuildTimeSpan() {
        return (buildEndTimestamp - buildStartTimestamp);
    }

    public long getScmTimeSpan() {
        return (scmEndTimestamp - scmStartTimestamp);
    }

    public long getTestTimeSpan() {
        return (testEndTimestamp - testStartTimestamp);
    }
    
    public long getTotalTimeSpan(){
        return (getScmTimeSpan() + getBuildTimeSpan() + getTestTimeSpan());
    }
    
    public String getBuildTimeSpanFmtedStr() {
        return DurationFormatUtils.formatDuration(getBuildTimeSpan(), "HH:mm:ss");
    }

    public String getScmTimeSpanFmtedStr() {
        return DurationFormatUtils.formatDuration(getScmTimeSpan(), "HH:mm:ss");
    }

    public String getTestTimeSpanFmtedStr() {
        return DurationFormatUtils.formatDuration(getTestTimeSpan(), "HH:mm:ss");
    }
    
    public String getTotalTimeSpanFmtedStr(){
        return DurationFormatUtils.formatDuration(getTotalTimeSpan(), "HH:mm:ss");
    }

    public int getBuildPercentage(){
        return buildPercentage;
    }

    public void setBuildPercentage(int buildPercentage) {
        this.buildPercentage = buildPercentage;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public int getScmPercentage() {
        return scmPercentage;
    }

    public void setScmPercentage(int scmPercentage) {
        this.scmPercentage = scmPercentage;
    }

    public int getTestPercentage() {
        return testPercentage;
    }

    public void setTestPercentage(int testPercentage) {
        this.testPercentage = testPercentage;
    }

    public int getTotalPercentage() {
        return totalPercentage;
    }

    public void setTotalPercentage(int totalPercentage) {
        this.totalPercentage = totalPercentage;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

}
