package com.nokia.ci.ejb;

import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.AbstractCustomParam;
import com.nokia.ci.ejb.model.AbstractCustomVerification;
import com.nokia.ci.ejb.model.AbstractCustomVerificationConf;
import com.nokia.ci.ejb.model.AbstractTemplateVerificationConf;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.BranchVerificationConf;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildCustomParameter;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildGroupCIServer;
import com.nokia.ci.ejb.model.BuildGroupCustomParameter;
import com.nokia.ci.ejb.model.BuildGroup_;
import com.nokia.ci.ejb.model.BuildInputParam;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.CIServer;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.CustomVerificationConf;
import com.nokia.ci.ejb.model.CustomVerificationParam;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.InputParam;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobAnnouncement;
import com.nokia.ci.ejb.model.JobCustomVerification;
import com.nokia.ci.ejb.model.JobCustomParameter;
import com.nokia.ci.ejb.model.JobPostVerification;
import com.nokia.ci.ejb.model.JobPrePostVerification;
import com.nokia.ci.ejb.model.JobPreVerification;
import com.nokia.ci.ejb.model.JobTriggerType;
import com.nokia.ci.ejb.model.JobVerificationConf;
import com.nokia.ci.ejb.model.Job_;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.ReportAction;
import com.nokia.ci.ejb.model.ReportAction_;
import com.nokia.ci.ejb.model.ResultDetailsParam;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.StatusTriggerPattern;
import com.nokia.ci.ejb.model.FileTriggerPattern;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.Template;
import com.nokia.ci.ejb.model.TemplateCustomVerification;
import com.nokia.ci.ejb.model.TemplateCustomVerificationConf;
import com.nokia.ci.ejb.model.TemplateVerificationConf;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationCardinality;
import com.nokia.ci.ejb.model.AbstractVerificationConf;
import com.nokia.ci.ejb.reportaction.ReportActionStatus;
import com.nokia.ci.ejb.model.TestResultType;
import com.nokia.ci.ejb.model.UserFile;
import com.nokia.ci.ejb.util.ConsistencyValidator;
import com.nokia.ci.ejb.util.Order;
import com.nokia.ci.ejb.util.RelationUtil;
import com.nokia.ci.ejb.util.VerificationConfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link Job} object operations.
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
public class JobEJB extends SecurityFunctionality<Job> implements Serializable, BuildGroupLoader {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(JobEJB.class);
    private final static int MAX_RELATED_FILES_AS_PARAM = 10;
    @EJB
    BuildEJB buildEJB;
    @EJB
    CIServerEJB ciServerEJB;
    @EJB
    BuildGroupCIServerEJB buildGroupCIServerEJB;
    @EJB
    ProductEJB productEJB;
    @EJB
    VerificationEJB verificationEJB;
    @EJB
    BranchEJB branchEJB;
    @EJB
    ProjectEJB projectEJB;
    @EJB
    SysUserEJB sysUserEJB;
    @EJB
    BuildGroupEJB buildGroupEJB;
    @EJB
    BuildVerificationConfEJB bvcEJB;
    @EJB
    JobCustomVerificationEJB jobCustomVerificationEJB;
    @EJB
    BuildCustomParameterEJB buildCustomParameterEJB;
    @EJB
    BuildInputParamEJB buildInputParamEJB;
    @EJB
    ChangeEJB changeEJB;
    @EJB
    TemplateEJB templateEJB;
    @Resource
    SessionContext ctx;

    public JobEJB() {
        super(Job.class);
    }

    public void checkAdminRights(Long id, SysUser user) throws BackendAppException {
        if (user.getUserRole() == RoleType.SYSTEM_ADMIN) {
            return;
        }

        if (user.getUserRole() == RoleType.USER) {
            throw new UnauthorizedException("Not project admin!");
        }

        Job j = read(id);
        List<SysUser> admins = projectEJB.getAdmins(j.getBranch().getProject().getId());
        for (SysUser u : admins) {
            if (u.getId().equals(user.getId())) {
                return;
            }
        }

        throw new UnauthorizedException("Not project admin!");
    }

    public void create(Job j, Long branchId, Long sysUserId)
            throws NotFoundException {

        log.info("creating job with id:{} for branch with id:{} for user with id:{}",
                new Object[]{j.getId(), branchId, sysUserId});

        Branch b = branchEJB.read(branchId);
        SysUser su = sysUserEJB.read(sysUserId);
        create(j);
        j.setProjectId(b.getProject().getId());
        RelationUtil.relate(b, j);
        RelationUtil.relate(su, j);
    }

    public void delete(Job j, Long sysUserId) throws UnauthorizedException,
            NotFoundException {
        log.info("deleting job with id:{} with user with id:{}", new Object[]{
            j.getId(), sysUserId});

        Job job = read(j.getId());
        SysUser sysUser = em.find(SysUser.class, sysUserId);

        checkPermissions(job, sysUser);

        if (job.getBranch() != null) {
            RelationUtil.unrelate(job.getBranch(), job);
        }

        delete(j);
    }

    public Job updateWithoutRelations(Job j) throws NotFoundException {
        log.info("updating job with id:{}", j.getId());

        Job oldJob = read(j.getId());

        oldJob.setName(j.getName());
        oldJob.setDisplayName(j.getDisplayName());

        return oldJob;
    }

    //TODO: remove updateFromUI method when git info is separated into another table
    public Job updateFromUI(Job job) throws NotFoundException {
        Job j = read(job.getId());
        job.setLastRun(j.getLastRun());
        job.setLastCronCheck(j.getLastCronCheck());
        job.setLastFetchHead(j.getLastFetchHead());
        job.setLastSuccesfullFetchHead(j.getLastSuccesfullFetchHead());
        return update(job);
    }

    /**
     * Gets list of jobs by given trigger type.
     *
     * @param triggerType Type of trigger of job
     * @return list of jobs
     */
    public List<Job> getJobsByTriggerType(JobTriggerType triggerType) {
        log.debug("Finding jobs with trigger type {}", triggerType);
        CriteriaQuery<Job> query = cb.createQuery(Job.class);
        Root<Job> job = query.from(Job.class);
        query.where(cb.equal(job.get(Job_.triggerType), triggerType));
        List<Job> jobs = em.createQuery(query).getResultList();
        log.debug("Found {} jobs by trigger type {}", jobs.size(), triggerType);
        return jobs;
    }

    public List<Job> getJobsWithName(String name) {
        log.debug("Finding jobs where name={}", name);
        Query query =
                em.createQuery("select j from Job j where j.name=:name");
        query.setParameter("name", name);
        List<Job> jobs = query.getResultList();

        log.debug("Found {} jobs", jobs.size());
        return jobs;
    }

    public Job getJobWithName(String name) throws NotFoundException {
        log.debug("Finding job with name={}", name);

        List<Job> jobsWithName = getJobsWithName(name);
        if (jobsWithName.isEmpty()) {
            throw new NotFoundException("Job " + name + " not Found");
        }

        return jobsWithName.get(0);
    }

    public List<Job> getUnassignedJobs() {
        log.debug("Finding jobs where branch is null");

        CriteriaQuery<Job> query = cb.createQuery(Job.class);
        Root<Job> job = query.from(Job.class);
        query.where(job.get(Job_.branch).isNull());
        List<Job> jobs = em.createQuery(query).getResultList();

        return jobs;
    }

    public BuildGroup retrigger(Long id, String gerritRefSpec, String gerritPatchSetRevision,
            List<Change> changes)
            throws NotFoundException, JobStartException {
        log.info("Retriggering job {}", id);

        List<Change> newChanges = new ArrayList<Change>();

        for (Change c : changes) {
            Change newChange = new Change();
            newChange.setAuthorEmail(c.getAuthorEmail());
            newChange.setMessage(c.getMessage());
            newChange.setSubject(c.getSubject());
            newChange.setAuthorName(c.getAuthorName());
            newChange.setCommitId(c.getCommitId());
            newChange.setCommitTime(c.getCommitTime());
            newChange.setProjectId(c.getProjectId());
            newChange.setStatus(c.getStatus());
            newChanges.add(newChange);
        }

        return start(id, gerritRefSpec, gerritPatchSetRevision, newChanges);
    }

    public BuildGroup start(Long id, String gerritRefSpec, String gerritPatchsetRevision)
            throws NotFoundException, JobStartException {
        return start(id, gerritRefSpec, gerritPatchsetRevision, null);
    }

    public BuildGroup start(Long id, String gerritRefSpec, String gerritPatchsetRevision,
            List<Change> changes)
            throws NotFoundException, JobStartException {
        // check if this job is disabled
        Job job = read(id);
        log.info("Starting job {}", job);
        if (job.getDisabled() != null && job.getDisabled().booleanValue()) {
            log.info("Job {} has been disabled and cannot be started.", job);
            return null;
        }

        // set last fetch head for job.
        job.setLastFetchHead(gerritPatchsetRevision);

        // create builds.
        BuildGroup buildGroup = null;
        try {
            log.info("Creating builds for job {}", job);
            buildGroup = createBuilds(id, gerritRefSpec, gerritPatchsetRevision, changes);
            log.info("Builds created for job {}", job);
        } catch (NotFoundException ex) {
            // something went wrong in creation. rollback all changes...
            ctx.setRollbackOnly();

            // ..and rethrow.
            throw ex;
        } catch (JobStartException ex) {
            // something went wrong in creation. rollback all changes...
            ctx.setRollbackOnly();

            // ..and rethrow.
            throw ex;
        }

        // we need to flush entity manager to update build id from database.
        // this is to be removed in future when build start has queue.
        em.flush();

        // construct parameters.
        Map<String, String> parameters = getCreatorParams(buildGroup);

        // run creator.
        try {
            buildGroupCIServerEJB.build(buildGroup.getBuildGroupCIServer(),
                    "ci20_job_creator", parameters);
        } catch (JobStartException ex) {
            // something went wrong in startup. rollback all changes...
            ctx.setRollbackOnly();

            // ..and rethrow.
            throw ex;
        }

        return buildGroup;
    }

    /**
     * Returns list of enabled verification configurations. Configuration is
     * enabled only if it is selected in project level and branch level.
     *
     * @param id Job id
     * @return List of {@link JobVerificationConf}
     * @throws NotFoundException If job not found with given id
     */
    public List<JobVerificationConf> getEnabledJobVerificationConfs(Long id) throws NotFoundException {
        Job job = read(id);

        List<? extends AbstractVerificationConf> enabledVerifications = getEnabledVerificationConfs(job);
        
        List<JobVerificationConf> validConfs = getValidJobVerificationConfs(job, enabledVerifications);

        return validConfs;
    }

    private List<? extends AbstractVerificationConf> getEnabledVerificationConfs(Job job) {
        List<? extends AbstractVerificationConf> enabledVerifications = new ArrayList<AbstractVerificationConf>();
        Branch branch = job.getBranch();
        if (branch == null) {
            log.warn("Branch is null for {}", job);
            return enabledVerifications;
        }
    
        Project project = branch.getProject();
        if (project == null) {
            log.warn("Project is null for {}", branch);
            return enabledVerifications;
        }
        List<ProjectVerificationConf> projectVerificationConfs = project.getVerificationConfs();
        List<BranchVerificationConf> branchVerificationConfs = branch.getVerificationConfs();
        enabledVerifications = VerificationConfUtil.getEnabledConfs(projectVerificationConfs, branchVerificationConfs);

        return enabledVerifications;
    }

    private List<JobVerificationConf> getValidJobVerificationConfs(Job job, List<? extends AbstractVerificationConf> enabledVerifications) {
        List<JobVerificationConf> validConfs = new ArrayList<JobVerificationConf>();
        if (enabledVerifications == null || enabledVerifications.isEmpty()) {
            return validConfs;
        }

        for (JobVerificationConf jobConf : job.getVerificationConfs()) {
            if (VerificationConfUtil.isCombinationSelected(enabledVerifications,
                    jobConf.getProduct(), jobConf.getVerification())) {
                validConfs.add(jobConf);
            }
        }

        return validConfs;
    }

    public List<? extends AbstractTemplateVerificationConf> getValidJobAndTemplateVerificaitonConfs(Long id) throws NotFoundException {
        Job job = read(id);

        List<? extends AbstractVerificationConf> enabledVerifications = getEnabledVerificationConfs(job);

        List<AbstractTemplateVerificationConf> validConfs = new ArrayList<AbstractTemplateVerificationConf>();
        validConfs.addAll(getValidJobVerificationConfs(job, enabledVerifications));
        
        if (enabledVerifications == null || enabledVerifications.isEmpty()) {
            return validConfs;
        }
        
        Branch branch = job.getBranch();
        Template template = branch.getTemplate();
        if (template == null) {
            return validConfs;
        }

        List<TemplateVerificationConf> templateConfs = templateEJB.getVerificationConfs(template.getId());
        
        if (templateConfs != null && !templateConfs.isEmpty()) {
            for (AbstractTemplateVerificationConf conf : templateConfs) {
                if (!VerificationConfUtil.isCombinationSelected(enabledVerifications,
                        conf.getProduct(), conf.getVerification())) {
                    // Template conf not enabled in project/branch level
                    continue;
                }
                if (VerificationConfUtil.isCombinationSelected(validConfs,
                        conf.getProduct(), conf.getVerification())) {
                    // Template conf is already selected in job level
                    continue;
                }
                validConfs.add(conf);
            }
        }
        
        return validConfs;
    }
    
    /**
     * Returns flat list of enabled {@link CustomVerificationConf} objects for
     * given job. Configuration is enabled only if it is selected in project
     * level.
     *
     * @param job Given job
     * @return List of {@link CustomVerificationConf}
     */
    public List<CustomVerificationConf> getEnabledCustomVerificationConfs(Long id) throws NotFoundException {
        Job job = read(id);

        List<CustomVerificationConf> customConfs = new ArrayList<CustomVerificationConf>();
        if (job == null) {
            log.warn("Given job is null!");
            return customConfs;
        }

        for (JobCustomVerification customVerification : job.getCustomVerifications()) {
            customConfs.addAll(jobCustomVerificationEJB.getEnabledCustomVerificationConfs(customVerification.getId()));
        }
        return customConfs;
    }

    public List<TemplateCustomVerificationConf> getEnabledTemplateCustomVerificationConfs(Long id) throws NotFoundException {
        Job job = read(id);
        List<TemplateCustomVerificationConf> customConfs = new ArrayList<TemplateCustomVerificationConf>();
        if (job == null) {
            log.warn("Given job is null!");
            return customConfs;
        }
        Branch branch = job.getBranch();
        if (branch == null) {
            log.warn("Branch is null for {}", job);
            return customConfs;
        }
        Project project = branch.getProject();
        if (project == null) {
            log.warn("Project is null for {}", job);
            return customConfs;
        }

        Template template = branch.getTemplate();
        if (template == null) {
            log.warn("Template is null for {}", job);
            return customConfs;
        }
        List<ProjectVerificationConf> projectVerificationConfs = project.getVerificationConfs();
        List<BranchVerificationConf> branchVerificationConfs = branch.getVerificationConfs();
        List<? extends AbstractVerificationConf> enabledVerifications = VerificationConfUtil.getEnabledConfs(projectVerificationConfs, branchVerificationConfs);
        for (TemplateCustomVerification customVerification : templateEJB.getCustomVerifications(template.getId())) {
            for (TemplateCustomVerificationConf customConf : customVerification.getCustomVerificationConfs()) {
                if (VerificationConfUtil.isCombinationSelected(enabledVerifications,
                        customConf.getProduct(), customVerification.getVerification())) {
                    customConfs.add(customConf);
                }
            }
        }
        return customConfs;
    }
    
    /**
     * Returns all custom verification configurations for job.
     *
     * @param id Job id
     * @return list of {@link JobVerificationConf} objects
     * @throws NotFoundException If job not found with given id.
     */
    public List<CustomVerificationConf> getCustomVerificationConfs(Long id) throws NotFoundException {
        Job job = read(id);

        List<CustomVerificationConf> customConfs = new ArrayList<CustomVerificationConf>();
        if (job == null) {
            log.warn("Given job is null!");
            return customConfs;
        }

        for (JobCustomVerification customVerification : job.getCustomVerifications()) {
            customConfs.addAll(customVerification.getCustomVerificationConfs());
        }
        return customConfs;
    }

    /**
     * Returns all verification configurations for job.
     *
     * @param id Job id
     * @return list of {@link JobVerificationConf} objects
     */
    public List<JobVerificationConf> getVerificationConfs(Long id) {
        return getJoinList(id, Job_.verificationConfs);
    }

    /**
     * Returns all custom verifications for job.
     *
     * @param id Job id
     * @return list of {@link JobCustomVerification} objects
     */
    public List<JobCustomVerification> getCustomVerifications(Long id) {
        return getJoinList(id, Job_.customVerifications);
    }

    public List<Verification> getPreVerifications(Long id) throws NotFoundException {
        Job job = read(id);
        return getJobPreVerifications(job);
    }

    public List<Verification> getPostVerifications(Long id) throws NotFoundException {
        Job job = read(id);
        return getJobPostVerifications(job);
    }

    /**
     * Returns all announcement for job.
     *
     * @param id Job id
     * @return list of {@link JobAnnouncement} objects
     */
    public List<JobAnnouncement> getJobAnnouncements(Long id) {
        return getJoinList(id, Job_.announcements);
    }

    /**
     * Saves given verification configurations for the job. Whole verification
     * configurations collection is replaced with given collection.
     *
     * @param id Job id
     * @param confs List of job verification configurations.
     * @return Updated job.
     * @throws NotFoundException If job not found with given id.
     */
    public Job saveVerificationConfs(Long id, List<JobVerificationConf> confs) throws NotFoundException {

        Job j = read(id);

        List<JobVerificationConf> toBeDeletedConfs = new ArrayList<JobVerificationConf>();

        for (JobVerificationConf jobVerificationConf : j.getVerificationConfs()) {
            if (!jobVerificationExists(confs, jobVerificationConf)) {
                em.remove(jobVerificationConf);
                toBeDeletedConfs.add(jobVerificationConf);
            }
        }

        j.getVerificationConfs().removeAll(toBeDeletedConfs);

        for (JobVerificationConf jvc : confs) {
            if (!jobVerificationExists(j.getVerificationConfs(), jvc)) {
                RelationUtil.relate(j, jvc);
            } else {
                jobVerificationFindUpdate(j, jvc);
            }
        }
        j.setModified(new Date());
        j.setModifiedBy(getCallerUsername());

        return em.merge(j);
    }

    protected void jobVerificationFindUpdate(Job job, JobVerificationConf targetJvc) {

        for (JobVerificationConf jobVerificationConf : job.getVerificationConfs()) {
            if (jobVerificationConfsMatch(jobVerificationConf, targetJvc)
                    && jobVerificationConf.getCardinality() != targetJvc.getCardinality()) {
                jobVerificationConf.setCardinality(targetJvc.getCardinality());
            }
        }
    }

    protected boolean jobVerificationExists(List<JobVerificationConf> jobVerificationConfs, JobVerificationConf targetJvc) {

        for (JobVerificationConf jobVerificationConf : jobVerificationConfs) {
            if (jobVerificationConfsMatch(jobVerificationConf, targetJvc)) {
                return true;
            }
        }

        return false;
    }

    protected boolean jobVerificationConfsMatch(JobVerificationConf confA, JobVerificationConf confB) {

        try {
            if (confA.getProduct().equals(confB.getProduct()) && confA.getVerification().equals(confB.getVerification())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.warn("Exception in jobVerificationConfsMatch. {}", e.getMessage() + e.getStackTrace());
            return false;
        }
    }

    public Job savePreVerifications(Long id, List<Verification> verifications) throws NotFoundException {
        Job j = read(id);
        List<JobPreVerification> preVerifications = new ArrayList();
        preVerifications.addAll(j.getPreVerifications());

        for (JobPreVerification v : preVerifications) {
            RelationUtil.unrelate(j, v);
            em.remove(v);
        }
        j.getPreVerifications().clear();

        Long ordinality = 1L;
        for (Verification v : verifications) {
            JobPreVerification pre = new JobPreVerification();
            pre.setVerificationOrdinality(ordinality);
            ordinality++;
            RelationUtil.relate(pre, v);
            RelationUtil.relate(j, pre);
        }

        j.setModified(new Date());
        j.setModifiedBy(getCallerUsername());

        return em.merge(j);
    }

    public Job savePostVerifications(Long id, List<Verification> verifications) throws NotFoundException {
        Job j = read(id);
        List<JobPostVerification> postVerifications = new ArrayList();
        postVerifications.addAll(j.getPostVerifications());

        for (JobPostVerification v : postVerifications) {
            RelationUtil.unrelate(j, v);
            em.remove(v);
        }
        j.getPostVerifications().clear();

        Long ordinality = 1L;
        for (Verification v : verifications) {
            JobPostVerification pre = new JobPostVerification();
            pre.setVerificationOrdinality(ordinality);
            ordinality++;
            RelationUtil.relate(pre, v);
            RelationUtil.relate(j, pre);
        }

        j.setModified(new Date());
        j.setModifiedBy(getCallerUsername());

        return em.merge(j);
    }

    public void setOwner(Long id, SysUser owner) throws NotFoundException {
        Job job = read(id);

        if (job.getOwner() != null) {
            RelationUtil.unrelate(job.getOwner(), job);
        }

        if (owner != null) {
            SysUser attachedOwner = em.find(SysUser.class, owner.getId());
            RelationUtil.relate(attachedOwner, job);
        }
    }

    public Gerrit getGerrit(Long id) throws NotFoundException {
        Job job = read(id);

        Branch branch = job.getBranch();
        if (branch == null) {
            throw new NotFoundException("branch not found for job:"
                    + job.getId());
        }

        Project project = branch.getProject();
        if (project == null) {
            throw new NotFoundException("project not found for branch:"
                    + branch.getId());
        }

        Gerrit gerrit = project.getGerrit();
        if (gerrit == null) {
            throw new NotFoundException("gerrit not found for project:"
                    + project.getId());
        }

        return gerrit;
    }

    /**
     * Get all report actions for job.
     *
     * @param id Job id
     * @return List of report actions.
     */
    public List<ReportAction> getReportActions(Long id) {
        return getJoinList(id, Job_.reportActions);
    }

    /**
     * Get report actions for job where status is equal with given report action
     * status.
     *
     * @param id Job id
     * @param status {@link ReportActionStatus} value
     * @return List of report actions with given status.
     */
    public List<ReportAction> getReportActions(Long id, ReportActionStatus status) {
        CriteriaQuery<ReportAction> query = cb.createQuery(ReportAction.class);
        Root<Job> job = query.from(Job.class);
        ListJoin<Job, ReportAction> reportActions = job.join(Job_.reportActions);
        query.select(reportActions);
        query.where(cb.and(cb.equal(job, id),
                cb.equal(reportActions.get(ReportAction_.status), status)));
        List<ReportAction> resultList = em.createQuery(query).getResultList();
        return resultList;
    }

    public List<BuildGroup> getLastBuildGroups(Long id, int size) {
        CriteriaQuery<BuildGroup> criteria = cb.createQuery(BuildGroup.class);
        Root<BuildGroup> buildGroup = criteria.from(BuildGroup.class);
        criteria.select(buildGroup);
        Predicate predicate = cb.equal(buildGroup.get(BuildGroup_.job), id);
        criteria.where(predicate);
        criteria.orderBy(cb.desc(buildGroup.get(BuildGroup_.startTime)));
        TypedQuery<BuildGroup> emQuery = em.createQuery(criteria);
        emQuery.setMaxResults(size);

        List<BuildGroup> resultList = emQuery.getResultList();
        log.debug("Found {} buildGroups", resultList.size());
        return resultList;
    }

    /**
     * Gets paged build groups for job. Results are ordered by startTime field
     * of build groups.
     *
     * @param id Job id
     * @param first Index of first result in result set. Starts from 0.
     * @param pageSize Maximum number of returned results.
     * @param order Direction of ordering. Default: ASC
     * @return List of build groups
     */
    public List<BuildGroup> getBuildGroups(Long id, int first, int pageSize, Order order) {
        return getBuildGroups(id, first, pageSize, null, order);
    }

    /**
     * Gets paged build groups for job.
     *
     * @param id Job id
     * @param first Index of first result in result set. Starts from 0.
     * @param pageSize Size of returned results
     * @param orderField Build group field to order results. Default: startTime
     * @param order Direction of ordering
     * @return List of build groups
     */
    @Override
    public List<BuildGroup> getBuildGroups(Long id, int first, int pageSize, String orderField, Order order) {
        if (first < 0 || pageSize < 1) {
            return null;
        }

        if (order == null) {
            order = Order.ASC;
        }

        if (StringUtils.isEmpty(orderField)) {
            orderField = "startTime";
        }

        if (log.isDebugEnabled()) {
            log.debug("Finding buildGroups where jobId={}, first={}, pageSize={}, orderField={}, order={}",
                    new Object[]{id, first, pageSize, orderField, order.getSql()});
        }

        CriteriaQuery<BuildGroup> criteria = cb.createQuery(BuildGroup.class);
        Root<BuildGroup> buildGroup = criteria.from(BuildGroup.class);
        criteria.select(buildGroup);
        Predicate predicate = cb.and(cb.equal(buildGroup.get(BuildGroup_.job), id), buildGroupEJB.getSecurityPredicate(buildGroup));
        criteria.where(predicate);
        if (order == Order.ASC) {
            criteria.orderBy(cb.asc(buildGroup.get(orderField)));
        } else {
            criteria.orderBy(cb.desc(buildGroup.get(orderField)));
        }
        TypedQuery<BuildGroup> emQuery = em.createQuery(criteria);
        emQuery.setFirstResult(first);
        emQuery.setMaxResults(pageSize);

        List<BuildGroup> resultList = emQuery.getResultList();
        log.debug("Found {} buildGroups", resultList.size());
        return resultList;
    }

    /**
     * Gets previous build group for this job by given build group id.
     * {@code NULL} is returned if no previous build group found.
     *
     * @param id Job id
     * @param buildGroupId Current build group id
     * @return Previous build group
     */
    public BuildGroup getPreviousBuildGroup(Long id, Long buildGroupId) {
        try {
            CriteriaQuery<BuildGroup> criteriaQuery = cb.createQuery(BuildGroup.class);
            Root<BuildGroup> buildGroup = criteriaQuery.from(BuildGroup.class);
            Predicate jobEqual = cb.equal(buildGroup.get(BuildGroup_.job), id);
            Predicate buildGroupIdLessThan = cb.lessThan(buildGroup.get(BuildGroup_.id), buildGroupId);
            criteriaQuery.where(cb.and(jobEqual, buildGroupIdLessThan))
                    .orderBy(cb.desc(buildGroup.get(BuildGroup_.id)));
            return em.createQuery(criteriaQuery).setMaxResults(1).getSingleResult();
        } catch (NoResultException nre) {
            log.debug("There was no previous build group results {}", nre.getMessage());
        }

        return null;
    }

    /**
     * Counts build groups for job.
     *
     * @param id Job id
     * @return number of build groups for given job
     */
    @Override
    public Long getBuildGroupCount(Long id) {
        log.debug("Counting all build groups for job with id {}", id);
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<BuildGroup> buildGroup = criteria.from(BuildGroup.class);

        criteria.select(cb.count(buildGroup));
        criteria.where(cb.and(cb.equal(buildGroup.get(BuildGroup_.job), id)), buildGroupEJB.getSecurityPredicate(buildGroup));
        Long count = em.createQuery(criteria).getSingleResult();

        log.debug(
                "Found {} build groups", count);
        return count;
    }

    public Job copyJob(Long jobId, SysUser user, String jobDisplayName) throws NotFoundException,
            IllegalArgumentException {
        log.info("Copying job {}", jobId);
        Job originalJob = read(jobId);

        if (originalJob.getBranch() == null) {
            throw new NotFoundException("Job {} does not have branch" + originalJob.getId());
        }

        if (originalJob.getBranch().getType() != BranchType.TOOLBOX && originalJob.getBranch().getType() != BranchType.DRAFT) {
            throw new IllegalArgumentException("Trying to copy non-toolbox,non-draft job {}" + originalJob.getId());
        }

        if (jobDisplayName == null) {
            jobDisplayName = originalJob.getDisplayName();
        }

        Job newJob = new Job();
        newJob.setDisplayName(jobDisplayName);
        newJob.setName(originalJob.getName());
        newJob.setProjectId(originalJob.getProjectId());
        create(newJob, originalJob.getBranch().getId(), user.getId());

        // Copy verification configs
        List<JobVerificationConf> verificationConfs = originalJob.getVerificationConfs();
        for (JobVerificationConf conf : verificationConfs) {
            JobVerificationConf newConf = new JobVerificationConf();
            newConf.setProduct(conf.getProduct());
            newConf.setVerification(conf.getVerification());
            newConf.setCardinality(conf.getCardinality());
            newConf.setImeiCode(conf.getImeiCode());
            newConf.setTasUrl(conf.getTasUrl());
            RelationUtil.relate(newJob, newConf);
        }

        // Copy custom verifications
        List<JobCustomVerification> customVerifications = originalJob.getCustomVerifications();
        for (JobCustomVerification verification : customVerifications) {
            JobCustomVerification newVerification = new JobCustomVerification();
            newVerification.setDescription(verification.getDescription());
            newVerification.setVerification(verification.getVerification());

            List<CustomVerificationConf> customConfs = verification.getCustomVerificationConfs();
            for (CustomVerificationConf conf : customConfs) {
                CustomVerificationConf newConf = new CustomVerificationConf();
                newConf.setProduct(conf.getProduct());
                newConf.setCardinality(conf.getCardinality());
                newConf.setImeiCode(conf.getImeiCode());
                newConf.setTasUrl(conf.getTasUrl());
                RelationUtil.relate(newVerification, newConf);
            }

            List<CustomVerificationParam> customParams = verification.getCustomVerificationParams();
            for (CustomVerificationParam param : customParams) {
                CustomVerificationParam newParam = new CustomVerificationParam();
                newParam.setCustomParam(param.getCustomParam());
                newParam.setParamValue(param.getParamValue());
                RelationUtil.relate(newVerification, newParam);
            }

            RelationUtil.relate(newJob, newVerification);
        }

        // Copy pre and post verifications
        List<JobPreVerification> preVerifications = originalJob.getPreVerifications();
        for (JobPreVerification pre : preVerifications) {
            JobPreVerification newPre = new JobPreVerification();
            newPre.setVerification(pre.getVerification());
            newPre.setVerificationOrdinality(pre.getVerificationOrdinality());
            RelationUtil.relate(newJob, newPre);
        }

        List<JobPostVerification> postVerifications = originalJob.getPostVerifications();
        for (JobPostVerification post : postVerifications) {
            JobPreVerification newPost = new JobPreVerification();
            newPost.setVerification(post.getVerification());
            newPost.setVerificationOrdinality(post.getVerificationOrdinality());
            RelationUtil.relate(newJob, newPost);
        }

        return newJob;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateAtomicLastRun(Long id) throws NotFoundException {
        Job job = read(id);
        job.setLastRun(new Date());
        em.flush();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateAtomicLastCronCheck(Long id) throws NotFoundException {
        Job job = read(id);
        job.setLastCronCheck(new Date());
        em.flush();
    }
    
    /**
     * Create buildgroup with builds according to given configuration.
     *
     * @param id
     * @param gerritRefSpec
     * @param gerritPatchsetRevision
     * @param changes
     * @return
     * @throws NotFoundException
     */
    protected BuildGroup createBuilds(Long id, String gerritRefSpec,
            String gerritPatchsetRevision, List<Change> changes)
            throws NotFoundException, JobStartException {

        // Load job
        Job job = read(id);

        // Validate.
        ConsistencyValidator.validate(job);

        // Resolve gerrit
        Gerrit gerrit = job.getBranch().getProject().getGerrit();
        // Create build group.
        BuildGroup buildGroup = createBuildGroup(job, gerritRefSpec, gerritPatchsetRevision, gerrit);

        // relate changes to build group
        if (changes != null) {
            for (Change c : changes) {
                RelationUtil.relate(buildGroup, c);
            }
        }

        // Create pre-build queue
        log.info("Creating pre-verification builds for job {}.", job);
        Build lastBuildOfPreVerifications = null;
        List<Verification> preVerifications = getJobPreVerifications(job);
        List<Build> preBuilds = createBuildQueue(buildGroup, preVerifications,
                VerificationCardinality.MANDATORY, null, null);
        if (preBuilds.size() > 0) {
            lastBuildOfPreVerifications = preBuilds.get(preBuilds.size() - 1);
            preBuilds.get(0).setStartNode(true);
        }

        // process job verification configurations.
        List<? extends AbstractTemplateVerificationConf> jvcs = getValidJobAndTemplateVerificaitonConfs(job.getId());

        Map<Verification, Map<Product, Build>> verificationProductMap =
                new HashMap<Verification, Map<Product, Build>>();

        log.info("Creating normal builds for job {}.", job);
        // Create a set of tailing builds. This list can be used to
        // insert post build steps.
        Set<Build> tailingBuilds = new HashSet<Build>();
        for (AbstractTemplateVerificationConf jvc : jvcs) {
            // create build chain.
            Build build = createBuildChain(buildGroup, null, lastBuildOfPreVerifications, jvc.getVerification(),
                    jvc.getProduct(), verificationProductMap, false, jvc.getCardinality(), jvc.getImeiCode(), jvc.getTasUrl(), jvc.getUserFiles());

            tailingBuilds.add(build);
        }

        // process custom verification configurations.
        List<AbstractCustomVerificationConf> cvcs = new ArrayList<AbstractCustomVerificationConf>();
        // add custom verification configurations specified in job 
        cvcs.addAll(getEnabledCustomVerificationConfs(job.getId()));
        // add custom verification configurations specified in template
        cvcs.addAll(getEnabledTemplateCustomVerificationConfs(job.getId()));

        log.info("Creating custom verification builds for job {}.", job);
        for (AbstractCustomVerificationConf cvc : cvcs) {
            // retrieve required data.
            AbstractCustomVerification jcv = cvc.getCustomVerification();
            Verification verification = jcv.getVerification();
            Product product = cvc.getProduct();

            // create build chain with verification confs.
            Build build = createBuildChain(buildGroup, null, lastBuildOfPreVerifications, verification,
                    product, verificationProductMap, true, cvc.getCardinality(), cvc.getImeiCode(), cvc.getTasUrl(), cvc.getUserFiles());

            // create build custom parameters for build verification conf.
            createBuildCustomParameters(build.getBuildVerificationConf(),
                    jcv.getAbstractCustomParams());
            tailingBuilds.add(build);
        }

        List<Verification> postVerifications = getJobPostVerifications(job);
        log.info("Creating post-verification builds for job {}.", job);
        List<Build> postBuilds = createBuildQueue(buildGroup, postVerifications,
                VerificationCardinality.MANDATORY, null, null);
        if (!postBuilds.isEmpty()) {
            Build firstPost = postBuilds.get(0);
            for (Build tailingBuild : tailingBuilds) {
                /* Do not connect builds that have childs within list. These
                 builds are user selections, but other user selections are configured
                 to depend from these builds. Therefore these builds are not actually
                 "tailing" from build chain perspective.*/
                if (!tailingBuild.getChildBuilds().isEmpty()) {
                    continue;
                }

                RelationUtil.relate(tailingBuild, firstPost);
            }
        }


        // Resolve ciserver.
        List<CIServer> ciServers = job.getBranch().getCiServers();

        CIServer ciServer = ciServerEJB.resolveCIServer(ciServers, buildGroup.getBuilds());

        if (ciServer == null) {
            log.warn("No CI Servers available for job {} to start building!", job);
            throw new JobStartException("No CI Servers available for build!");
        }

        // Create snapshot of ci server for build group
        BuildGroupCIServer buildGroupCIServer = new BuildGroupCIServer();
        buildGroupCIServer.setUrl(ciServer.getUrl());
        buildGroupCIServer.setCiServerUuid(ciServer.getUuid());
        buildGroupCIServer.setPort(ciServer.getPort());
        buildGroupCIServer.setUsername(ciServer.getUsername());
        buildGroupCIServer.setPassword(ciServer.getPassword());
        buildGroupCIServer.setTestResultStorage(ciServer.getTestResultStorage());
        buildGroupCIServer.setProxyServerUrl(ciServer.getProxyServerUrl());
        RelationUtil.relate(buildGroupCIServer, buildGroup);

        ciServerEJB.startBuild(ciServer.getId());

        return buildGroup;
    }

    /**
     * Create build structure for a single verification.
     *
     * This is a recursive method that creates all builds that are required for
     * given verification (verification dependency).
     *
     * Recursion is started from required verification and continued upwards the
     * dependency tree from bottom to up.
     *
     * todo: when custom verification can depend from other custom
     * verification(s) this method needs to be refractored to support
     * verification-product-customparameters-build caching. this can be
     * implemented with wrapper class that contains hashcode and equals methods.
     * currently custom verificatins are specified to depend only from
     * non-custom verifications and is implemented as specified.
     *
     * @param imeiCode
     * @param tasUrl
     */
    private Build createBuildChain(BuildGroup buildGroup, Build childBuild,
            Build lastBuildOfPreVerifications, Verification verification, Product product,
            Map<Verification, Map<Product, Build>> verificationProductMap,
            boolean isCustomVerification, VerificationCardinality cardinality, String imeiCode, String tasUrl, List<UserFile> userFiles) throws NotFoundException {

        /**
         * Verification-Product-Build combinations are stored two maps to
         * prevent creation of multiple builds for a single verification and
         * product combination. Recursion might reach same verification and
         * product combination multiple times via separate paths (e.g.
         * verification has two parents and those parents share a common
         * grandparent).
         *
         * if build for combination is already found, then also all of its
         * parent paths are already fulfilled so it is ok to exit after we have
         * set relation to our child build.
         *
         * custom verifications are not stored in map based cache.
         */
        if (verificationProductMap == null) {
            verificationProductMap = new HashMap<Verification, Map<Product, Build>>();
        }

        // retrieve product-build map for verification.
        Map<Product, Build> productBuildMap = verificationProductMap.get(
                verification);

        Build build = null;

        /*
         * custom verifications are not stored in verification-product-build
         * cache.
         */
        if (!isCustomVerification && productBuildMap != null) {
            build = productBuildMap.get(product);
        }

        if (build != null) {
            /**
             * Verification along with it's parents is already processed via
             * other previously completed dependency path.
             */
            if (childBuild != null) {
                RelationUtil.relate(build, childBuild);
            }

            if (childBuild == null && cardinality == VerificationCardinality.MANDATORY) {
                /**
                 * This code block is ran when user selected verification +
                 * product combination is already found from build chain. In
                 * this case childBuild is null.
                 */
                build.getBuildVerificationConf().setCardinality(VerificationCardinality.MANDATORY);
                setParentCardinalityRecursively(build, VerificationCardinality.MANDATORY);
            } else if (childBuild != null && childBuild.getBuildVerificationConf().getCardinality() == VerificationCardinality.MANDATORY
                    && build.getBuildVerificationConf().getCardinality() == VerificationCardinality.OPTIONAL) {
                /**
                 * This code block is ran when dependency for user selection is
                 * already found from build chain.
                 */
                setParentCardinalityRecursively(childBuild, VerificationCardinality.MANDATORY);
            }

            return build;
        }

        // create build for verification
        build = createBuild(buildGroup, verification, product, cardinality, imeiCode, tasUrl, userFiles);

        if (childBuild != null) {
            RelationUtil.relate(build, childBuild);
        }

        /*
         * custom verifications are not stored in verification-product-build
         * cache.
         */
        if (!isCustomVerification) {
            // create product-build map if not existing.
            if (productBuildMap == null) {
                productBuildMap = new HashMap<Product, Build>();
                verificationProductMap.put(verification, productBuildMap);
            }

            /*
             * insert build for product. now we have searchable
             * verification-product-build combination stored. it can be used in
             * following iterations for path resolving.
             */
            productBuildMap.put(product, build);
        }

        // loop parents for current verification if any
        for (Verification parentVerification : verification.getParentVerifications()) {
            createBuildChain(buildGroup, build, lastBuildOfPreVerifications, parentVerification, product,
                    verificationProductMap, false, cardinality, imeiCode, tasUrl, userFiles);
        }

        /**
         * if no parents then we have reached the top most verification.
         */
        if (verification.getParentVerifications().isEmpty()) {
            if (lastBuildOfPreVerifications != null) {
                // There is pre-verifications relate to last of them.
                RelationUtil.relate(lastBuildOfPreVerifications, build);
            } else {
                //There is no pre-verifications in chain, so mark this build as start node
                build.setStartNode(true);
            }
        }

        // done.
        return build;
    }

    private void checkPermissions(Job job, SysUser sysUser)
            throws UnauthorizedException {

        if (sysUser.getUserRole() == RoleType.SYSTEM_ADMIN) {
            // allow for system admin regardless of owner.
            return;
        }

        SysUser jobOwner = job.getOwner();

        if (jobOwner == null || !jobOwner.getId().equals(sysUser.getId())) {
            throw new UnauthorizedException("Operation not allowed for user "
                    + sysUser);
        }
    }

    private List<Verification> getJobPreVerifications(Job job) {
        List<JobPreVerification> verifications = job.getPreVerifications();
        List<Verification> preVerifications = new ArrayList<Verification>();
        for (JobPrePostVerification v : verifications) {
            preVerifications.add(v.getVerification());
        }
        return preVerifications;
    }

    private List<Verification> getJobPostVerifications(Job job) {
        List<JobPostVerification> verifications = job.getPostVerifications();
        List<Verification> postVerifications = new ArrayList<Verification>();
        for (JobPrePostVerification v : verifications) {
            postVerifications.add(v.getVerification());
        }
        return postVerifications;
    }

    private Build createBuild(BuildGroup buildGroup, Verification verification, Product product,
            VerificationCardinality cardinality, String imeiCode, String tasUrl, List<UserFile> userFiles) throws NotFoundException {

        Build build = new Build();
        build.setPhase(BuildPhase.CONFIGURED);
        build.setStatus(BuildStatus.SUCCESS);
        RelationUtil.relate(buildGroup, build);

        // Create build verification configuration for build
        createBuildVerificationConf(build, verification, product, cardinality, imeiCode, tasUrl, userFiles);

        // set name for build job
        build.setJobName(createCiJobName(build));

        // set display name for build job
        build.setJobDisplayName(build.getJobName());
        return build;
    }

    private void setParentCardinalityRecursively(Build build,
            VerificationCardinality cardinality) {

        Set<Build> parentBuilds = new HashSet<Build>();
        collectParentBuildsRecursively(build, parentBuilds);
        for (Build b : parentBuilds) {
            b.getBuildVerificationConf().setCardinality(cardinality);
        }
    }

    private Set<Build> collectParentBuildsRecursively(Build childBuild, Set<Build> parentBuilds) {
        for (Build parentBuild : childBuild.getParentBuilds()) {
            parentBuilds.add(parentBuild);
            if (!parentBuild.getParentBuilds().isEmpty()) {
                collectParentBuildsRecursively(parentBuild, parentBuilds);
            }
        }

        log.debug("Found recursively {} parent builds for build {}",
                parentBuilds.size(), childBuild);
        return parentBuilds;
    }

    private List<Build> createBuildQueue(BuildGroup buildGroup, List<Verification> verifications,
            VerificationCardinality cardinality, String imeiCode, String tasUrl) throws NotFoundException {

        List<Build> ret = new ArrayList<Build>();
        Build parent = null;
        for (Verification v : verifications) {
            Build b = createBuild(buildGroup, v, null, cardinality, imeiCode, tasUrl, null);
            if (parent != null) {
                RelationUtil.relate(parent, b);
            }
            ret.add(b);
            parent = b;
        }
        return ret;
    }

    private Map<String, String> getCreatorParams(BuildGroup buildGroup) {
        Map<String, String> parameters = new HashMap<String, String>();

        // Jobs.
        List<Build> childBuilds = buildGroup.getBuilds();
        String jobsValue = getCreatorJobsParam(childBuilds);
        parameters.put(CIParam.JOBS.toString(), jobsValue);

        // Build group id.
        if (buildGroup.getId() != null) {
            parameters.put(CIParam.BUILD_GROUP_ID.toString(), buildGroup.getId().toString());
        }

        return parameters;
    }

    private String getCreatorJobsParam(List<Build> builds) {
        // use set to remove duplicate values.
        Set<String> jobNames = new HashSet<String>();
        for (Build build : builds) {
            jobNames.add(build.getJobName());
        }

        // construct parameter string.
        StringBuilder sb = new StringBuilder();
        Iterator i = jobNames.iterator();
        while (i.hasNext()) {
            if (sb.length() != 0) {
                sb.append(',');
            }

            sb.append(i.next());
        }
        return sb.toString();
    }

    private BuildVerificationConf createBuildVerificationConf(Build build,
            Verification verification, Product product, VerificationCardinality cardinality,
            String imeiCode, String tasUrl, List<UserFile> userFiles) {

        BuildVerificationConf bvc = new BuildVerificationConf();
        RelationUtil.relate(build, bvc);

        bvc.setCardinality(cardinality);
        if (product != null) {

            if (product.getUuid() == null || product.getUuid().isEmpty()) {
                product.setUuid(UUID.randomUUID().toString());
            }
            bvc.setProductName(product.getName());
            bvc.setProductDisplayName(product.getDisplayName());
            bvc.setProductRmCode(product.getRmCode());
            bvc.setProductUuid(product.getUuid());
            bvc.setImeiCode(imeiCode);
            bvc.setTasUrl(tasUrl);
        }
        if (verification != null) {

            if (verification.getUuid() == null || verification.getUuid().isEmpty()) {
                verification.setUuid(UUID.randomUUID().toString());
            }

            Set<String> labelNames = new HashSet<String>();

            for (SlaveLabel label : verification.getSlaveLabels()) {
                labelNames.add(label.getName());
            }
            bvc.setLabelNames(labelNames);

            bvc.setVerificationName(verification.getName());
            bvc.setVerificationDisplayName(verification.getDisplayName());
            bvc.setParentStatusThreshold(verification.getParentStatusThreshold());
            bvc.setTestResultIndexFile(verification.getTestResultIndexFile());
            bvc.setVerificationType(verification.getType());
            bvc.setVerificationTargetPlatform(verification.getTargetPlatform());
            bvc.setVerificationUuid(verification.getUuid());

            Set<TestResultType> testResultTypes = new HashSet();
            testResultTypes.addAll(verification.getTestResultTypes());
            bvc.setTestResultTypes(testResultTypes);

            for (InputParam inputParam : verification.getInputParams()) {
                BuildInputParam buildInputParam = new BuildInputParam();
                RelationUtil.relate(bvc, buildInputParam);
                buildInputParam.setParamKey(inputParam.getParamKey());
                buildInputParam.setParamValue(inputParam.getParamValue());
            }

            for (ResultDetailsParam resultDetailsParam : verification.getResultDetailsParams()) {
                BuildResultDetailsParam buildResultDetailsParam = new BuildResultDetailsParam();
                RelationUtil.relate(bvc, buildResultDetailsParam);
                buildResultDetailsParam.setParamKey(resultDetailsParam.getParamKey());
                buildResultDetailsParam.setParamValue(resultDetailsParam.getParamValue());
                buildResultDetailsParam.setDisplayName(resultDetailsParam.getDisplayName());
                buildResultDetailsParam.setDescription(resultDetailsParam.getDescription());
            }
        }

        if (userFiles != null && userFiles.size() > 0) {
            //Considering possible 8K HTTP GET length, put limitation on the length of user file string as jenkins injected parameters.
            int upperLimit = userFiles.size() < MAX_RELATED_FILES_AS_PARAM ? userFiles.size() : MAX_RELATED_FILES_AS_PARAM;

            StringBuilder userFilesStrBuilder = new StringBuilder("");
            for (int i = 0; i < upperLimit; i++) {
                userFilesStrBuilder.append(userFiles.get(i).getUuid());
                if (i < upperLimit - 1) {
                    userFilesStrBuilder.append(",");
                }
            }
            bvc.setUserFiles(userFilesStrBuilder.toString());
        }

        return bvc;
    }

    private void createBuildCustomParameters(BuildVerificationConf bvc,
            List<? extends AbstractCustomParam> jvcs) {

        for (AbstractCustomParam cvp : jvcs) {
            BuildCustomParameter bcp = new BuildCustomParameter();
            RelationUtil.relate(bvc, bcp);
            bcp.setParamKey(cvp.getCustomParam().getParamKey());
            bcp.setParamValue(cvp.getParamValue());
        }
    }

    private String createCiJobName(Build build) throws NotFoundException {
        /*
         * SPEC: project---verification.
         *
         * Example:
         *
         * myproject---myverification
         */

        // validate consistency.
        ConsistencyValidator.validate(build);

        // get project name.
        String projectName = build.getBuildGroup().getJob().getBranch().getProject().getName();

        // get verification string.
        BuildVerificationConf bvc = build.getBuildVerificationConf();
        if (bvc == null) {
            throw new NotFoundException("Build verification configuration not found for build"
                    + build.getId());
        }
        String verificationName = build.getBuildVerificationConf().getVerificationName();

        // construct and ci job name.
        StringBuilder jobNameBuilder = new StringBuilder();
        jobNameBuilder.append(projectName).append("---").append(verificationName);

        // done.
        return jobNameBuilder.toString();
    }

    private BuildGroup createBuildGroup(Job job, String gerritRefSpec, String gerritPatchsetRevision, Gerrit gerrit) {
        BuildGroup buildGroup = new BuildGroup();
        buildGroup.setPhase(BuildPhase.STARTED);
        buildGroup.setStatus(BuildStatus.SUCCESS);
        buildGroup.setProjectId(job.getBranch().getProject().getId());
        buildGroup.setJobName(job.getName());
        buildGroup.setJobDisplayName(job.getDisplayName());
        buildGroup.setStartTime(new Date());
        buildGroup.setGerritRefSpec(gerritRefSpec);
        buildGroup.setGerritUrl(gerrit.getUrl());
        buildGroup.setGerritPatchSetRevision(gerritPatchsetRevision);
        buildGroup.setGerritBranch(job.getBranch().getName());
        buildGroup.setGerritProject(job.getBranch().getProject().getGerritProject());
        buildGroup.setGroupUid(UUID.randomUUID().toString());
        buildGroup.setBranchType(job.getBranch().getType());
        RelationUtil.relate(job, buildGroup);

        for (JobCustomParameter jcp : job.getCustomParameters()) {
            if (StringUtils.isEmpty(jcp.getParamKey()) || StringUtils.isEmpty(jcp.getParamValue())) {
                log.info("job {} has configuration error in custom parameter list", job);
                continue;
            }

            BuildGroupCustomParameter bgcp = new BuildGroupCustomParameter();
            bgcp.setParamKey(jcp.getParamKey());
            bgcp.setParamValue(jcp.getParamValue());
            RelationUtil.relate(buildGroup, bgcp);
        }

        buildGroupEJB.create(buildGroup);
        return buildGroup;
    }

    public List<StatusTriggerPattern> getStatusTriggerPatterns(Long id) {
        return getJoinList(id, Job_.statusTriggerPatterns);
    }

    public Job saveStatusTriggerPatterns(Long id, List<StatusTriggerPattern> patterns) throws NotFoundException {
        Job job = read(id);
        List<StatusTriggerPattern> oldPatterns = new ArrayList();
        oldPatterns.addAll(job.getStatusTriggerPatterns());

        for (StatusTriggerPattern oldPattern : oldPatterns) {
            RelationUtil.unrelate(job, oldPattern);
            em.remove(oldPattern);
        }
        job.getStatusTriggerPatterns().clear();

        for (StatusTriggerPattern newPattern : patterns) {
            RelationUtil.relate(job, newPattern);
        }

        job.setModified(new Date());
        job.setModifiedBy(getCallerUsername());

        return em.merge(job);
    }

    public List<FileTriggerPattern> getFileTriggerPatterns(Long id) {
        return getJoinList(id, Job_.fileTriggerPatterns);
    }

    public Job saveFileTriggerPatterns(Long id, List<FileTriggerPattern> patterns) throws NotFoundException {
        Job job = read(id);
        List<FileTriggerPattern> oldPatterns = new ArrayList();
        oldPatterns.addAll(job.getFileTriggerPatterns());

        for (FileTriggerPattern oldPattern : oldPatterns) {
            RelationUtil.unrelate(job, oldPattern);
            em.remove(oldPattern);
        }
        job.getFileTriggerPatterns().clear();

        for (FileTriggerPattern newPattern : patterns) {
            RelationUtil.relate(job, newPattern);
        }

        job.setModified(new Date());
        job.setModifiedBy(getCallerUsername());

        return em.merge(job);
    }

    public void saveCustomParameters(Long id, List<JobCustomParameter> parameters) throws NotFoundException {
        Job job = read(id);

        // Remove old global parameters.
        List<JobCustomParameter> oldParameters = new ArrayList<JobCustomParameter>();
        oldParameters.addAll(job.getCustomParameters());
        for (JobCustomParameter oldParameter : oldParameters) {
            RelationUtil.unrelate(job, oldParameter);
            em.remove(oldParameter);
        }

        job.getCustomParameters().clear();

        // Add new.
        for (JobCustomParameter newParameter : parameters) {
            newParameter.setId(null);
            RelationUtil.relate(job, newParameter);
        }
    }

    public List<JobCustomParameter> getCustomParameters(Long id) {
        return getJoinList(id, Job_.customParameters);
    }

    public JobVerificationConf findVerificationConf(Long jobId, Long verificationId, Long productId) throws NotFoundException {

        List<JobVerificationConf> verificationConfs = getEnabledJobVerificationConfs(jobId);

        for (JobVerificationConf verificationConf : verificationConfs) {
            if (verificationConf.getProduct().getId().longValue() == productId.longValue()
                    && verificationConf.getVerification().getId().longValue() == verificationId.longValue()) {
                return verificationConf;
            }
        }
        return null;
    }

    public CustomVerificationConf findCustomVerificationConf(Long jobId, Long verificationId, Long productId) throws NotFoundException {

        List<CustomVerificationConf> customVerificationConfs = getEnabledCustomVerificationConfs(jobId);

        for (CustomVerificationConf customVerificationConf : customVerificationConfs) {
            if (customVerificationConf.getProduct().getId().longValue() == productId.longValue()
                    && customVerificationConf.getCustomVerification().getId().longValue() == verificationId.longValue()) {
                return customVerificationConf;
            }
        }
        return null;
    }
}
