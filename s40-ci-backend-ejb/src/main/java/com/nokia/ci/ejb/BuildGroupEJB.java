/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.event.BuildGroupFinishedEvent;
import com.nokia.ci.ejb.event.BuildGroupReleasedEvent;
import com.nokia.ci.ejb.event.BuildGroupStartedEvent;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.InvalidPhaseException;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.jms.JobStartProducer;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildGroup_;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.BuildResultDetailsParam_;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.BuildVerificationConf_;
import com.nokia.ci.ejb.model.Build_;
import com.nokia.ci.ejb.model.CIServer;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Change_;
import com.nokia.ci.ejb.model.Component;
import com.nokia.ci.ejb.model.Component_;
import com.nokia.ci.ejb.model.Release;
import com.nokia.ci.ejb.model.Release_;
import com.nokia.ci.ejb.util.RelationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author miikka
 */
@Stateless
@LocalBean
public class BuildGroupEJB extends SecurityFunctionality<BuildGroup> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(BuildGroupEJB.class);
    @EJB
    BuildEJB buildEJB;
    @EJB
    ChangeEJB changeEJB;
    @EJB
    CIServerEJB ciServerEJB;
    @Inject
    @BuildGroupStartedEvent
    Event<Long> bgStartedEvent;
    @Inject
    @BuildGroupFinishedEvent
    Event<Long> bgFinishedEvent;
    @Inject
    @BuildGroupReleasedEvent
    Event<Long> bgReleasedEvent;
    @Inject
    JobStartProducer producer;

    public BuildGroupEJB() {
        super(BuildGroup.class);
    }

    public List<Build> getBuilds(long id) {
        return getJoinList(id, BuildGroup_.builds);
    }

    public List<Change> getChanges(long id) {
        return getJoinList(id, BuildGroup_.changes);
    }

    public List<Change> getChangesWithFiles(long id) throws NotFoundException {
        BuildGroup buildGroup = read(id);
        CriteriaQuery<Change> criteria = cb.createQuery(Change.class);
        Root<Change> change = criteria.from(Change.class);
        change.fetch(Change_.changeFiles, JoinType.LEFT);
        criteria.select(change);
        Predicate bgPredicate = cb.isMember(buildGroup, change.get(Change_.buildGroups));
        criteria.where(cb.and(bgPredicate, changeEJB.getSecurityPredicate(change))).distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    public List<BuildGroup> getStartedBuildGroups() {
        log.debug("Finding started buildGroups");
        CriteriaQuery<BuildGroup> criteria = cb.createQuery(BuildGroup.class);
        Root<BuildGroup> buildGroup = criteria.from(BuildGroup.class);
        criteria.where(cb.equal(buildGroup.get(BuildGroup_.phase), BuildPhase.STARTED));
        List<BuildGroup> buildGroups = em.createQuery(criteria).getResultList();
        log.debug("Found {} buildgroups", buildGroups.size());
        return buildGroups;
    }

    public List<BuildResultDetailsParam> getBuildResultDetailsParams(long id) {

        Class<BuildResultDetailsParam> joinClass = BuildResultDetailsParam.class;
        if (log.isDebugEnabled()) {
            log.debug("Quering list of {} for {}", joinClass, BuildGroup.class);
        }
        CriteriaQuery<BuildResultDetailsParam> query = cb.createQuery(BuildResultDetailsParam.class);
        Root<BuildGroup> root = query.from(BuildGroup.class);
        ListJoin<BuildGroup, Build> buildJoin = root.join(BuildGroup_.builds);
        Join<Build, BuildVerificationConf> buildVerificationConfPath = buildJoin.join(Build_.buildVerificationConf);
        ListJoin<BuildVerificationConf, BuildResultDetailsParam> buildResultDetailsParamJoin = buildVerificationConfPath.join(BuildVerificationConf_.buildResultDetailsParams);
        buildResultDetailsParamJoin.fetch(BuildResultDetailsParam_.buildVerificationConf);
        query.select(buildResultDetailsParamJoin);
        query.where(cb.and(cb.equal(root, id), cb.isNotNull(buildResultDetailsParamJoin.get(BuildResultDetailsParam_.paramValue))));

        List<BuildResultDetailsParam> params = em.createQuery(query).getResultList();

        if (log.isDebugEnabled()) {
            log.debug("Found {} of {} for {}", new Object[]{params.size(), joinClass, BuildGroup.class});
        }
        return params;
    }

    public List<BuildGroup> getPreviousBuildGroupsWithSameJob(Long id) throws NotFoundException {
        return getPreviousBuildGroupsWithSameJob(id, 10);
    }

    /**
     * Returns previous build group with same Job from the given id
     *
     * @param id Last build group id
     * @return Previous build group
     */
    public List<BuildGroup> getPreviousBuildGroupsWithSameJob(Long id, int limit) throws NotFoundException {
        BuildGroup old = read(id);
        CriteriaQuery<BuildGroup> criteria = cb.createQuery(BuildGroup.class);
        Root<BuildGroup> buildGroup = criteria.from(BuildGroup.class);
        Predicate sameJob = cb.equal(buildGroup.get(BuildGroup_.job), old.getJob());
        Predicate earlier = cb.lessThan(buildGroup.get(BuildGroup_.startTime), old.getStartTime());
        criteria.where(cb.and(sameJob, earlier));
        criteria.orderBy(cb.desc(buildGroup.get(BuildGroup_.startTime)));
        return em.createQuery(criteria).setMaxResults(limit).getResultList();
    }

    public void release(Long id) throws NotFoundException, InvalidArgumentException {
        BuildGroup buildGroup = read(id);
        if (buildGroup.getRelease() != null) {
            throw new InvalidArgumentException("BuildGroup already released");
        }

        Release release = new Release();
        release.setReleaseTime(new Date());
        RelationUtil.relate(buildGroup, release);

        bgReleasedEvent.fire(id);
    }

    /**
     * Gets start node builds from given build group. Start node builds are
     * builds that will be first in the build chain.
     *
     * @param id Build group id
     * @return List of start node builds.
     */
    public List<Build> getStartNodes(Long id) {
        CriteriaQuery<Build> criteriaQuery = cb.createQuery(Build.class);
        Root<Build> build = criteriaQuery.from(Build.class);
        Predicate buildGroupEquals = cb.equal(build.get(Build_.buildGroup), id);
        Predicate isStartNode = cb.equal(build.get(Build_.startNode), true);
        criteriaQuery.where(cb.and(buildGroupEquals, isStartNode));
        return em.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Starts start node builds of build group.
     *
     * @param id Build group id
     * @throws NotFoundException If build group can not be found with given id.
     */
    public void start(Long id) throws NotFoundException {
        BuildGroup buildGroup = read(id);
        log.info("Starting {}", buildGroup);
        List<Build> startNodes = getStartNodes(id);
        if (startNodes.isEmpty()) {
            log.warn("There was no start node builds in {}. This may indicate configuration problem. Check configuration of {}", buildGroup, buildGroup.getJob());
            return;
        }

        for (Build startNode : startNodes) {
            buildEJB.start(startNode.getId());
        }

        bgStartedEvent.fire(id);
    }

    public Release getRelease(Long id) {
        try {
            CriteriaQuery<Release> criteriaQuery = cb.createQuery(Release.class);
            Root<Release> release = criteriaQuery.from(Release.class);
            criteriaQuery.where(cb.equal(release.get(Release_.buildGroup), id));
            return em.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException nre) {
            log.debug("No results found {}", nre.getMessage());
        }
        return null;
    }

    public BuildGroup updateStatus(Long id, BuildStatus buildStatus) throws InvalidPhaseException, NotFoundException {
        BuildGroup buildGroup = read(id);
        if (buildGroup.getPhase() == BuildPhase.FINISHED) {
            throw new InvalidPhaseException("BuildGroup " + buildGroup + " is already finished! Can not update status anymore!");
        }

        if (buildGroup.getStatus() == null) {
            // Monitor/legacy style buildgroups might have null status.
            buildGroup.setStatus(buildStatus);
        } else {
            buildGroup.setStatus(buildGroup.getStatus().combine(buildStatus));
        }

        log.info("Check if all builds are ready in group {}", buildGroup);
        for (Build build : buildGroup.getBuilds()) {
            if (build.getPhase() != BuildPhase.FINISHED) {
                log.info("There is unfinished builds in group {}", buildGroup);
                return buildGroup;
            }
        }

        // Buildgroup is finished
        log.info("All builds are finished in group {}", buildGroup);
        buildGroup.setPhase(BuildPhase.FINISHED);
        buildGroup.setEndTime(new Date());

        // Update CIServer if exists
        if (buildGroup.getBuildGroupCIServer() != null) {
            String uuid = buildGroup.getBuildGroupCIServer().getCiServerUuid();
            if (!StringUtils.isEmpty(uuid)) {
                ciServerEJB.finalizeBuild(uuid);
            }
        }

        boolean passed = (buildGroup.getStatus() == BuildStatus.SUCCESS || buildGroup.getStatus() == BuildStatus.UNSTABLE);
        if (passed) {
            buildGroup.getJob().setLastSuccesfullFetchHead(buildGroup.getJob().getLastFetchHead());
        }
        bgFinishedEvent.fire(id);

        return buildGroup;
    }

    public BuildGroup addComponents(Long id, List<Component> components) throws NotFoundException {
        log.debug("Received {} components for build group {}", components.size(), id);
        long startTime = System.currentTimeMillis();

        List<Component> groupComponents = getComponentsByBuildGroupId(id);
        BuildGroup buildGroup = read(id);

        // Add build events.
        for (Component component : components) {

            if (!componentNameExists(groupComponents, component)) {
                RelationUtil.relate(buildGroup, component);
                log.debug("New component {} is created", component.getName());
            } else {
                log.debug("Component name {} for this build group exists, bypass creating it.", component.getName());
            }
        }

        log.info("Finished adding components for build group {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return buildGroup;
    }

    public List<Component> getComponents(long id) {
        return getJoinList(id, BuildGroup_.components);
    }

    public List<Component> getComponentsByBuildGroupIdAndName(Long buidGroupId, String componentName) {
        log.debug("Finding components where buildGroupId={} and name={}", buidGroupId, componentName);
        CriteriaQuery<Component> criteria = cb.createQuery(Component.class);
        Root<Component> component = criteria.from(Component.class);
        Predicate buildGroupEqual = cb.equal(component.get(Component_.buildGroup), buidGroupId);
        Predicate componentEqual = cb.equal(component.get(Component_.name), componentName);
        criteria.where(cb.and(buildGroupEqual, componentEqual));
        List<Component> components = em.createQuery(criteria).getResultList();
        log.debug("Found {} components", components.size());
        return components;
    }

    public List<Component> getComponentsByBuildGroupId(Long buidGroupId) {
        log.debug("Finding components where buildGroupId={}", buidGroupId);
        CriteriaQuery<Component> criteria = cb.createQuery(Component.class);
        Root<Component> component = criteria.from(Component.class);
        criteria.where(cb.equal(component.get(Component_.buildGroup), buidGroupId));
        List<Component> components = em.createQuery(criteria).getResultList();
        log.debug("Found {} components", components.size());
        return components;
    }

    public void retrigger(Long id) throws NotFoundException, JobStartException {
        BuildGroup bg = read(id);

        List<Change> changes = new ArrayList<Change>();
        changes.addAll(getChanges(bg.getId()));
        producer.sendJobStart(bg.getJob().getId(), bg.getGerritRefSpec(),
                bg.getGerritPatchSetRevision(), changes);
    }

    /**
     * BuildGroup is classified ok if all of its classifiable builds are
     * classified ok, and all non-classifiable builds are successful.
     *
     * @param id
     * @return
     */
    public boolean isClassifiedOk(Long id) throws NotFoundException {

        List<Build> builds = getBuilds(id);
        for (Build build : builds) {

            if (buildEJB.isClassifiable(build.getId()) && !buildEJB.isClassifiedOk(build.getId())) {
                return false;
            }

            if (!buildEJB.isClassifiable(build.getId()) && build.getStatus().worstThan(BuildStatus.SUCCESS)) {
                return false;
            }
        }
        return true;
    }

    private boolean componentNameExists(List<Component> components, Component component) {
        for (Component curComponent : components) {
            if (component.getName().equals(curComponent.getName())) {
                return true;
            }
        }

        return false;
    }
}
