/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.model;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Aggregated data for a batch of builds.
 *
 * @author larryang
 */
@XmlRootElement
public class MetricsDatasetView {

    @XmlElement
    private Long buildId;
    @XmlElement
    private List<BuildEventView> buildEventViews;
    @XmlElement
    private String executor;
    @XmlElement
    private List<MemConsumptionView> memConsumptions;
    @XmlElement
    private List<TestCaseStatView> testCaseStats;
    @XmlElement
    private List<TestCoverageView> testCoverages;
    @XmlElement
    private List<BuildFailureView> buildFailures;

    public List<BuildEventView> getBuildEventViews() {
        return buildEventViews;
    }

    public void setBuildEventViews(List<BuildEventView> buildEventViews) {
        this.buildEventViews = buildEventViews;
    }

    public List<MemConsumptionView> getMemConsumptions() {
        return memConsumptions;
    }

    public void setMemConsumptions(List<MemConsumptionView> memConsumptions) {
        this.memConsumptions = memConsumptions;
    }

    public Long getBuildId() {
        return buildId;
    }

    public void setBuildId(Long buildId) {
        this.buildId = buildId;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public List<TestCaseStatView> getTestCaseStats() {
        return testCaseStats;
    }

    public void setTestCaseStats(List<TestCaseStatView> testCaseStats) {
        this.testCaseStats = testCaseStats;
    }

    public List<TestCoverageView> getTestCoverages() {
        return testCoverages;
    }

    public void setTestCoverages(List<TestCoverageView> testCoverages) {
        this.testCoverages = testCoverages;
    }

    public List<BuildFailureView> getBuildFailures() {
        return buildFailures;
    }

    public void setBuildFailures(List<BuildFailureView> buildFailures) {
        this.buildFailures = buildFailures;
    }
}
