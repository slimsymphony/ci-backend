/*
 * Build group entity
 */
package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 *
 * @author jajuutin
 */
@Entity
@Table(name = "BUILD_GROUP")
@Indexed(index = "buildGroups")
public class BuildGroup extends SecurityEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Access(AccessType.PROPERTY)
    @Field(name = "bgId")
    @Boost(1.2f)
    private Long id;
    @OneToMany(mappedBy = "buildGroup", cascade = CascadeType.ALL)
    private List<Build> builds = new ArrayList<Build>();
    private String groupUid;
    @OneToOne(mappedBy = "buildGroup", cascade = CascadeType.ALL)
    private BuildGroupCIServer buildGroupCIServer;
    @ManyToMany
    @JoinTable(name = "BUILD_GROUP_CHANGE")
    @IndexedEmbedded(depth = 1)
    private List<Change> changes = new ArrayList<Change>();
    @OneToOne(mappedBy = "buildGroup", cascade = CascadeType.ALL)
    private Release release;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @IndexedEmbedded(depth = 1)
    private Job job;
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    @Field(name = "bgUrl")
    @Column(length = 4000)
    private String url;
    @Enumerated(EnumType.STRING)
    private BuildPhase phase;
    @Enumerated(EnumType.STRING)
    private BuildStatus status;
    private String jobName;
    private String jobDisplayName;
    // Gerrit information
    @Field(analyze = Analyze.NO, name = "bgRefspec")
    @Boost(1.1f)
    private String gerritRefSpec;
    @Field(name = "bgPatchsetRevision")
    private String gerritPatchSetRevision;
    @Field(name = "bgGerritUrl")
    private String gerritUrl;
    @Field(name = "bgGerritBranch")
    private String gerritBranch;
    @Field(name = "bgGerritProject")
    private String gerritProject;
    @OneToMany(mappedBy = "buildGroup", cascade = CascadeType.ALL)
    private List<Component> components = new ArrayList<Component>();
    @Enumerated(EnumType.STRING)
    private BranchType branchType;
    @OneToMany(mappedBy = "buildGroup", cascade = CascadeType.ALL)
    private List<BuildGroupCustomParameter> customParameters = new ArrayList<BuildGroupCustomParameter>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public List<Build> getBuilds() {
        return builds;
    }

    public void setBuilds(List<Build> builds) {
        this.builds = builds;
    }

    public String getGroupUid() {
        return groupUid;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    public BuildGroupCIServer getBuildGroupCIServer() {
        return buildGroupCIServer;
    }

    public void setBuildGroupCIServer(BuildGroupCIServer buildGroupCIServer) {
        this.buildGroupCIServer = buildGroupCIServer;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getGerritRefSpec() {
        return gerritRefSpec;
    }

    public void setGerritRefSpec(String gerritRefSpec) {
        this.gerritRefSpec = gerritRefSpec;
    }

    public String getGerritPatchSetRevision() {
        return gerritPatchSetRevision;
    }

    public void setGerritPatchSetRevision(String gerritPatchSetRevision) {
        this.gerritPatchSetRevision = gerritPatchSetRevision;
    }

    public String getGerritUrl() {
        return gerritUrl;
    }

    public void setGerritUrl(String gerritUrl) {
        this.gerritUrl = gerritUrl;
    }

    public String getGerritBranch() {
        return gerritBranch;
    }

    public void setGerritBranch(String gerritBranch) {
        this.gerritBranch = gerritBranch;
    }

    public String getGerritProject() {
        return gerritProject;
    }

    public void setGerritProject(String gerritProject) {
        this.gerritProject = gerritProject;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public BranchType getBranchType() {
        return branchType;
    }

    public void setBranchType(BranchType branchType) {
        this.branchType = branchType;
    }

    public List<BuildGroupCustomParameter> getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(List<BuildGroupCustomParameter> customParameters) {
        this.customParameters = customParameters;
    }
}
