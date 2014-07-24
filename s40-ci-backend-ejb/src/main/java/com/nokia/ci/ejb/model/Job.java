package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * Entity class of Job.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "JOB")
@Indexed(index = "jobs")
public class Job extends SecurityEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Access(AccessType.PROPERTY) // Enable id reading from proxy object.
    private Long id;
    @Field(name = "jobName")
    private String name;
    private String contactPerson;
    private Boolean disabled = false;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lifespan;
    @ManyToOne
    @IndexedEmbedded
    private Branch branch;
    @Field(name = "jobDisplayName")
    private String displayName;
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobVerificationConf> verificationConfs =
            new ArrayList<JobVerificationConf>();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    @OrderBy("verificationOrdinality ASC")
    private List<JobPostVerification> postVerifications =
            new ArrayList<JobPostVerification>();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    @OrderBy("verificationOrdinality ASC")
    private List<JobPreVerification> preVerifications =
            new ArrayList<JobPreVerification>();
    @ManyToOne
    private SysUser owner;
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobAnnouncement> announcements = new ArrayList<JobAnnouncement>();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobCustomVerification> customVerifications =
            new ArrayList<JobCustomVerification>();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<ReportAction> reportActions = new ArrayList<ReportAction>();
    @Enumerated(EnumType.STRING)
    private JobTriggerType triggerType;
    @Enumerated(EnumType.STRING)
    private JobTriggerScope triggerScope;
    private Integer pollInterval;
    private String cronExpression;
    private String cronTimezone;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastRun;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastCronCheck;
    @Field(name = "jobFetchHead")
    private String lastFetchHead;
    @Field(name = "jobSuccesfulFetchHead")
    private String lastSuccesfullFetchHead;
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    @ContainedIn
    private List<BuildGroup> buildGroups = new ArrayList<BuildGroup>();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<StatusTriggerPattern> statusTriggerPatterns = new ArrayList<StatusTriggerPattern>();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobCustomParameter> customParameters = new ArrayList<JobCustomParameter>();
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<FileTriggerPattern> fileTriggerPatterns = new ArrayList<FileTriggerPattern>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public Date getLifespan() {
        return lifespan;
    }

    public void setLifespan(Date lifespan) {
        this.lifespan = lifespan;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public List<JobPostVerification> getPostVerifications() {
        return postVerifications;
    }

    public void setPostVerifications(List<JobPostVerification> postVerifications) {
        this.postVerifications = postVerifications;
    }

    public List<JobPreVerification> getPreVerifications() {
        return preVerifications;
    }

    public void setPreVerifications(List<JobPreVerification> preVerifications) {
        this.preVerifications = preVerifications;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<JobVerificationConf> getVerificationConfs() {
        return verificationConfs;
    }

    public void setVerificationConfs(List<JobVerificationConf> verificationConfs) {
        this.verificationConfs = verificationConfs;
    }

    public SysUser getOwner() {
        return owner;
    }

    public void setOwner(SysUser owner) {
        this.owner = owner;
    }

    public List<JobAnnouncement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<JobAnnouncement> announcements) {
        this.announcements = announcements;
    }

    public List<JobCustomVerification> getCustomVerifications() {
        return customVerifications;
    }

    public void setCustomVerifications(List<JobCustomVerification> customVerifications) {
        this.customVerifications = customVerifications;
    }

    public List<ReportAction> getReportActions() {
        return reportActions;
    }

    public void setReportActions(List<ReportAction> reportActions) {
        this.reportActions = reportActions;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public JobTriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(JobTriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public JobTriggerScope getTriggerScope() {
        return triggerScope;
    }

    public void setTriggerScope(JobTriggerScope triggerScope) {
        this.triggerScope = triggerScope;
    }

    public Integer getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Integer pollInterval) {
        this.pollInterval = pollInterval;
    }

    public Date getLastRun() {
        return lastRun;
    }

    public void setLastRun(Date lastRun) {
        this.lastRun = lastRun;
    }

    public String getLastFetchHead() {
        return lastFetchHead;
    }

    public void setLastFetchHead(String lastFetchHead) {
        this.lastFetchHead = lastFetchHead;
    }

    public List<BuildGroup> getBuildGroups() {
        return buildGroups;
    }

    public void setBuildGroups(List<BuildGroup> buildGroups) {
        this.buildGroups = buildGroups;
    }

    public String getLastSuccesfullFetchHead() {
        return lastSuccesfullFetchHead;
    }

    public void setLastSuccesfullFetchHead(String lastSuccesfullFetchHead) {
        this.lastSuccesfullFetchHead = lastSuccesfullFetchHead;
    }

    public List<StatusTriggerPattern> getStatusTriggerPatterns() {
        return statusTriggerPatterns;
    }

    public void setStatusTriggerPatterns(List<StatusTriggerPattern> statusTriggerPatterns) {
        this.statusTriggerPatterns = statusTriggerPatterns;
    }
    
    public List<FileTriggerPattern> getFileTriggerPatterns() {
        return fileTriggerPatterns;
    }

    public void setFileTriggerPatterns(List<FileTriggerPattern> fileTriggerPatterns) {
        this.fileTriggerPatterns = fileTriggerPatterns;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public List<JobCustomParameter> getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(List<JobCustomParameter> customParameters) {
        this.customParameters = customParameters;
    }

    public String getCronTimezone() {
        return cronTimezone;
    }

    public void setCronTimezone(String cronTimezone) {
        this.cronTimezone = cronTimezone;
    }

    public Date getLastCronCheck() {
        return lastCronCheck;
    }

    public void setLastCronCheck(Date lastCronCheck) {
        this.lastCronCheck = lastCronCheck;
    }
    
}
