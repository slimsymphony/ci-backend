package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.*;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link Branch} object operations.
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
public class BranchEJB extends CrudFunctionality<Branch> implements Serializable {

    @EJB
    ProjectEJB projectEJB;
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(BranchEJB.class);

    public BranchEJB() {
        super(Branch.class);
    }

    public void checkAdminRights(Long id, SysUser user) throws BackendAppException {
        if (user.getUserRole() == RoleType.SYSTEM_ADMIN) {
            return;
        }

        if (user.getUserRole() == RoleType.USER) {
            throw new UnauthorizedException("Not project admin!");
        }

        Branch b = read(id);
        List<SysUser> admins = projectEJB.getAdmins(b.getProject().getId());
        for (SysUser u : admins) {
            if (u.getId().equals(user.getId())) {
                return;
            }
        }

        throw new UnauthorizedException("Not project admin!");
    }

    /**
     * Get jobs for branch by system user id. If system user id is provided all
     * personal jobs are included also to list of returned jobs.
     *
     * Only parent jobs are included in result.
     *
     * @param id Branch id
     * @param sysUserId System user id
     * @return List of jobs
     */
    public List<Job> getJobs(Long id, Long sysUserId) throws NotFoundException {
        Branch b = read(id);

        log.debug("Find jobs for branch {} by user id {}", b, sysUserId);
        CriteriaQuery<Job> query = cb.createQuery(Job.class);
        Root<Branch> branch = query.from(Branch.class);
        ListJoin<Branch, Job> jobs = branch.join(Branch_.jobs);
        query.select(jobs);
        Predicate predicate = cb.notEqual(branch.get(Branch_.type), BranchType.TOOLBOX);
        predicate = cb.and(predicate, cb.notEqual(branch.get(Branch_.type), BranchType.DRAFT));
        if (sysUserId != null) {
            SysUser sysUser = new SysUser();
            sysUser.setId(sysUserId);
            predicate = cb.or(predicate, cb.equal(jobs.get(Job_.owner), sysUser));
        }
        query.where(cb.and(predicate, cb.equal(branch, b)));
        List<Job> results = em.createQuery(query).getResultList();
        log.debug("Found {} jobs for branch {} by user id {}", new Object[]{results.size(), b, sysUserId});
        return results;
    }

    /**
     * Get jobs for branch. All types of jobs are returned.
     *
     * @param id Branch id
     * @return List of jobs
     */
    public List<Job> getJobs(Long id) {
        return getJoinList(id, Branch_.jobs);
    }

    public void setJobs(Long id, List<Job> jobs) throws NotFoundException {
        Branch b = read(id);

        List<Job> managedJobs = new ArrayList<Job>();
        for (Job j : jobs) {
            managedJobs.add(em.find(Job.class, j.getId()));
        }

        RelationUtil.unrelateJobs(b);

        for (Job j : managedJobs) {
            RelationUtil.relate(b, j);
        }
    }

    public List<Branch> getBranches(List<BranchType> branchTypes) {
        CriteriaQuery<Branch> query = cb.createQuery(Branch.class);
        Root<Branch> branch = query.from(Branch.class);
        query.where(branch.get(Branch_.type).in(branchTypes));
        List<Branch> results = em.createQuery(query).getResultList();
        return results;
    }

    public List<Job> getPeriodicJobsToTrigger(Long id) {
        CriteriaQuery<Job> query = cb.createQuery(Job.class);
        Root<Branch> branch = query.from(Branch.class);
        ListJoin<Branch, Job> jobs = branch.join(Branch_.jobs);
        query.select(jobs);
        query.where(cb.and(cb.equal(jobs.get(Job_.triggerType), JobTriggerType.POLL),
                cb.equal(jobs.get(Job_.branch), id)));
        List<Job> results = em.createQuery(query).getResultList();
        return results;
    }

    public List<Job> getScheduledJobsToTrigger(Long id) {
        CriteriaQuery<Job> query = cb.createQuery(Job.class);
        Root<Branch> branch = query.from(Branch.class);
        ListJoin<Branch, Job> jobs = branch.join(Branch_.jobs);
        query.select(jobs);
        query.where(cb.and(cb.equal(jobs.get(Job_.triggerType), JobTriggerType.SCHEDULE),
                cb.equal(jobs.get(Job_.branch), id)));
        List<Job> results = em.createQuery(query).getResultList();
        return results;
    }

    /**
     * Finds branches that are not connected to any project.
     *
     * @return list of non connected branches.
     */
    public List<Branch> getUnassignedBranches() {
        log.debug("Finding unassigned branches");

        CriteriaQuery<Branch> query = cb.createQuery(Branch.class);
        Root<Branch> branch = query.from(Branch.class);
        query.where(branch.get(Branch_.project).isNull());
        List<Branch> branches = em.createQuery(query).getResultList();

        log.debug("Found {} unassigned branches", branches.size());

        return branches;
    }

    public List<CIServer> getCIServers(Long id) {
        return getJoinList(id, Branch_.ciServers);
    }

    public void setCIServers(Long id, List<CIServer> ciServers) throws NotFoundException {
        Branch b = read(id);

        List<CIServer> managedCIServers = new ArrayList<CIServer>();
        for (CIServer s : ciServers) {
            managedCIServers.add(em.find(CIServer.class, s.getId()));
        }

        RelationUtil.unrelateCIServers(b);

        for (CIServer s : managedCIServers) {
            RelationUtil.relate(b, s);
        }
    }

    /**
     * Returns all verification configurations for branch.
     *
     * @param id Branch id
     * @return list of {@link BranchVerificationConf} objects
     */
    public List<BranchVerificationConf> getVerificationConfs(Long id) {
        return getJoinList(id, Branch_.verificationConfs);
    }

    public Branch update(Branch branch, List<BranchVerificationConf> selectedBranchVerificationConfs) throws NotFoundException {
        Branch b = update(branch);
        for (BranchVerificationConf conf : b.getVerificationConfs()) {
            em.remove(conf);
        }
        b.getVerificationConfs().clear();
        attachVerificationConfs(b, selectedBranchVerificationConfs);
        return b;
    }

    //TODO: remove updateFromUI method when branch status info is separated into another table
    public Branch updateFromUI(Branch branch, List<BranchVerificationConf> selectedBranchVerificationConfs) throws NotFoundException {
        Branch b = read(branch.getId());
        branch.setGitRepositoryStatus(b.getGitRepositoryStatus());
        return update(branch, selectedBranchVerificationConfs);
    }

    public void create(Branch branch, List<BranchVerificationConf> selectedBranchVerificationConfs) {
        create(branch);
        branch.setVerificationConfs(new ArrayList<BranchVerificationConf>());
        attachVerificationConfs(branch, selectedBranchVerificationConfs);
    }

    private void attachVerificationConfs(Branch branch, List<BranchVerificationConf> selectedBranchVerificationConfs) {
        for (BranchVerificationConf selected : selectedBranchVerificationConfs) {
            RelationUtil.relate(branch, selected);
        }
    }
}
