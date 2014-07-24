/*
 * Build verification conf entity.
 */
package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cascade;

/**
 *
 * @author jajuutin
 */
@Entity
@Table(name = "BUILD_VERIFICATION_CONF")
public class BuildVerificationConf extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Access(AccessType.PROPERTY) // Enable id reading from proxy object.
    private Long id;
    private String verificationName;
    private String verificationDisplayName;
    @Enumerated(EnumType.STRING)
    private VerificationTargetPlatform verificationTargetPlatform;
    @ElementCollection
    @CollectionTable(name = "BUILD_VERIFICATION_CONF_LABELS")
    private Set<String> labelNames = new HashSet();
    @Enumerated(EnumType.STRING)
    private VerificationType verificationType = VerificationType.NORMAL;
    private String productName;
    private String productDisplayName;
    private String productRmCode;
    private String imeiCode;
    @Column(length = 1024)
    private String tasUrl;
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "BVC_TEST_RESULT_TYPES", joinColumns =
            @JoinColumn(name = "BVC_ID", referencedColumnName = "ID"))
    @Enumerated(EnumType.STRING)
    private Set<TestResultType> testResultTypes = new HashSet();
    private String testResultIndexFile;
    @OneToMany(mappedBy = "buildVerificationConf", cascade = CascadeType.ALL)
    private List<BuildCustomParameter> customParameters = new ArrayList<BuildCustomParameter>();
    @OneToMany(mappedBy = "buildVerificationConf", cascade = CascadeType.ALL)
    private List<BuildInputParam> buildInputParams = new ArrayList<BuildInputParam>();
    @OneToMany(mappedBy = "buildVerificationConf", cascade = CascadeType.ALL)
    private List<BuildResultDetailsParam> buildResultDetailsParams = new ArrayList<BuildResultDetailsParam>();
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "BUILD_ID")
    private Build build;
    @Enumerated(EnumType.STRING)
    private BuildStatus parentStatusThreshold;
    @Enumerated(EnumType.STRING)
    private VerificationCardinality cardinality;
    private String verificationUuid;
    private String productUuid;
    @Column(length=1024)
    private String userFiles;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getVerificationName() {
        return verificationName;
    }

    public Set<String> getLabelNames() {
        return labelNames;
    }

    public void setLabelNames(Set<String> labelNames) {
        this.labelNames = labelNames;
    }

    public void setVerificationName(String verificationName) {
        this.verificationName = verificationName;
    }

    public String getVerificationDisplayName() {
        return verificationDisplayName;
    }

    public void setVerificationDisplayName(String verificationDisplayName) {
        this.verificationDisplayName = verificationDisplayName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDisplayName() {
        return productDisplayName;
    }

    public void setProductDisplayName(String productDisplayName) {
        this.productDisplayName = productDisplayName;
    }

    public List<BuildCustomParameter> getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(List<BuildCustomParameter> customParameters) {
        this.customParameters = customParameters;
    }

    public List<BuildInputParam> getBuildInputParams() {
        return buildInputParams;
    }

    public void setBuildInputParams(List<BuildInputParam> buildInputParams) {
        this.buildInputParams = buildInputParams;
    }

    public List<BuildResultDetailsParam> getBuildResultDetailsParams() {
        return buildResultDetailsParams;
    }

    public void setBuildResultDetailsParams(List<BuildResultDetailsParam> buildResultDetailsParams) {
        this.buildResultDetailsParams = buildResultDetailsParams;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public BuildStatus getParentStatusThreshold() {
        return parentStatusThreshold;
    }

    public void setParentStatusThreshold(BuildStatus parentStatusThreshold) {
        this.parentStatusThreshold = parentStatusThreshold;
    }

    public VerificationCardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(VerificationCardinality cardinality) {
        this.cardinality = cardinality;
    }

    public Set<TestResultType> getTestResultTypes() {
        return testResultTypes;
    }

    public void setTestResultTypes(Set<TestResultType> testResultTypes) {
        this.testResultTypes = testResultTypes;
    }

    public String getTestResultIndexFile() {
        return testResultIndexFile;
    }

    public void setTestResultIndexFile(String testResultIndexFile) {
        this.testResultIndexFile = testResultIndexFile;
    }

    public VerificationType getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(VerificationType verificationType) {
        this.verificationType = verificationType;
    }

    public String getProductUuid() {
        return productUuid;
    }

    public void setProductUuid(String productUuid) {
        this.productUuid = productUuid;
    }

    public String getVerificationUuid() {
        return verificationUuid;
    }

    public void setVerificationUuid(String verificationUuid) {
        this.verificationUuid = verificationUuid;
    }

    public Boolean sameType(BuildVerificationConf conf) {
        if (productName.equals(conf.getProductName())
                && verificationName.equals(conf.getVerificationName())
                && testResultTypes.containsAll(conf.getTestResultTypes())
                && verificationType.equals(conf.getVerificationType())) {
            return true;
        }

        return false;
    }

    public String getProductRmCode() {
        return productRmCode;
    }

    public void setProductRmCode(String productRmCode) {
        this.productRmCode = productRmCode;
    }

    public VerificationTargetPlatform getVerificationTargetPlatform() {
        return verificationTargetPlatform;
    }

    public void setVerificationTargetPlatform(VerificationTargetPlatform verificationTargetPlatform) {
        this.verificationTargetPlatform = verificationTargetPlatform;
    }

    public String getImeiCode() {
        return imeiCode;
    }

    public void setImeiCode(String imeiCode) {
        this.imeiCode = imeiCode;
    }

    public String getTasUrl() {
        return tasUrl;
    }

    public void setTasUrl(String tasUrl) {
        this.tasUrl = tasUrl;
    }
    
    public String getUserFiles() {
        return userFiles;
    }

    public void setUserFiles(String userFiles) {
        this.userFiles = userFiles;
    }

}
