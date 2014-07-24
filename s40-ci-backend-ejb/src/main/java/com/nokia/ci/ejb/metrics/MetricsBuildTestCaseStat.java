/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.model.BuildStatus;
import java.util.Date;

/**
 *
 * @author larryang
 */
public class MetricsBuildTestCaseStat extends MetricsBuild {

    private String componentName;
    private Integer totalCount;
    private Integer passedCount;
    private Integer failedCount;
    private Integer naCount;

    public MetricsBuildTestCaseStat(Long id, BuildStatus status, Date startTime, Date endTime, String verificationUuid, String productUuid) {

        super(id, status, startTime, endTime, verificationUuid, productUuid);
    }

    public MetricsBuildTestCaseStat(MetricsBuild metricsBuild) {

        super(metricsBuild.getId(), metricsBuild.getStatus(), metricsBuild.getStartTime(), metricsBuild.getEndTime(),
                metricsBuild.getVerificationUuid(), metricsBuild.getProductUuid());
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Integer getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(Integer passedCount) {
        this.passedCount = passedCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public Integer getNaCount() {
        return naCount;
    }

    public void setNaCount(Integer naCount) {
        this.naCount = naCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;

        if (this.totalCount > 0) {
            setTriggerTest(true);
        }
    }
}