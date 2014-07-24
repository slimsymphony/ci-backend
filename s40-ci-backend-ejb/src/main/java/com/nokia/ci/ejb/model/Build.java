package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class for Build.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "BUILD")
public class Build extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int buildNumber;
    @Enumerated(EnumType.STRING)
    private BuildPhase phase;
    @Enumerated(EnumType.STRING)
    private BuildStatus status;
    private String url;
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    @OneToOne(mappedBy = "build", cascade = CascadeType.ALL)
    private BuildVerificationConf buildVerificationConf;
    private String refSpec;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private BuildGroup buildGroup;
    @ManyToMany
    @JoinTable(name = "BUILD_BUILD")
    private List<Build> parentBuilds = new ArrayList<Build>();
    @ManyToMany(mappedBy = "parentBuilds")
    private List<Build> childBuilds = new ArrayList<Build>();
    private String jobName;
    private String jobDisplayName;
    @Lob
    private String jobUrl;
    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL)
    private List<BuildEvent> buildEvents = new ArrayList<BuildEvent>();
    private String executor;
    private Boolean startNode = false;
    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL)
    private List<MemConsumption> memConsumptions = new ArrayList<MemConsumption>();
    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL)
    private List<TestCaseStat> testCaseStats = new ArrayList<TestCaseStat>();
    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL)
    private List<TestCoverage> testCoverages = new ArrayList<TestCoverage>();
    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL)
    private List<BuildFailure> buildFailures = new ArrayList<BuildFailure>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    public BuildPhase getPhase() {
        return phase;
    }

    public void setPhase(BuildPhase phase) {
        this.phase = phase;
    }

    public BuildStatus getStatus() {
        return status;
    }

    public void setStatus(BuildStatus status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getRefSpec() {
        return refSpec;
    }

    public void setRefSpec(String refSpec) {
        this.refSpec = refSpec;
    }

    public BuildVerificationConf getBuildVerificationConf() {
        return buildVerificationConf;
    }

    public void setBuildVerificationConf(BuildVerificationConf buildVerificationConf) {
        this.buildVerificationConf = buildVerificationConf;
    }

    public BuildGroup getBuildGroup() {
        return buildGroup;
    }

    public void setBuildGroup(BuildGroup buildGroup) {
        this.buildGroup = buildGroup;
    }

    public List<Build> getParentBuilds() {
        return parentBuilds;
    }

    public void setParentBuilds(List<Build> parentBuilds) {
        this.parentBuilds = parentBuilds;
    }

    public List<Build> getChildBuilds() {
        return childBuilds;
    }

    public void setChildBuilds(List<Build> childBuilds) {
        this.childBuilds = childBuilds;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDisplayName() {
        return jobDisplayName;
    }

    public void setJobDisplayName(String jobDisplayName) {
        this.jobDisplayName = jobDisplayName;
    }

    public List<BuildEvent> getBuildEvents() {
        return buildEvents;
    }

    public void setBuildEvents(List<BuildEvent> buildEvents) {
        this.buildEvents = buildEvents;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public Boolean getStartNode() {
        return startNode;
    }

    public void setStartNode(Boolean startNode) {
        this.startNode = startNode;
    }

    public List<MemConsumption> getMemConsumptions() {
        return memConsumptions;
    }

    public void setMemConsumptions(List<MemConsumption> memConsumptions) {
        this.memConsumptions = memConsumptions;
    }

    public List<TestCaseStat> getTestCaseStats() {
        return testCaseStats;
    }

    public void setTestCaseStats(List<TestCaseStat> testCaseStats) {
        this.testCaseStats = testCaseStats;
    }

    public List<TestCoverage> getTestCoverages() {
        return testCoverages;
    }

    public void setTestCoverages(List<TestCoverage> testCoverages) {
        this.testCoverages = testCoverages;
    }

    public List<BuildFailure> getBuildFailures() {
        return buildFailures;
    }

    public void setBuildFailures(List<BuildFailure> buildFailures) {
        this.buildFailures = buildFailures;
    }
}
