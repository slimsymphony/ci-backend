package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Cascade;

/**
 * Entity class for Verification.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "VERIFICATION")
public class Verification extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String displayName;
    @Lob
    private String description;
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "VERIFICATION_TEST_RESULT_TYPES", joinColumns =
            @JoinColumn(name = "VERIFICATION_ID", referencedColumnName = "ID"))
    @Enumerated(EnumType.STRING)
    private Set<TestResultType> testResultTypes = new HashSet();
    private String testResultIndexFile;
    @ManyToMany(mappedBy = "verifications")
    private List<Project> projects = new ArrayList<Project>();
    @ManyToMany
    @JoinTable(name = "VERIFICATION_VERIFICATION")
    private List<Verification> parentVerifications = new ArrayList<Verification>();
    @ManyToMany
    @JoinTable(name = "VERIFICATION_SLAVE_LABEL")
    private List<SlaveLabel> slaveLabels = new ArrayList<SlaveLabel>();
    @ManyToMany(mappedBy = "parentVerifications")
    private List<Verification> childVerifications = new ArrayList<Verification>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL)
    private List<CustomParam> customParams = new ArrayList<CustomParam>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL)
    private List<InputParam> inputParams = new ArrayList<InputParam>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL)
    private List<ResultDetailsParam> resultDetailsParams = new ArrayList<ResultDetailsParam>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.REMOVE)
    private List<BranchVerificationConf> branchVerificationConfs = new ArrayList<BranchVerificationConf>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.REMOVE)
    private List<JobCustomVerification> jobCustomVerifications = new ArrayList<JobCustomVerification>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.REMOVE)
    private List<JobVerificationConf> jobVerificationConfs = new ArrayList<JobVerificationConf>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.REMOVE)
    private List<ProjectVerificationConf> projectVerificationConfs = new ArrayList<ProjectVerificationConf>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.REMOVE)
    private List<JobPreVerification> jobPreVerifications = new ArrayList<JobPreVerification>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.REMOVE)
    private List<JobPostVerification> jobPostVerifications = new ArrayList<JobPostVerification>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.REMOVE)
    private List<TemplateVerificationConf> templateVerificationConfs = new ArrayList<TemplateVerificationConf>();
    @OneToMany(mappedBy = "verification", cascade = CascadeType.REMOVE)
    private List<TemplateCustomVerification> templateCustomVerifications = new ArrayList<TemplateCustomVerification>();
    @Enumerated(EnumType.STRING)
    private VerificationType type = VerificationType.NORMAL;
    @Enumerated(EnumType.STRING)
    private VerificationTargetPlatform targetPlatform;
    @Enumerated(EnumType.STRING)
    private BuildStatus parentStatusThreshold = BuildStatus.NOT_BUILT;
    private String uuid;
    @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL)
    private List<VerificationFailureReason> failureReasons = new ArrayList<VerificationFailureReason>();    

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

    public List<SlaveLabel> getSlaveLabels() {
        return slaveLabels;
    }

    public void setSlaveLabels(List<SlaveLabel> slaveLabels) {
        this.slaveLabels = slaveLabels;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<TestResultType> getTestResultTypes() {
        return testResultTypes;
    }

    public void setTestResultTypes(Set<TestResultType> testResultTypes) {
        this.testResultTypes = testResultTypes;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Verification> getParentVerifications() {
        return parentVerifications;
    }

    public void setParentVerifications(List<Verification> parentVerifications) {
        this.parentVerifications = parentVerifications;
    }

    public List<Verification> getChildVerifications() {
        return childVerifications;
    }

    public void setChildVerifications(List<Verification> childVerifications) {
        this.childVerifications = childVerifications;
    }

    public List<CustomParam> getCustomParams() {
        return customParams;
    }

    public void setCustomParams(List<CustomParam> customParams) {
        this.customParams = customParams;
    }

    public List<InputParam> getInputParams() {
        return inputParams;
    }

    public void setInputParams(List<InputParam> inputParams) {
        this.inputParams = inputParams;
    }

    public List<ResultDetailsParam> getResultDetailsParams() {
        return resultDetailsParams;
    }

    public void setResultDetailsParams(List<ResultDetailsParam> resultDetailsParams) {
        this.resultDetailsParams = resultDetailsParams;
    }

    public BuildStatus getParentStatusThreshold() {
        return parentStatusThreshold;
    }

    public void setParentStatusThreshold(BuildStatus parentStatusThreshold) {
        this.parentStatusThreshold = parentStatusThreshold;
    }

    public VerificationType getType() {
        return type;
    }

    public void setType(VerificationType type) {
        this.type = type;
    }

    public List<BranchVerificationConf> getBranchVerificationConfs() {
        return branchVerificationConfs;
    }

    public void setBranchVerificationConfs(List<BranchVerificationConf> branchVerificationConfs) {
        this.branchVerificationConfs = branchVerificationConfs;
    }

    public List<JobCustomVerification> getJobCustomVerifications() {
        return jobCustomVerifications;
    }

    public void setJobCustomVerifications(List<JobCustomVerification> jobCustomVerifications) {
        this.jobCustomVerifications = jobCustomVerifications;
    }

    public List<JobVerificationConf> getJobVerificationConfs() {
        return jobVerificationConfs;
    }

    public void setJobVerificationConfs(List<JobVerificationConf> jobVerificationConfs) {
        this.jobVerificationConfs = jobVerificationConfs;
    }

    public List<ProjectVerificationConf> getProjectVerificationConfs() {
        return projectVerificationConfs;
    }

    public void setProjectVerificationConfs(List<ProjectVerificationConf> projectVerificationConfs) {
        this.projectVerificationConfs = projectVerificationConfs;
    }

    public String getTestResultIndexFile() {
        return testResultIndexFile;
    }

    public void setTestResultIndexFile(String testResultIndexFile) {
        this.testResultIndexFile = testResultIndexFile;
    }

    public List<JobPreVerification> getJobPreVerifications() {
        return jobPreVerifications;
    }

    public void setJobPreVerifications(List<JobPreVerification> jobPreVerifications) {
        this.jobPreVerifications = jobPreVerifications;
    }

    public List<JobPostVerification> getJobPostVerifications() {
        return jobPostVerifications;
    }

    public void setJobPostVerifications(List<JobPostVerification> jobPostVerifications) {
        this.jobPostVerifications = jobPostVerifications;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<TemplateVerificationConf> getTemplateVerificationConfs() {
        return templateVerificationConfs;
    }

    public void setTemplateVerificationConfs(List<TemplateVerificationConf> templateVerificationConfs) {
        this.templateVerificationConfs = templateVerificationConfs;
    }

    public List<TemplateCustomVerification> getTemplateCustomVerifications() {
        return templateCustomVerifications;
    }

    public void setTemplateCustomVerifications(List<TemplateCustomVerification> templateCustomVerifications) {
        this.templateCustomVerifications = templateCustomVerifications;
    }

    public List<VerificationFailureReason> getFailureReasons() {
        return failureReasons;
    }

    public void setFailureReasons(List<VerificationFailureReason> failureReasons) {
        this.failureReasons = failureReasons;
    }

    public VerificationTargetPlatform getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(VerificationTargetPlatform targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

}
