package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Branch_;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeTracker;
import com.nokia.ci.ejb.model.ChangeTracker_;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobPrePostVerification;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectAnnouncement;
import com.nokia.ci.ejb.model.ProjectExternalLink;
import com.nokia.ci.ejb.model.ProjectGroup;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.Project_;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationType;
import com.nokia.ci.ejb.util.ListUtils;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link Project} object operations.
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
public class ProjectEJB extends CrudFunctionality<Project> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ProjectEJB.class);

    public ProjectEJB() {
        super(Project.class);
    }

    /**
     * Gets branches for given project.
     *
     * @param id Project id
     * @return List of branches
     */
    public List<Branch> getBranches(Long id) {
        return getJoinList(id, Project_.branches);
    }

    /**
     * Returns all products for project.
     *
     * @param id Project id
     * @return list of {@link Product} objects
     */
    public List<Product> getProducts(Long id) {
        return getJoinList(id, Project_.products);
    }

    public List<SysUser> getAdmins(Long id) {
        return getJoinList(id, Project_.adminAccess);
    }

    public void checkAdminRights(Long id, SysUser user) throws UnauthorizedException {
        if (user.getUserRole() == RoleType.SYSTEM_ADMIN) {
            return;
        }

        if (user.getUserRole() == RoleType.USER) {
            throw new UnauthorizedException("Not project admin!");
        }

        List<SysUser> admins = getAdmins(id);
        for (SysUser u : admins) {
            if (u.getId().equals(user.getId())) {
                return;
            }
        }

        throw new UnauthorizedException("Not project admin!");
    }

    /**
     * Returns project verifications by type
     *
     * @param id Project id type VerificationType
     * @return list of {@link Verification} objects
     */
    private List<Verification> getVerificationsByType(List<Verification> verifications, VerificationType type) {
        List<Verification> ret = new ArrayList<Verification>();
        for (Verification v : verifications) {
            if (v.getType() == type) {
                ret.add(v);
            }
        }
        return ret;
    }

    public List<Project> getProjectsWithToolboxOrDraftBranch() {
        log.debug("Finding projects with toolbox or draft branch");

        CriteriaQuery<Project> query = cb.createQuery(Project.class);
        Root<Project> project = query.from(Project.class);
        ListJoin<Project, Branch> listJoin = project.join(Project_.branches);
        Predicate typeToolbox = cb.equal(listJoin.get(Branch_.type), BranchType.TOOLBOX);
        Predicate typeDraft = cb.equal(listJoin.get(Branch_.type), BranchType.DRAFT);
        query.where(cb.or(typeToolbox, typeDraft));
        List<Project> projects = em.createQuery(query).getResultList();

        log.debug("Found {} projects with toolbox branch", projects.size());
        return projects;
    }

    public List<Project> getProjectsByGerritProject(String gerritProject, Gerrit gerrit) {
        log.debug("Finding project with gerrit project {}", gerritProject);
        CriteriaQuery<Project> query = cb.createQuery(Project.class);
        Root<Project> project = query.from(Project.class);

        Predicate projPredicate = cb.equal(project.get(Project_.gerritProject), gerritProject);
        Predicate gerritPredicate = cb.equal(project.get(Project_.gerrit), gerrit);
        query.where(cb.and(projPredicate, gerritPredicate));

        return em.createQuery(query).getResultList();
    }

    /**
     * Returns all verification for project.
     *
     * @param id Project id
     * @return list of {@link Verification} objects
     */
    public List<Verification> getAllVerifications(Long id) {
        return getJoinList(id, Project_.verifications);
    }

    /**
     * Returns normal verification for project.
     *
     * @param id Project id
     * @return list of {@link Verification} objects
     */
    public List<Verification> getVerifications(Long id) {
        return getVerificationsByType(getAllVerifications(id), VerificationType.NORMAL);
    }

    /**
     * Returns pre-verifications for project.
     *
     * @param id Project id
     * @return list of {@link Verification} objects
     */
    public List<Verification> getPreVerifications(Long id) {
        return getVerificationsByType(getAllVerifications(id), VerificationType.PRE_BUILD);
    }

    /**
     * Returns all post-verifications for project.
     *
     * @param id Project id
     * @return list of {@link Verification} objects
     */
    public List<Verification> getPostVerifications(Long id) {
        return getVerificationsByType(getAllVerifications(id), VerificationType.POST_BUILD);
    }

    /**
     * Returns all verification configurations for project.
     *
     * @param id Project id
     * @return list of {@link ProjectVerificationConf} objects
     */
    public List<ProjectVerificationConf> getVerificationConfs(Long id) {
        return getJoinList(id, Project_.verificationConfs);
    }

    /**
     * Returns all external links for project.
     *
     * @param id
     * @return
     * @throws NotFoundException
     */
    public List<ProjectExternalLink> getExternalLinks(Long id) {
        return getJoinList(id, Project_.links);
    }

    /**
     * Gets announcement for given project.
     *
     * @param id Project id
     * @return List of announcement
     */
    public List<ProjectAnnouncement> getProjectAnnouncements(Long id) {
        return getJoinList(id, Project_.announcements);
    }

    public void addBranch(Long projectId, Branch branch) throws
            NotFoundException {
        Project p = read(projectId);
        List<Branch> branches = new ArrayList<Branch>();
        branches.addAll(p.getBranches());
        branches.add(branch);
        setBranches(p, branches);
    }

    @Override
    public void delete(Project project) throws NotFoundException {
        Project p = read(project.getId());

        List<Branch> branches = new ArrayList();
        branches.addAll(p.getBranches());
        for (Branch b : branches) {
            RelationUtil.unrelate(p, b);
        }

        em.remove(p);
    }

    public void update(Project project, List<ProjectVerificationConf> selectedProjectVerificationConfs) throws NotFoundException {
        Project p = update(project);
        for (ProjectVerificationConf conf : p.getVerificationConfs()) {
            em.remove(conf);
        }
        p.getVerificationConfs().clear();
        attachVerificationConfs(p, selectedProjectVerificationConfs);
    }

    public void create(Project project, List<ProjectVerificationConf> selectedProjectVerificationConfs) {
        create(project);
        project.setVerificationConfs(new ArrayList<ProjectVerificationConf>());
        attachVerificationConfs(project, selectedProjectVerificationConfs);
    }

    public void setBranches(Long id, List<Branch> branches) throws NotFoundException {
        Project p = read(id);
        setBranches(p, branches);
    }

    private void setBranches(Project p, List<Branch> branches) {
        detachBranches(p);
        List<Branch> managedBranches = new ArrayList<Branch>();
        for (Branch b : branches) {
            managedBranches.add(em.find(Branch.class, b.getId()));
        }

        for (Branch b : managedBranches) {
            RelationUtil.relate(p, b);
        }
    }

    private void detachBranches(Project project) {
        List<Branch> oldBranchList = new ArrayList<Branch>();
        oldBranchList.addAll(project.getBranches());
        for (Branch b : oldBranchList) {
            RelationUtil.unrelate(project, b);
        }
    }

    public void setProjectGroup(Long id, ProjectGroup projectGroup) throws NotFoundException {
        Project p = read(id);

        RelationUtil.unrelateProjectGroup(p);

        if (projectGroup != null) {
            ProjectGroup g = em.find(ProjectGroup.class, projectGroup.getId());
            RelationUtil.relate(g, p);
        }
    }

    public void setGerrit(Long id, Gerrit gerrit) throws NotFoundException {
        Project p = read(id);

        RelationUtil.unrelateGerrit(p);

        if (gerrit != null) {
            Gerrit g = em.find(Gerrit.class, gerrit.getId());
            RelationUtil.relate(g, p);
        }
    }

    private void attachVerificationConfs(Project project, List<ProjectVerificationConf> selectedProjectVerificationConfs) {
        for (ProjectVerificationConf selected : selectedProjectVerificationConfs) {
            RelationUtil.relate(project, selected);
        }
    }

    public void removeJobVerifications(Long id, List<Verification> verifications) throws NotFoundException {
        Project p = read(id);
        List<Branch> branches = p.getBranches();

        for (Branch b : branches) {
            List<Job> jobs = b.getJobs();
            for (Job j : jobs) {
                List<JobPrePostVerification> prePost = new ArrayList();
                prePost.addAll(j.getPostVerifications());
                prePost.addAll(j.getPreVerifications());
                for (JobPrePostVerification v : prePost) {
                    if (verifications.contains(v.getVerification())) {
                        RelationUtil.unrelate(j, v);
                        em.remove(v);
                    }
                }
                j.getPreVerifications().clear();
                j.getPostVerifications().clear();
            }
        }
    }

    public List<ChangeTracker> getChangeTrackers(Long id) throws NotFoundException {
        Project project = read(id);
        CriteriaQuery<ChangeTracker> query = cb.createQuery(ChangeTracker.class);
        Root<ChangeTracker> from = query.from(ChangeTracker.class);
        query.where(cb.and(cb.isMember(project, from.get(ChangeTracker_.projects))));
        return em.createQuery(query).getResultList();
    }

    public List<ChangeTracker> getChangeTrackers(Long id, List<Change> changes) throws NotFoundException {
        Project project = read(id);

        if (changes.isEmpty()) {
            return new ArrayList<ChangeTracker>();
        }

        List<String> unprocessedIds = new ArrayList<String>();
        for (Change change : changes) {
            unprocessedIds.add(change.getCommitId());
        }

        // 1. Get existing change trackers that are linked to this project.
        List<ChangeTracker> ownedCts = getChangeTrackers(project, true, unprocessedIds);
        for (ChangeTracker ownedCt : ownedCts) {
            unprocessedIds.remove(ownedCt.getCommitId());
        }

        // 2. Get existing unlinked change trackers and link.
        List<ChangeTracker> nonOwnedCts = getChangeTrackers(project, false, unprocessedIds);
        for (ChangeTracker ct : nonOwnedCts) {
            RelationUtil.relate(project, ct);
        }
        ownedCts.addAll(nonOwnedCts);
        for (ChangeTracker nonOwnedCt : nonOwnedCts) {
            unprocessedIds.remove(nonOwnedCt.getCommitId());
        }

        // 3. Create and link non-existing change trackers.
        for (String commitId : unprocessedIds) {
            ChangeTracker ct = new ChangeTracker();
            ct.setCommitId(commitId);
            em.persist(ct);
            RelationUtil.relate(project, ct);
            ownedCts.add(ct);
        }

        // Return all change trackers from steps 1,2 and 3.
        return ownedCts;
    }

    private List<ChangeTracker> getChangeTrackers(Project project, boolean isMember, List<String> commitIds) {
        List<ChangeTracker> cts = new ArrayList<ChangeTracker>();
        // Hibernate can not handle more than 1000 in predicates in a same query. therefore partition to smaller patches.
        List<List<String>> subLists = ListUtils.partition(commitIds, 512);
        for (List subList : subLists) {
            List<ChangeTracker> subResults = getChangeTrackersPatch(project, isMember, subList);
            cts.addAll(subResults);
        }
        return cts;
    }

    private List<ChangeTracker> getChangeTrackersPatch(Project project, boolean isMember, Collection<String> commitIds) {
        if (commitIds.isEmpty()) {
            return new ArrayList<ChangeTracker>();
        }

        CriteriaQuery<ChangeTracker> query = cb.createQuery(ChangeTracker.class);
        Root<ChangeTracker> from = query.from(ChangeTracker.class);
        Predicate commitIdPredicate = from.get(ChangeTracker_.commitId).in(commitIds);
        Predicate projectPredicate;
        if (isMember) {
            projectPredicate = cb.isMember(project, from.get(ChangeTracker_.projects));
        } else {
            projectPredicate = cb.isNotMember(project, from.get(ChangeTracker_.projects));
        }
        query.where(cb.and(commitIdPredicate, projectPredicate));
        return em.createQuery(query).getResultList();
    }
}
