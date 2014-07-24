/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.Project_;
import com.nokia.ci.ejb.model.SysUser_;
import com.nokia.ci.ejb.model.BuildGroup_;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Change_;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysUserImage;
import com.nokia.ci.ejb.model.UserFile;
import com.nokia.ci.ejb.model.Widget;
import com.nokia.ci.ejb.model.Widget_;
import com.nokia.ci.ejb.util.LDAPUser;
import com.nokia.ci.ejb.util.LDAPUtil;
import com.nokia.ci.ejb.util.Order;
import com.nokia.ci.ejb.util.RelationUtil;
import com.nokia.ci.ejb.util.SysUserUtil;
import com.nokia.ci.ejb.util.TimezoneUtil;
import com.unboundid.ldap.sdk.LDAPException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.StringUtils;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class SysUserEJB extends CrudFunctionality<SysUser> implements Serializable, BuildGroupLoader {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SysUserEJB.class);
    @Inject
    ChangeEJB changeEJB;
    @Inject
    BuildGroupEJB buildGroupEJB;
    @Inject
    ProjectEJB projectEJB;
    @Inject
    WidgetEJB widgetEJB;
    @Resource(lookup = "java:jboss/infinispan/cache/ci20/session-cache")
    Cache<String, SysUser> cache;

    public SysUserEJB() {
        super(SysUser.class);
    }

    public SysUser getSysUser(String loginName) throws NotFoundException {
        SysUser user = null;
        log.debug("Finding SysUser where loginName={}", loginName);
        CriteriaQuery<SysUser> criteria = cb.createQuery(SysUser.class);
        Root<SysUser> sysUser = criteria.from(SysUser.class);
        criteria.where(cb.equal(sysUser.get(SysUser_.loginName), loginName));
        TypedQuery<SysUser> emQuery = em.createQuery(criteria);

        try {
            user = emQuery.getSingleResult();
        } catch (NoResultException e) {
            log.debug("Could not find user with login name {}", loginName);
            throw new NotFoundException("SysUser not found with loginName: " + loginName);
        }

        return user;
    }

    public SysUser getSysUserWithSecretKey(String key) throws NotFoundException {
        SysUser user = null;
        log.debug("Finding SysUser where secretkey={}", key);
        CriteriaQuery<SysUser> criteria = cb.createQuery(SysUser.class);
        Root<SysUser> sysUser = criteria.from(SysUser.class);
        criteria.where(cb.equal(sysUser.get(SysUser_.secretKey), key));
        TypedQuery<SysUser> emQuery = em.createQuery(criteria);

        try {
            user = emQuery.getSingleResult();
        } catch (NoResultException e) {
            log.debug("Could not find user with secret key {}", key);
            throw new NotFoundException("SysUser not found with secret key");
        }

        return user;
    }

    public List<SysUser> getSysAdmins() {
        CriteriaQuery<SysUser> criteria = cb.createQuery(SysUser.class);
        Root<SysUser> sysUser = criteria.from(SysUser.class);
        criteria.where(cb.equal(sysUser.get(SysUser_.userRole), RoleType.SYSTEM_ADMIN));
        TypedQuery<SysUser> emQuery = em.createQuery(criteria);
        return emQuery.getResultList();
    }

    public List<SysUser> getSysUsers(int first, int pageSize, String orderField, Order order, Map<String, String> filters) {
        if (first < 0 || pageSize < 1) {
            return null;
        }

        if (order == null) {
            order = Order.ASC;
        }

        if (StringUtils.isEmpty(orderField)) {
            orderField = "loginName";
        }

        if (log.isDebugEnabled()) {
            log.debug("Finding sysUsers where first={}, pageSize={}, orderField={}, order={}",
                    new Object[]{first, pageSize, orderField, order.getSql()});
        }

        CriteriaQuery<SysUser> criteria = cb.createQuery(SysUser.class);
        Root<SysUser> user = criteria.from(SysUser.class);
        criteria.select(user);

        if (!filters.isEmpty()) {
            Iterator it = filters.entrySet().iterator();
            List<Predicate> predicates = new ArrayList<Predicate>();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();

                Path<String> p = user.get(pairs.getKey());
                if (p != null) {
                    Predicate pred = cb.like(cb.upper(p), "%" + pairs.getValue().toUpperCase() + "%");
                    predicates.add(pred);
                }

                it.remove();
            }

            criteria.where(predicates.toArray(new Predicate[]{}));
        }

        if (order == Order.ASC) {
            criteria.orderBy(cb.asc(user.get(orderField)));
        } else {
            criteria.orderBy(cb.desc(user.get(orderField)));
        }
        TypedQuery<SysUser> emQuery = em.createQuery(criteria);
        emQuery.setFirstResult(first);
        emQuery.setMaxResults(pageSize);

        List<SysUser> resultList = emQuery.getResultList();
        log.debug("Found {} users", resultList.size());
        return resultList;
    }

    public Boolean hasAccessToProject(Long id, Long projectId) throws NotFoundException {

        CriteriaQuery<SysUser> criteria = cb.createQuery(SysUser.class);
        Root<SysUser> user = criteria.from(SysUser.class);
        criteria.select(user);
        Join accessJoin = user.join(SysUser_.projectAccess);

        Predicate userPredicate = cb.equal(user.get(SysUser_.id), id);
        Predicate projectPredicate = cb.equal(accessJoin.get(Project_.id), projectId);
        Predicate predicate = cb.and(userPredicate, projectPredicate);
        criteria.where(predicate);

        TypedQuery<SysUser> emQuery = em.createQuery(criteria);

        try {
            SysUser sysUser = emQuery.getSingleResult();
        } catch (NoResultException e) {
            return false;
        }

        return true;
    }

    public List<Job> getOwnedJobs(Long id) {
        return getJoinList(id, SysUser_.jobs);
    }

    public List<Widget> getWidgets(Long id) {
        return getJoinList(id, SysUser_.widgets);
    }

    public List<Widget> getWidgetsInOrder(Long userId) {
        CriteriaQuery<Widget> query = cb.createQuery(Widget.class);
        Root<Widget> wgt = query.from(Widget.class);
        query.select(wgt);

        wgt.fetch(Widget_.settings, JoinType.LEFT);
        Predicate predicate = cb.and(cb.equal(wgt.get(Widget_.sysUser), userId));
        query.where(predicate).distinct(true);

        query.orderBy(cb.asc(wgt.get(Widget_.itemIndex)));
        TypedQuery<Widget> emQuery = em.createQuery(query);
        return emQuery.getResultList();
    }

    public void addWidget(Long userId, Widget widget) throws NotFoundException {
        SysUser u = read(userId);
        List<Widget> widgets = u.getWidgets();

        for (Widget w : widgets) {
            if (w.getIdentifier().equals(widget.getIdentifier())) {
                return;
            }
        }

        widget.setItemIndex(widgets.size());
        RelationUtil.relate(u, widget);
    }

    public void saveWidgets(Long userId, List<Widget> widgets) throws NotFoundException {
        SysUser u = read(userId);
        u.setWidgets(new ArrayList<Widget>());

        int index = 0;
        for (Widget w : widgets) {
            if (w.getId() != null) {
                w.setItemIndex(index);
                widgetEJB.update(w);
                index++;
                continue;
            }

            w.setItemIndex(index);
            RelationUtil.relate(u, w);
            index++;
        }
    }

    public SysUser createUser(LDAPUser ldapUser) {
        SysUser sysUser = new SysUser();
        sysUser.setLoginName(ldapUser.getUsername());
        sysUser.setUserRole(RoleType.USER);
        sysUser.setEmail(ldapUser.getEmail());
        sysUser.setRealName(ldapUser.getRealname());
        sysUser.setSendEmail(Boolean.TRUE);
        sysUser.setNextUser(LDAPUtil.isNextUser(ldapUser.getUsername()));
        sysUser.setTimezone(TimezoneUtil.getTimezoneByNokiaSite(ldapUser.getNokiaSite()));
        sysUser.setSecretKey(SysUserUtil.generateSecretKey());
        sysUser.setLastLogin(new Date());
        sysUser.setShowTutorials(Boolean.TRUE);
        create(sysUser);
        return sysUser;
    }

    public List<Project> getProjectAccess(Long id) {
        return getJoinList(id, SysUser_.projectAccess);
    }

    /**
     * Gets user project access from cache. If cache does not contain user
     * project accesses database is accessed. This method doesn't affect data in
     * cache.
     *
     * @param loginName User login name
     * @return List of projects where user has access.
     * @throws NotFoundException If user can not be found with login name.
     */
    public List<Project> getCachedProjectAccess(String loginName) throws NotFoundException {
        SysUser user = cache.get(loginName);
        if (user != null) {
            return user.getProjectAccess();
        }
        user = getSysUser(loginName);
        return getProjectAccess(user.getId());
    }

    public List<Project> getCachedAdminProjectAccess(String loginName) throws NotFoundException {
        SysUser user = cache.get(loginName);
        if (user != null) {
            return user.getProjectAdminAccess();
        }
        user = getSysUser(loginName);
        return getProjectAdminAccess(user.getId());
    }

    public void cleanCachedUserData(String loginName) {
        cache.remove(loginName);
    }

    public void clearProjectAccess(Long id, Gerrit g) throws NotFoundException {
        SysUser user = read(id);

        List<Project> projects = new ArrayList<Project>();
        projects.addAll(user.getProjectAccess());
        for (Project p : projects) {
            if (p.getGerrit().equals(g)) {
                RelationUtil.unrelate(user, p);
            }
        }
    }

    public List<Project> getProjectAdminAccess(Long id) {
        return getJoinList(id, SysUser_.projectAdminAccess);
    }

    /**
     * Gets paged build groups for user.
     *
     * @param id User id
     * @param first Index of first result in result set. Starts from 0.
     * @param pageSize Size of returned results
     * @param orderField Build group field to order results. Default: startTime
     * @param order Direction of ordering
     * @return List of build groups
     */
    @Override
    public List<BuildGroup> getBuildGroups(Long id, int first, int pageSize, String orderField, Order order) {
        List<BuildGroup> resultList = new ArrayList<BuildGroup>();

        try {
            if (first < 0 || pageSize < 1) {
                return null;
            }
            SysUser user = read(id);

            if (order == null) {
                order = Order.ASC;
            }

            if (StringUtils.isEmpty(orderField)) {
                orderField = "startTime";
            }

            if (log.isDebugEnabled()) {
                log.debug("Finding buildGroups where userId={}, first={}, pageSize={}, orderField={}, order={}",
                        new Object[]{id, first, pageSize, orderField, order.getSql()});
            }

            CriteriaQuery<BuildGroup> query = cb.createQuery(BuildGroup.class);
            Root<BuildGroup> from = query.from(BuildGroup.class);

            ListJoin<BuildGroup, Change> changesJoin = from.join(BuildGroup_.changes);
            Predicate emailpredicate = cb.equal(changesJoin.get(Change_.authorEmail), user.getEmail());
            Predicate securityPredicate = buildGroupEJB.getSecurityPredicate(from);
            query.where(cb.and(emailpredicate, securityPredicate));

            if (order == Order.ASC) {
                query.orderBy(cb.asc(from.get(orderField)));
            } else {
                query.orderBy(cb.desc(from.get(orderField)));
            }

            query.distinct(true);
            resultList = em.createQuery(query).setFirstResult(first).setMaxResults(pageSize).getResultList();
            log.debug("Found {} buildGroups", resultList.size());
        } catch (NotFoundException ex) {
            log.error("Could not fetch buildgroups for user {}. cause: {}", id, ex.getMessage());
        }

        return resultList;
    }

    /**
     * Counts build groups for user.
     *
     * @param id Job id
     * @return number of build groups for given job
     */
    @Override
    public Long getBuildGroupCount(Long id) {
        Long count = 0L;
        try {
            log.debug("Counting all build groups for user with id {}", id);
            SysUser user = read(id);
            CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
            Root<BuildGroup> buildGroup = criteria.from(BuildGroup.class);
            buildGroup.alias("bg");
            criteria.select(cb.count(buildGroup));

            Join changesJoin = buildGroup.join(BuildGroup_.changes);
            Path authorEmail = changesJoin.get(Change_.authorEmail);
            Predicate predicate = cb.and(cb.equal(authorEmail, user.getEmail()), buildGroupEJB.getSecurityPredicate(buildGroup));
            criteria.where(predicate);

            count = em.createQuery(criteria).getSingleResult();
            log.debug("Found {} build groups", count);
        } catch (NotFoundException ex) {
            log.error("Could not fetch build group count for user {}", id);
        }
        return count;
    }

    /**
     * Search {@link SysUser} entities by login name, email and real name.
     *
     * @param query Free text search query
     * @return List of matching users
     */
    public List<SysUser> search(String query) {
        return search(query, SysUser_.loginName, SysUser_.realName,
                SysUser_.email);
    }

    public List<Change> getChanges(Long id, int first, int pageSize, String orderField, Order order) {
        List<Change> changes = new ArrayList<Change>();
        try {
            if (first < 0 || pageSize < 1) {
                return null;
            }

            SysUser user = read(id);

            if (order == null) {
                order = Order.ASC;
            }

            if (StringUtils.isEmpty(orderField)) {
                orderField = "commitTime";
            }

            if (log.isDebugEnabled()) {
                log.debug("Finding changes where userId={}, first={}, pageSize={}, orderField={}, order={}",
                        new Object[]{id, first, pageSize, orderField, order.getSql()});
            }

            CriteriaQuery<Change> criteria = cb.createQuery(Change.class);
            Root<Change> change = criteria.from(Change.class);
            Predicate predicate = cb.and(cb.equal(change.get(Change_.authorEmail), user.getEmail()), changeEJB.getSecurityPredicate(change));
            criteria.where(predicate);

            if (order == Order.ASC) {
                criteria.orderBy(cb.asc(change.get(orderField)));
            } else {
                criteria.orderBy(cb.desc(change.get(orderField)));
            }

            changes = em.createQuery(criteria).setFirstResult(first).setMaxResults(pageSize).getResultList();
            log.debug("Found {} changes", changes.size());
        } catch (NotFoundException ex) {
            log.error("Could not fetch changes for user {}", id);
        }

        return changes;
    }

    public Long getChangeCount(Long id) {
        Long count = 0L;
        try {
            log.debug("Counting all changes for user with id {}", id);
            SysUser user = read(id);
            CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
            Root<Change> change = criteria.from(Change.class);
            criteria.select(cb.count(change));

            Predicate predicate = cb.and(cb.equal(change.get(Change_.authorEmail), user.getEmail()), changeEJB.getSecurityPredicate(change));
            criteria.where(predicate);

            count = em.createQuery(criteria).getSingleResult();
            log.debug("Found {} changes", count);
        } catch (NotFoundException ex) {
            log.error("Could not fetch change count for user {}", id);
        }
        return count;
    }

    public Boolean resolveAsNextUser(String email) {

        if (StringUtils.isEmpty(email)) {
            log.error("Resolving null or empty email as NOT NEXt user!");
            return false;
        }

        if (email.endsWith("@nokia.com") && !email.startsWith("ext-")) {
            return false;
        }

        String loginName;
        log.debug("Finding SysUser where email={}", email);
        CriteriaQuery<SysUser> criteria = cb.createQuery(SysUser.class);
        Root<SysUser> sysUser = criteria.from(SysUser.class);
        criteria.where(cb.equal(sysUser.get(SysUser_.email), email));
        TypedQuery<SysUser> emQuery = em.createQuery(criteria);

        try {
            loginName = emQuery.getSingleResult().getLoginName();
            return LDAPUtil.isNextUser(loginName);
        } catch (NoResultException e) {
            log.debug("Could not find next user information in CI database with email {}", email);
        }
        LDAPUtil util = new LDAPUtil();
        try {
            loginName = util.getUserIdByEmail(email);
            if (loginName != null) {
                return LDAPUtil.isNextUser(loginName);
            } else {
                return true;
            }
        } catch (LDAPException e) {
            return true;
        }
    }

    public SysUser addFile(Long id, UserFile userFile) throws NotFoundException {
        log.debug("Received {} user file for SysUser {}", id);
        long startTime = System.currentTimeMillis();

        SysUser sysUser = read(id);

        RelationUtil.relate(sysUser, userFile);

        log.debug("Finished adding file for SysUser {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return sysUser;
    }

    public List<UserFile> getFiles(Long id) {
        return getJoinList(id, SysUser_.userFiles);
    }

    public SysUser setProfileImage(Long id, SysUserImage image) throws NotFoundException {
        SysUser user = read(id);
        user.setUserImage(image);
        return update(user);
    }
}
