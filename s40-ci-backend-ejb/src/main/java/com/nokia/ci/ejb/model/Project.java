package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * Entity class for Project.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "PROJECT")
@Indexed(index = "projects")
public class Project extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Field(name = "projectId")
    private Long id;
    @Field(name = "projectName")
    private String name;
    @Field(name = "projectDisplayName")
    private String displayName;
    private String configurationResponsible;
    private String testingResponsible;
    private String contactList;
    @ManyToMany
    @JoinTable(name = "PROJECT_PRODUCT")
    private List<Product> products = new ArrayList<Product>();
    @ManyToMany
    @JoinTable(name = "PROJECT_VERIFICATION")
    private List<Verification> verifications = new ArrayList<Verification>();
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ProjectVerificationConf> verificationConfs = new ArrayList<ProjectVerificationConf>();
    @OneToMany(mappedBy = "project", cascade = {CascadeType.MERGE,
        CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH})
    @IndexedEmbedded
    private List<Branch> branches = new ArrayList<Branch>();
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ProjectAnnouncement> announcements = new ArrayList<ProjectAnnouncement>();
    @ManyToOne
    private ProjectGroup projectGroup;
    @Lob
    @Field(name = "projectDescription")
    @Analyzer(impl = ClassicAnalyzer.class)
    private String description;
    @ManyToOne
    private Gerrit gerrit;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ProjectExternalLink> links = new ArrayList<ProjectExternalLink>();
    @Field(name = "projectGerritProject")
    private String gerritProject;
    @ManyToMany
    @JoinTable(name = "PROJECT_USER_ACCESS")
    private List<SysUser> userAccess = new ArrayList<SysUser>();
    @ManyToMany
    @JoinTable(name = "PROJECT_ADMIN_ACCESS")
    private List<SysUser> adminAccess = new ArrayList<SysUser>();
    @ManyToMany
    @JoinTable(name = "PROJECT_CHANGE_TRACKER")
    private List<ChangeTracker> changeTrackers = new ArrayList<ChangeTracker>();

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

    public String getConfigurationResponsible() {
        return configurationResponsible;
    }

    public void setConfigurationResponsible(String configurationResponsible) {
        this.configurationResponsible = configurationResponsible;
    }

    public String getTestingResponsible() {
        return testingResponsible;
    }

    public void setTestingResponsible(String testingResponsible) {
        this.testingResponsible = testingResponsible;
    }

    public String getContactList() {
        return contactList;
    }

    public void setContactList(String contactList) {
        this.contactList = contactList;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Verification> getVerifications() {
        return verifications;
    }

    public void setVerifications(List<Verification> verifications) {
        this.verifications = verifications;
    }

    public List<ProjectVerificationConf> getVerificationConfs() {
        return verificationConfs;
    }

    public void setVerificationConfs(List<ProjectVerificationConf> verificationConfs) {
        this.verificationConfs = verificationConfs;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Gerrit getGerrit() {
        return gerrit;
    }

    public void setGerrit(Gerrit gerrit) {
        this.gerrit = gerrit;
    }

    public List<ProjectAnnouncement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<ProjectAnnouncement> announcements) {
        this.announcements = announcements;
    }

    public List<ProjectExternalLink> getLinks() {
        return links;
    }

    public void setLinks(List<ProjectExternalLink> links) {
        this.links = links;

    }

    public ProjectGroup getProjectGroup() {
        return projectGroup;
    }

    public void setProjectGroup(ProjectGroup projectGroup) {
        this.projectGroup = projectGroup;

    }

    public String getGerritProject() {
        return gerritProject;
    }

    public void setGerritProject(String gerritProject) {
        this.gerritProject = gerritProject;
    }

    public List<ChangeTracker> getChangeTrackers() {
        return changeTrackers;
    }

    public void setChangeTrackers(List<ChangeTracker> changeTrackers) {
        this.changeTrackers = changeTrackers;
    }

    public List<SysUser> getUserAccess() {
        return userAccess;
    }

    public void setUserAccess(List<SysUser> userAccess) {
        this.userAccess = userAccess;
    }

    public List<SysUser> getAdminAccess() {
        return adminAccess;
    }

    public void setAdminAccess(List<SysUser> adminAccess) {
        this.adminAccess = adminAccess;
    }
}
