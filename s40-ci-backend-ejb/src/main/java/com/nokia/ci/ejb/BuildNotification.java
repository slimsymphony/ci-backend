/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import java.util.Map;

/**
 *
 * @author jajuutin
 */
public class BuildNotification extends Notification {

    private String jobName;
    private String jobDisplayName;
    private String buildUrl;
    private String jobUrl;
    private int buildNumber;
    private BuildPhase buildPhase;
    private BuildStatus buildStatus;
    private String buildRefSpec;
    private Long buildId;
    private String monitor;
    private Long buildGroupId;
    private Map<String, String> parameters;
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("jobName:").append(jobName);
        sb.append(",jobDisplayName:").append(jobDisplayName);
        sb.append(",buildUrl:").append(buildUrl);
        sb.append(",jobUrl:").append(jobUrl);
        sb.append(",buildNumber:").append(buildNumber);
        sb.append(",buildPhase:").append(buildPhase);
        sb.append(",buildStatus:").append(buildStatus);
        sb.append(",buildRefSpec:").append(buildRefSpec);
        sb.append(",buildId:").append(buildId);
        sb.append(",monitor:").append(monitor);
        sb.append(",buildGroupId:").append(buildGroupId);
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                sb.append(",");
                sb.append(entry.getKey());
                sb.append(":");
                sb.append(entry.getValue());
            }
        }
        return sb.toString();
    }

    /**
     * @return the jobName
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * @param jobName the jobName to set
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * @return the jobDisplayName
     */
    public String getJobDisplayName() {
        return jobDisplayName;
    }

    /**
     * @param jobDisplayName the jobDisplayName to set
     */
    public void setJobDisplayName(String jobDisplayName) {
        this.jobDisplayName = jobDisplayName;
    }

    /**
     * @return the buildUrl
     */
    public String getBuildUrl() {
        return buildUrl;
    }

    /**
     * @param buildUrl the buildUrl to set
     */
    public void setBuildUrl(String buildUrl) {
        this.buildUrl = buildUrl;
    }

    /**
     * @return the buildNumber
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * @param buildNumber the buildNumber to set
     */
    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * @return the buildPhase
     */
    public BuildPhase getBuildPhase() {
        return buildPhase;
    }

    /**
     * @param buildPhase the buildPhase to set
     */
    public void setBuildPhase(BuildPhase buildPhase) {
        this.buildPhase = buildPhase;
    }

    /**
     * @return the buildStatus
     */
    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    /**
     * @param buildStatus the buildStatus to set
     */
    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
    }

    /**
     * @return the jobUrl
     */
    public String getJobUrl() {
        return jobUrl;
    }

    /**
     * @param jobUrl the jobUrl to set
     */
    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    /**
     * @return the buildRefSpec
     */
    public String getBuildRefSpec() {
        return buildRefSpec;
    }

    /**
     * @param buildRefSpec the buildRefSpec to set
     */
    public void setBuildRefSpec(String buildRefSpec) {
        this.buildRefSpec = buildRefSpec;
    }

    /**
     * @return the buildId
     */
    public Long getBuildId() {
        return buildId;
    }

    /**
     * @param buildId the buildId to set
     */
    public void setBuildId(Long buildId) {
        this.buildId = buildId;
    }

    public String getMonitor() {
        return monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }

    public Long getBuildGroupId() {
        return buildGroupId;
    }

    public void setBuildGroupId(Long buildGroupId) {
        this.buildGroupId = buildGroupId;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}