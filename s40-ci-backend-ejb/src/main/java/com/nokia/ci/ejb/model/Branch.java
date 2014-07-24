package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;

/**
 * Entity class of Branch.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "BRANCH")
public class Branch extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Field
    private String name;
    @Field
    private String displayName;
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL)
    private List<BranchVerificationConf> verificationConfs = new ArrayList<BranchVerificationConf>();
    @OneToMany(mappedBy = "branch")
    @ContainedIn
    private List<Job> jobs = new ArrayList<Job>();
    @ManyToOne
    @ContainedIn
    private Project project;
    @Enumerated(EnumType.STRING)
    private BranchType type;
    @ManyToMany
    @JoinTable(name = "BRANCH_CISERVER")
    private List<CIServer> ciServers = new ArrayList<CIServer>();
    private String gitRepositoryPath;
    @Enumerated(EnumType.STRING)
    private GitRepositoryStatus gitRepositoryStatus;
    private Integer gitFailureCounter;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastGitOperationStarted;
    @ManyToOne(optional = true)
    private Template template;

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public BranchType getType() {
        return type;
    }

    public void setType(BranchType type) {
        this.type = type;
    }

    public List<CIServer> getCiServers() {
        return ciServers;
    }

    public void setCiServers(List<CIServer> ciServers) {
        this.ciServers = ciServers;
    }

    public List<BranchVerificationConf> getVerificationConfs() {
        return verificationConfs;
    }

    public void setVerificationConfs(List<BranchVerificationConf> verificationConfs) {
        this.verificationConfs = verificationConfs;
    }

    public String getGitRepositoryPath() {
        return gitRepositoryPath;
    }

    public void setGitRepositoryPath(String gitRepositoryPath) {
        this.gitRepositoryPath = gitRepositoryPath;
    }

    public GitRepositoryStatus getGitRepositoryStatus() {
        return gitRepositoryStatus;
    }

    public void setGitRepositoryStatus(GitRepositoryStatus gitRepositoryStatus) {
        this.gitRepositoryStatus = gitRepositoryStatus;
    }

    public Integer getGitFailureCounter() {
        return gitFailureCounter;
    }

    public void setGitFailureCounter(Integer gitFailureCounter) {
        this.gitFailureCounter = gitFailureCounter;
    }

    public Date getLastGitOperationStarted() {
        return lastGitOperationStarted;
    }

    public void setLastGitOperationStarted(Date lastGitOperationStarted) {
        this.lastGitOperationStarted = lastGitOperationStarted;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }
}
