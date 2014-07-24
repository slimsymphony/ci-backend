package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildGroup_;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeFile;
import com.nokia.ci.ejb.model.ChangeFile_;
import com.nokia.ci.ejb.model.ChangeFileType;
import com.nokia.ci.ejb.model.Change_;
import com.nokia.ci.ejb.util.ListUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class ChangeEJB extends SecurityFunctionality<Change> implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(ChangeEJB.class);
    @EJB
    private SysUserEJB sysUserEJB;

    public ChangeEJB() {
        super(Change.class);
    }

    public List<ChangeFile> getChangeFiles(Long id) {
        return getJoinList(id, Change_.changeFiles);
    }

    public List<Change> getParentChanges(Long id) {
        return getJoinList(id, Change_.parentChanges);
    }

    public List<Change> getChildChanges(Long id) {
        return getJoinList(id, Change_.childChanges);
    }

    public Change getChangeByCommitId(String commitId) throws NotFoundException {
        CriteriaQuery<Change> query = cb.createQuery(Change.class);
        Root<Change> change = query.from(Change.class);
        query.select(change);

        Predicate predicate = cb.equal(change.get(Change_.commitId), commitId);
        query.where(predicate);

        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            log.warn("Could not find commit with id " + commitId);
            throw new NotFoundException(e);
        } catch (NonUniqueResultException ex) {
            log.error("Found more than one change with commit id " + commitId + ", returning first..", ex);
            return em.createQuery(query).getResultList().get(0);
        }
    }

    public Change getChangeByCommitIdSecure(String commitId) throws BackendAppException {
        Change change = getChangeByCommitId(commitId);
        checkAuth(change);
        return change;
    }

    private List<Change> getChangesByCommitIds(List<String> commitIds) {
        if (commitIds.isEmpty()) {
            return new ArrayList<Change>();
        }

        List<Change> changes = new ArrayList<Change>();

        List<List<String>> subLists = ListUtils.partition(commitIds, 512);
        for (List subList : subLists) {
            List<Change> subResults = getChangesByCommitIdsPatch(subList);
            changes.addAll(subResults);
        }

        return changes;
    }

    private List<Change> getChangesByCommitIdsPatch(List<String> commitIds) {
        if (commitIds.isEmpty()) {
            return new ArrayList<Change>();
        }

        CriteriaQuery<Change> query = cb.createQuery(Change.class);
        Root<Change> from = query.from(Change.class);
        Predicate commitIdPredicate = from.get(Change_.commitId).in(commitIds);
        query.where(commitIdPredicate);

        return em.createQuery(query).getResultList();
    }

    public List<ChangeFile> getChangeFilesByType(Long id, ChangeFileType type) {
        CriteriaQuery<ChangeFile> query = cb.createQuery(ChangeFile.class);
        Root<ChangeFile> changeFile = query.from(ChangeFile.class);

        Predicate predicateChange = cb.equal(changeFile.get(ChangeFile_.change), id);
        Predicate predicateType = cb.equal(changeFile.get(ChangeFile_.fileType), type);
        query.where(cb.and(predicateChange, predicateType));

        return em.createQuery(query).getResultList();
    }

    /**
     * Persist list of given changes. return list of attached changes. Some of
     * the given changes might already be in database.
     *
     * @param changes
     * @return
     */
    public List<Change> getChanges(List<Change> changes) {
        if (changes.isEmpty()) {
            return new ArrayList<Change>();
        }
        log.info("Getting attached changes for {} unprocessed changes", changes.size());
        // create <COMMIT_ID, CHANGE> map to help processing.
        Map<String, Change> unprocessedChanges = new HashMap<String, Change>();

        for (Change change : changes) {
            if (StringUtils.isEmpty(change.getCommitId())) {
                log.warn("found empty string as commit id for change {}", change);
                continue;
            }
            unprocessedChanges.put(change.getCommitId(), change);
        }

        // fetch existing changes
        List<Change> attachedChanges = getChangesByCommitIds(new ArrayList<String>(unprocessedChanges.keySet()));

        log.info("Found {} changes already in database", attachedChanges.size());
        List<Change> tobeRemoved = new ArrayList<Change>();
        // merge change if existing change found. Then delete from map of unprocessed changes.
        for (Change target : attachedChanges) {
            Change source = unprocessedChanges.get(target.getCommitId());
            if (source == null) {
                tobeRemoved.add(target);
                log.warn("List of unprocessed changes does not contain change that was found from database. commit id {}.", target.getCommitId());
                continue;
            }
            target.merge(source);
            unprocessedChanges.remove(source.getCommitId());
        }
        attachedChanges.removeAll(tobeRemoved);

        log.info("Creating {} new changes", unprocessedChanges.values().size());
        // Following changes are new to system.
        for (Change change : unprocessedChanges.values()) {

            //resolve NEXT user value for new changes
            if (StringUtils.isEmpty(change.getAuthorEmail())) {
                log.warn("Change {} contains author with empty or null email address!", change);
            }

            Boolean nextUser = sysUserEJB.resolveAsNextUser(change.getAuthorEmail());
            change.setHasNextUser(nextUser);
            log.info("Creating change {}", change.getCommitId());
            createNoFlush(change);
            attachedChanges.add(change);
        }
        unprocessedChanges.clear();
        log.info("Done processing changes. Returning {} attached changes", attachedChanges.size());

        return attachedChanges;
    }

    /**
     * Search {@link Change} entities by commit ID, author email, author name
     * and subject.
     *
     * @param query Free text search query
     * @return List of matching changes
     */
    public List<Change> search(String query) {
        return search(query, Change_.commitId, Change_.authorName,
                Change_.authorEmail, Change_.subject);
    }

    public List<BuildGroup> getBuildGroups(Long id) {
        return getJoinList(id, Change_.buildGroups);
    }

    public BuildStatus getLatestBuildGroupStatus(Long id, BranchType branchType) throws NotFoundException {

        CriteriaQuery<BuildGroup> query = cb.createQuery(BuildGroup.class);
        Root<Change> change = query.from(Change.class);
        ListJoin<Change, BuildGroup> buildGroups = change.join(Change_.buildGroups);
        query.select(buildGroups);
        query.where(cb.and(cb.equal(change, id), cb.equal(buildGroups.get(BuildGroup_.branchType), branchType)));
        query.orderBy(cb.desc(buildGroups.get(BuildGroup_.endTime)));

        List<BuildGroup> results = em.createQuery(query).getResultList();
        if (results.isEmpty()) {
            throw new NotFoundException("No buildGroup results (type = " + branchType + ") found for change " + id);
        }
        return results.get(0).getStatus();

    }
}
