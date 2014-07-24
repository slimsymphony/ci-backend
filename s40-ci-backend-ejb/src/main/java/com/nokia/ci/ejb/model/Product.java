package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class for Product.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "PRODUCT")
public class Product extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String displayName;
    private String rmCode;
    @ManyToMany(mappedBy = "products")
    private List<Project> projects = new ArrayList<Project>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    private List<ProjectVerificationConf> projectVerificationConfs = new ArrayList<ProjectVerificationConf>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    private List<BranchVerificationConf> branchVerificationConfs = new ArrayList<BranchVerificationConf>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    private List<JobVerificationConf> jobVerificationConfs = new ArrayList<JobVerificationConf>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    private List<CustomVerificationConf> customVerificationConfs = new ArrayList<CustomVerificationConf>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    private List<TemplateVerificationConf> templateVerificationConfs = new ArrayList<TemplateVerificationConf>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    private List<TemplateCustomVerificationConf> templateCustomVerificationConfs = new ArrayList<TemplateCustomVerificationConf>();
    private String uuid;

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

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<ProjectVerificationConf> getProjectVerificationConfs() {
        return projectVerificationConfs;
    }

    public void setProjectVerificationConfs(List<ProjectVerificationConf> projectVerificationConfs) {
        this.projectVerificationConfs = projectVerificationConfs;
    }

    public List<BranchVerificationConf> getBranchVerificationConfs() {
        return branchVerificationConfs;
    }

    public void setBranchVerificationConfs(List<BranchVerificationConf> branchVerificationConfs) {
        this.branchVerificationConfs = branchVerificationConfs;
    }

    public List<JobVerificationConf> getJobVerificationConfs() {
        return jobVerificationConfs;
    }

    public void setJobVerificationConfs(List<JobVerificationConf> jobVerificationConfs) {
        this.jobVerificationConfs = jobVerificationConfs;
    }

    public List<CustomVerificationConf> getCustomVerificationConfs() {
        return customVerificationConfs;
    }

    public void setCustomVerificationConfs(List<CustomVerificationConf> customVerificationConfs) {
        this.customVerificationConfs = customVerificationConfs;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<TemplateCustomVerificationConf> getTemplateCustomVerificationConfs() {
        return templateCustomVerificationConfs;
    }

    public void setTemplateCustomVerificationConfs(List<TemplateCustomVerificationConf> templateCustomVerificationConfs) {
        this.templateCustomVerificationConfs = templateCustomVerificationConfs;
    }

    public List<TemplateVerificationConf> getTemplateVerificationConfs() {
        return templateVerificationConfs;
    }

    public void setTemplateVerificationConfs(List<TemplateVerificationConf> templateVerificationConfs) {
        this.templateVerificationConfs = templateVerificationConfs;
    }

    public String getRmCode() {
        return rmCode;
    }

    public void setRmCode(String rmCode) {
        this.rmCode = rmCode;
    }
    
}
