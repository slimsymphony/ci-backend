/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

/**
 *
 * @author jajuutin
 */
@Entity
@Table(name = "SYS_USER")
@Indexed(index = "users")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    @Field(name = "userLoginName")
    private String loginName;
    @Field(name = "userRealName")
    private String realName;
    @Column(unique = true)
    @Field(name = "userEmail")
    private String email;
    private String timezone;
    private Boolean sendEmail;
    private Boolean nextUser = true;
    private String defaultPage;
    @Column(unique = true)
    private String secretKey;
    private String theme;
    @Enumerated(value = EnumType.STRING)
    private RoleType userRole;
    @OneToMany(mappedBy = "owner")
    private List<Job> jobs = new ArrayList<Job>();
    @OneToMany(mappedBy = "sysUser", cascade = CascadeType.ALL)
    private List<Widget> widgets = new ArrayList<Widget>();
    private Boolean systemMetricsAllowed = false;
    private Boolean buildClassificationAllowed = false;
    private Long failedLogins;
    @Temporal(TemporalType.TIMESTAMP)
    private Date firstFailedLogin;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;
    @ManyToMany(mappedBy = "userAccess")
    private List<Project> projectAccess = new ArrayList<Project>();
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<UserFile> userFiles = new ArrayList<UserFile>();
    @ManyToMany(mappedBy = "adminAccess")
    private List<Project> projectAdminAccess = new ArrayList<Project>();
    @OneToOne(cascade = CascadeType.ALL)
    private SysUserImage userImage;
    private Boolean showTutorials = true;

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the loginName
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * @param loginName the loginName to set
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public RoleType getUserRole() {
        return userRole;
    }

    public void setUserRole(RoleType userRole) {
        this.userRole = userRole;
    }

    /**
     * @return the jobs
     */
    public List<Job> getJobs() {
        return jobs;
    }

    /**
     * @param jobs the jobs to set
     */
    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getSystemMetricsAllowed() {
        return systemMetricsAllowed;
    }

    public void setSystemMetricsAllowed(Boolean systemMetricsAllowed) {
        this.systemMetricsAllowed = systemMetricsAllowed;
    }

    public Boolean getBuildClassificationAllowed() {
        return buildClassificationAllowed;
    }

    public void setBuildClassificationAllowed(Boolean buildClassificationAllowed) {
        this.buildClassificationAllowed = buildClassificationAllowed;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public Boolean getNextUser() {
        return nextUser;
    }

    public void setNextUser(Boolean nextUser) {
        this.nextUser = nextUser;
    }

    public String getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Long getFailedLogins() {
        return failedLogins;
    }

    public void setFailedLogins(Long failedLogins) {
        this.failedLogins = failedLogins;
    }

    public Date getFirstFailedLogin() {
        return firstFailedLogin;
    }

    public void setFirstFailedLogin(Date firstFailedLogin) {
        this.firstFailedLogin = firstFailedLogin;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public List<Project> getProjectAccess() {
        return projectAccess;
    }

    public void setProjectAccess(List<Project> projectAccess) {
        this.projectAccess = projectAccess;
    }

    public List<Widget> getWidgets() {
        return widgets;
    }

    public void setWidgets(List<Widget> widgets) {
        this.widgets = widgets;
    }

    public List<UserFile> getUserFiles() {
        return userFiles;
    }

    public void setUserFiles(List<UserFile> userFiles) {
        this.userFiles = userFiles;
    }

    public List<Project> getProjectAdminAccess() {
        return projectAdminAccess;
    }

    public void setProjectAdminAccess(List<Project> projectAdminAccess) {
        this.projectAdminAccess = projectAdminAccess;
    }

    public SysUserImage getUserImage() {
        return userImage;
    }

    public void setUserImage(SysUserImage userImage) {
        this.userImage = userImage;
    }

    public Boolean getShowTutorials() {
        return showTutorials;
    }

    public void setShowTutorials(Boolean showTutorials) {
        this.showTutorials = showTutorials;
    }
}
