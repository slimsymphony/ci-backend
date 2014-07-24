/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.exception.UserSessionException;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.SecurityEntity;
import com.nokia.ci.ejb.model.SecurityEntity_;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public abstract class SecurityFunctionality<T extends SecurityEntity> extends CrudFunctionality<T> {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SecurityFunctionality.class);
    @Resource(lookup = "java:jboss/infinispan/cache/ci20/project-access-cache")
    protected Cache<String, SysUser> sysUserCache;

    public SecurityFunctionality() {
    }

    public SecurityFunctionality(Class<T> type) {
        this.type = type;
    }

    /**
     * Read of CRUD with security check.
     */
    public T readSecure(Long id) throws BackendAppException {
        log.debug("Read '{}' from database using id {}",
                type.getCanonicalName(), id);

        T found = em.find(type, id);

        if (found == null) {
            throw new NotFoundException(id, type);
        }

        checkAuth(found);

        return found;
    }

    public Predicate getSecurityPredicate(Root<T> root) {
        // Always false
        Predicate predicate = cb.disjunction();

        SysUser user = getSysUser();
        if (isAdmin(user)) {
            // Always true
            predicate = cb.conjunction();
            return predicate;
        }

        if (user == null) {
            return predicate;
        }

        List<Project> projects = new ArrayList<Project>();
        projects.addAll(user.getProjectAccess());

        if (user.getUserRole() == RoleType.PROJECT_ADMIN) {
            projects.addAll(user.getProjectAdminAccess());
        }

        if (projects.isEmpty()) {
            return predicate;
        }

        List<Long> projectIds = new ArrayList<Long>();
        for (Project p : projects) {
            projectIds.add(p.getId());
        }

        predicate = root.get(SecurityEntity_.projectId).in(projectIds);
        return predicate;
    }

    public SysUser getCallerSysUser() {
        return getSysUser();
    }

    private SysUser getSysUser() {
        String username = getCallerUsername();

        if (username == null || username.equals("anonymous")) {
            return null;
        }

        SysUser user = sysUserCache.get(username);
        return user;
    }

    private boolean isAdmin(SysUser user) {
        if (context.isCallerInRole("SYSTEM_ADMIN")) {
            return true;
        }

        if (user == null) {
            return false;
        }

        if (user.getUserRole() == RoleType.SYSTEM_ADMIN) {
            return true;
        }

        return false;
    }

    protected void checkAuth(T t) throws BackendAppException {
        SysUser sysUser = getSysUser();

        if (isAdmin(sysUser)) {
            return;
        }

        if (sysUser == null || sysUser.getId() == null || sysUser.getProjectAccess() == null
                || sysUser.getProjectAdminAccess() == null) {
            throw new UserSessionException(sysUser);
        }

        Project mockProject = new Project();
        mockProject.setId(t.getProjectId());
        if (sysUser.getProjectAccess().contains(mockProject)) {
            return;
        }

        if (sysUser.getUserRole() == RoleType.PROJECT_ADMIN) {
            if (sysUser.getProjectAdminAccess().contains(mockProject)) {
                return;
            }
        }

        throw new UnauthorizedException("no access to requested resource.");
    }
}
