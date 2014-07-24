package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.Widget;
import com.nokia.ci.ui.model.BuildGroupsLazyDataModel;
import com.nokia.ci.ui.model.ChangesLazyDataModel;
import com.nokia.ci.ui.model.ExtendedBuildGroup;
import com.nokia.ci.ui.widget.UserBuildsWidget;
import com.nokia.ci.ui.widget.UserChangesWidget;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DualListModel;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for user.
 *
 * @author vrouvine
 */
@Named
@ViewScoped
public class UserBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(UserBean.class);
    private SysUser user;
    private LazyDataModel<ExtendedBuildGroup> userBuilds;
    private LazyDataModel<Change> userChanges;
    private DualListModel<Project> adminRights = new DualListModel<Project>();
    @Inject
    SysUserEJB userEJB;
    @Inject
    BuildGroupEJB buildGroupEJB;
    @Inject
    private SysConfigEJB sysConfigEJB;
    @Inject
    ProjectEJB projectEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    private String userName;
    private String userImageUuid;

    @Override
    protected void init() throws NotFoundException {
        String userId = getQueryParam("userId");
        String userLogin = getQueryParam("userLogin");
        if (userId != null) {
            log.debug("Finding user {} for editing!", userId);
            user = userEJB.read(Long.parseLong(userId));

            if (StringUtils.isNotEmpty(user.getRealName())) {
                userName = user.getRealName();
            } else {
                userName = user.getLoginName();
            }
        }

        if (userLogin != null) {
            log.debug("Finding user with login name {}", userLogin);
            user = userEJB.getSysUser(userLogin);
        }

        if (httpSessionBean.isAdmin()) {
            initAdministatedProjects();
        }

        if (user != null && user.getUserImage() != null) {
            userImageUuid = user.getUserImage().getFileUuid();
        }
    }

    public String getUserImageUuid() {
        return userImageUuid;
    }

    public SysUser getUser() {
        return user;
    }

    public RoleType[] getRoles() {
        return RoleType.values();
    }

    private void initAdministatedProjects() {
        List<Project> allProjects = projectEJB.readAll();
        List<Project> administrated = userEJB.getProjectAdminAccess(user.getId());

        allProjects.removeAll(administrated);
        adminRights.setSource(allProjects);
        adminRights.setTarget(administrated);
    }

    public void pinBuildsToMyToolbox() {
        try {
            if (user == null) {
                addMessage(FacesMessage.SEVERITY_WARN,
                        "Pin failed", "User could not be found!");
                return;
            }

            if (user.equals(httpSessionBean.getSysUser())) {
                addMessage(FacesMessage.SEVERITY_INFO,
                        "Pin failed", "You cannot pin your own builds, please do this from 'My settings'!");
                return;
            }

            UserBuildsWidget wgt = new UserBuildsWidget(user.getId(), user.getRealName() + " last builds");
            Widget jobWidget = wgt.getPersistentWidget();
            userEJB.addWidget(httpSessionBean.getSysUserId(), jobWidget);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "User builds added", "User last builds is now on your toolbox");
        } catch (NotFoundException ex) {
            log.error("Could not bind user {} builds to toolbox", user);
        }
    }

    public void pinChangesToMyToolbox() {
        try {
            if (user == null) {
                addMessage(FacesMessage.SEVERITY_WARN,
                        "Pin failed", "User could not be found!");
                return;
            }

            if (user.equals(httpSessionBean.getSysUser())) {
                addMessage(FacesMessage.SEVERITY_INFO,
                        "Pin failed", "You cannot pin your own changes, please do this from 'My settings'!");
                return;
            }

            UserChangesWidget wgt = new UserChangesWidget(user.getId(), user.getRealName() + " last changes");
            Widget changeWidget = wgt.getPersistentWidget();
            userEJB.addWidget(httpSessionBean.getSysUserId(), changeWidget);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "User changes added", "User last changes is now on your toolbox");
        } catch (NotFoundException ex) {
            log.error("Could not bind user {} changes to toolbox", user);
        }
    }

    public LazyDataModel<ExtendedBuildGroup> getUserBuilds() {
        if (userBuilds == null) {
            userBuilds = new BuildGroupsLazyDataModel(userEJB, buildGroupEJB, sysConfigEJB, user.getId());
        }
        return userBuilds;
    }

    public LazyDataModel<Change> getUserChanges() {
        if (userChanges == null) {
            userChanges = new ChangesLazyDataModel(userEJB, user.getId());
        }
        return userChanges;
    }

    @RolesAllowed("SYSTEM_ADMIN")
    public String save() {
        try {
            if (user.getUserRole() == RoleType.PROJECT_ADMIN
                    || user.getUserRole() == RoleType.SYSTEM_ADMIN) {
                // User's admin rights should be removed from non-selected projects
                removeAdminAccess(user, adminRights.getSource());
                // User should be granted admin rights to selected projects
                addAdminAccess(user, adminRights.getTarget());
            } else {
                // Not a system or project admin
                // User's admin rights should be removed from all projects
                removeAdminAccess(user, projectEJB.readAll());
            }

            user.setProjectAdminAccess(adminRights.getTarget());
            userEJB.update(user);
            return "users?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save user {}! Cause: {}", user, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "User could not be saved!", "");
        }

        return null;
    }

    private void addAdminAccess(SysUser sysUser, List<Project> projects) throws NotFoundException {
        for (Project p : projects) {
            List<SysUser> admins = projectEJB.getAdmins(p.getId());
            if (!admins.contains(sysUser)) {
                admins.add(sysUser);
                p.setAdminAccess(admins);
                projectEJB.update(p);
            }
        }
    }

    private void removeAdminAccess(SysUser sysUser, List<Project> projects) throws NotFoundException {
        for (Project p : projects) {
            List<SysUser> admins = projectEJB.getAdmins(p.getId());
            if (admins.contains(sysUser)) {
                admins.remove(sysUser);
                p.setAdminAccess(admins);
                projectEJB.update(p);
            }
        }
    }

    @RolesAllowed("SYSTEM_ADMIN")
    public String cancelEdit() {
        return "users?faces-redirect=true";
    }

    public String getUserName() {
        return userName;
    }

    public DualListModel<Project> getAdminRights() {
        return adminRights;
    }

    public void setAdminRights(DualListModel<Project> adminRights) {
        this.adminRights = adminRights;
    }
}
