package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.AuthEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.UserFileEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 *
 */
@Named
@SessionScoped
public class HttpSessionBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(HttpSessionBean.class);
    private static final long DEFAULT_MIN_PROJECT_ACCESS_CACHE_READ_INTERVAL = 2000L;
    @Inject
    AuthEJB authEJB;
    @Inject
    ProjectEJB projectEJB;
    @Inject
    SysUserEJB sysUserEJB;
    @Inject
    UserFileEJB userFileEJB;
    @Inject
    SysConfigEJB sysConfigEJB;
    private String theme;
    private SysUser sysUser;
    private List<Project> projectAccess = null;
    private List<Project> projectAdminAccess = null;
    private long lastProjectAccessCacheCheck = 0L;
    private long lastProjectAdminAccessCacheCheck = 0L;

    @PostConstruct
    void postConstruct() {
        theme = "ci20theme";
        log.debug("HttpSessionBean bean constructed");
    }

    @PreDestroy
    void preDestroy() {
        log.debug("HttpSessionBean bean destroyed");
        try {
            sysUserEJB.cleanCachedUserData(sysUser.getLoginName());
            log.info("Cached data for user {} has been cleaned.", sysUser.getLoginName());
        } catch (Exception e) {
            log.warn("Exception when cleanCachedUserData: {}", e);
        }
    }

    public boolean isLoggedIn() {
        ExternalContext exc = FacesContext.getCurrentInstance().getExternalContext();
        return StringUtils.isNotEmpty(exc.getRemoteUser());
    }

    /**
     * @return the sysUser
     */
    public SysUser getSysUser() {
        return sysUser;
    }

    public boolean isAdmin() {
        if (!isLoggedIn()) {
            return false;
        }
        ExternalContext exc = FacesContext.getCurrentInstance().getExternalContext();
        return exc.isUserInRole(RoleType.SYSTEM_ADMIN.toString());
    }

    public boolean isProjectAdmin() {
        if (!isLoggedIn()) {
            return false;
        }

        if (isAdmin()) {
            return true;
        }

        ExternalContext exc = FacesContext.getCurrentInstance().getExternalContext();
        return exc.isUserInRole(RoleType.PROJECT_ADMIN.toString());
    }

    public boolean isUploadingEnabled() {
        return sysConfigEJB.configExists(SysConfigKey.USER_FILE_UPLOAD_PATH);
    }

    /**
     * @param sysUser the sysUser to set
     */
    public void setSysUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }

    /**
     * Gets logged in system user id. If no user logged in {@code null} is
     * returned.
     *
     * @return system user id
     */
    public Long getSysUserId() {
        if (sysUser == null) {
            return null;
        }
        return sysUser.getId();
    }

    public String getSysUserEmail() {
        if (sysUser == null) {
            return null;
        }
        return sysUser.getEmail();
    }

    public String getSysUserLoginName() {
        if (sysUser == null) {
            return null;
        }
        return sysUser.getLoginName();
    }

    public void initProjectAccess() {
        try {
            if (System.currentTimeMillis() - lastProjectAccessCacheCheck > sysConfigEJB.getValue(
                    SysConfigKey.MIN_PROJECT_ACCESS_CACHE_READ_INTERVAL, DEFAULT_MIN_PROJECT_ACCESS_CACHE_READ_INTERVAL)) {
                projectAccess = sysUserEJB.getCachedProjectAccess(sysUser.getLoginName());
                lastProjectAccessCacheCheck = System.currentTimeMillis();
            }
        } catch (NotFoundException ex) {
            log.warn("Cannot initialize project access for {}! Cause: {}", sysUser, ex.getMessage());
        }
    }

    public void initProjectAdminAccess() {
        try {
            if (System.currentTimeMillis() - lastProjectAdminAccessCacheCheck > sysConfigEJB.getValue(
                    SysConfigKey.MIN_PROJECT_ACCESS_CACHE_READ_INTERVAL, DEFAULT_MIN_PROJECT_ACCESS_CACHE_READ_INTERVAL)) {
                projectAdminAccess = sysUserEJB.getCachedAdminProjectAccess(sysUser.getLoginName());
                lastProjectAdminAccessCacheCheck = System.currentTimeMillis();
            }
        } catch (NotFoundException ex) {
            log.warn("Cannot initialize project admin access for {}! Cause: {}", sysUser, ex.getMessage());
        }
    }

    public boolean hasAdminAccessToProject(Long projectId) {
        if (!isLoggedIn()) {
            return false;
        }

        if (isAdmin()) {
            return true;
        }

        if (!isProjectAdmin()) {
            return false;
        }

        initProjectAdminAccess();

        for (Project p : projectAdminAccess) {
            if (p.getId() != null && p.getId().equals(projectId)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAccessToProject(Long projectId) {
        if (hasAdminAccessToProject(projectId)) {
            return true;
        }

        initProjectAccess();

        for (Project p : projectAccess) {
            if (p.getId() != null && p.getId().equals(projectId)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAccessToArtifactsAndReports(Long projectId) throws NotFoundException {
        if (!hasAccessToProject(projectId)) {
            return false;
        }

        if (sysUser != null && !sysUser.getNextUser()) {
            return true;
        }

        return false;
    }

    public String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
        if (sysUser != null && StringUtils.isNotEmpty(sysUser.getTimezone())) {
            format.setTimeZone(TimeZone.getTimeZone(sysUser.getTimezone()));
        }

        String ret = format.format(new Date());
        ret += " (" + format.getTimeZone().getDisplayName() + ")";

        return ret;
    }

    public String getTheme() {
        if (isLoggedIn()) {
            theme = sysUser.getTheme();
            if (theme == null) {
                theme = "ci20theme";
            }
        }

        return theme;
    }

    public void hideTutorials() throws NotFoundException {
        if (sysUser == null) {
            return;
        }

        sysUser.setShowTutorials(Boolean.FALSE);
        sysUserEJB.update(sysUser);
    }

    public void showTutorials() throws NotFoundException {
        if (sysUser == null) {
            return;
        }

        sysUser.setShowTutorials(Boolean.TRUE);
        sysUserEJB.update(sysUser);
    }

    public String restartTutorial() {
        try {
            showTutorials();
        } catch (NotFoundException ex) {
            log.warn("Could not restart tutorials for user {}", sysUser);
        }
        return "myToolbox?faces-redirect=true";
    }

    @Override
    protected void init() throws Exception {
    }
}
