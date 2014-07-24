/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.event.AuthFailureEvent;
import com.nokia.ci.ejb.event.AuthSuccessEvent;
import com.nokia.ci.ejb.exception.AuthException;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.LoginNotAllowedException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.gerrit.GerritProjectAccess;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.util.LDAPUser;
import com.nokia.ci.ejb.util.LDAPUtil;
import com.nokia.ci.ejb.util.SysUserUtil;
import com.nokia.ci.ejb.util.TimezoneUtil;
import com.unboundid.ldap.sdk.LDAPException;

import java.security.GeneralSecurityException;
import java.util.Date;
import java.io.Serializable;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Miikka Liukkonen
 */
@Stateless
@LocalBean
public class AuthEJB implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(AuthEJB.class);
    @PersistenceContext(unitName = "NokiaCI-PU")
    protected EntityManager em;
    @EJB
    SysUserEJB sysUserEJB;
    @EJB
    SysConfigEJB sysConfigEJB;
    @EJB
    GerritEJB gerritEJB;
    @EJB
    ProjectEJB projectEJB;
    @Inject
    @AuthSuccessEvent
    Event<Long> loginSucceedEvents;
    @Inject
    @AuthFailureEvent
    Event<String> loginFailureEvents;
    @EJB
    GerritProjectAccess gerritProjectAccess;
    transient LDAPUtil ldapUtil = new LDAPUtil();
    @Resource(lookup = "java:jboss/infinispan/cache/ci20/project-access-cache")
    Cache<String, SysUser> cache;

    public SysUser authenticate(String loginName, String password) throws
            InvalidArgumentException, AuthException, LoginNotAllowedException {
        try {
            SysUser sysUser = authenticateInternal(loginName, password);
            loginSucceedEvents.fire(sysUser.getId());
            return sysUser;
        } catch (AuthException e) {
            loginFailureEvents.fire(loginName);
            throw e;
        }
    }

    private SysUser authenticateInternal(String loginName, String password) throws
            InvalidArgumentException, AuthException, LoginNotAllowedException {

        if (StringUtils.isEmpty(loginName) || StringUtils.isEmpty(password)) {
            throw new InvalidArgumentException("Invalid authentication arguments");
        }

        log.info("Authenticating user {}", loginName);
        LDAPUser ldapUser;
        try {
            ldapUser = ldapUtil.authenticate(loginName, password);
        } catch (LDAPException ex) {
            log.warn("Authentication failed for user {}. Cause: {}", loginName, ex.getMessage());
            throw new AuthException(ex);
        } catch (GeneralSecurityException ex) {
            log.warn("Authentication failed for user {}. Cause: {}", loginName, ex.getMessage());
            throw new AuthException(ex);
        }

        if (ldapUser == null) {
            throw new AuthException("Can not find user " + loginName + " from LDAP");
        }

        if (!sysConfigEJB.isNextUsersAllowed() && LDAPUtil.isNextUser(loginName)) {
            throw new AuthException("NOE Account Required.");
        }

        SysUser sysUser;
        try {
            sysUser = sysUserEJB.getSysUser(loginName);
            sysUser.setEmail(ldapUser.getEmail());
            sysUser.setRealName(ldapUser.getRealname());
            sysUser.setNextUser(LDAPUtil.isNextUser(loginName));
            if (sysUser.getTimezone() == null) {
                sysUser.setTimezone(TimezoneUtil.getTimezoneByNokiaSite(ldapUser.getNokiaSite()));
            }
            if (StringUtils.isEmpty(sysUser.getSecretKey())) {
                sysUser.setSecretKey(SysUserUtil.generateSecretKey());
            }
            // First time login, show tutorials
            if (sysUser.getLastLogin() == null || sysUser.getShowTutorials() == null) {
                sysUser.setShowTutorials(Boolean.TRUE);
            }

            sysUser.setLastLogin(new Date());
            sysUser = sysUserEJB.update(sysUser);
        } catch (NotFoundException ex) {
            log.info("User {} does not exist! Cause: {}, creating new one.", loginName, ex.getMessage());
            sysUser = sysUserEJB.createUser(ldapUser);
        }

        if (sysUser.getUserRole() != RoleType.SYSTEM_ADMIN && !sysConfigEJB.isLoginAllowed()) {
            throw new LoginNotAllowedException("System login currently not allowed.");
        }

        // Need to flush as we are detaching the entity before adding to cache
        em.flush();

        // Add detached entity to cache.
        em.detach(sysUser);
        sysUser.setProjectAccess(sysUserEJB.getProjectAccess(sysUser.getId()));
        sysUser.setProjectAdminAccess(sysUserEJB.getProjectAdminAccess(sysUser.getId()));
        cache.put(sysUser.getLoginName(), sysUser);

        // Return detached entity.
        return sysUser;
    }
}
