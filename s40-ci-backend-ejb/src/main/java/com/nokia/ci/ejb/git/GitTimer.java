package com.nokia.ci.ejb.git;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.ChangeEJB;
import com.nokia.ci.ejb.FileTriggerPatternEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.event.GitFetchFailedEvent;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.hasingleton.AbstractHASingletonTimer;
import com.nokia.ci.ejb.jms.JobStartProducer;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchByLastGitOperation;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeFile;
import com.nokia.ci.ejb.model.ChangeStatus;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.GitRepositoryStatus;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.RepositoryType;
import com.nokia.ci.ejb.model.SysConfigKey;
import org.apache.commons.lang3.StringUtils;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.quartz.CronExpression;

@Singleton
@TransactionTimeout(value = 1, unit = TimeUnit.HOURS)
public class GitTimer extends AbstractHASingletonTimer {

    private static final long TIMER_TIMEOUT_DEFAULT = 60 * 1000;
    private static final long DEFAULT_GIT_ASYNC_REPO_UPDATE_TIMEOUT = 60 * 60 * 1000;
    @Resource
    TimerService timerService;
    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private BranchEJB branchEJB;
    @EJB
    private ChangeEJB changeEJB;
    @EJB
    private JobEJB jobEJB;
    @EJB
    private FileTriggerPatternEJB fileTriggerPatternEJB;
    @EJB
    private AsyncGitCloner cloner;
    @EJB
    private AsyncGitFetcher fetcher;
    @EJB
    private JobStartProducer jobStartProducer;
    @Inject
    @GitFetchFailedEvent
    private Event<Long> gitFetchFailedEvents;
    private String gitRootPath = null;
    private Map<Branch, RepositoryUpdateInformation> repositories = new HashMap<Branch, RepositoryUpdateInformation>();
    private long timeout;
    private final int DEFAULT_GIT_FETCH_LOAD = 1;
    private final int DEFAULT_GIT_CLONE_LOAD = 10;
    private final int DEFAULT_GIT_MAX_LOAD = 47;
    private int gitFetchLoad = DEFAULT_GIT_FETCH_LOAD;
    private int gitCloneLoad = DEFAULT_GIT_CLONE_LOAD;
    private int gitMaxLoad = DEFAULT_GIT_MAX_LOAD;

    @Override
    protected void initTimer() {
        timeout = sysConfigEJB.getValue(SysConfigKey.GIT_TIMER_TIMEOUT, TIMER_TIMEOUT_DEFAULT);
        TimerConfig timerConfig = new TimerConfig("Git Timer", false);
        log.info("Creating single action timer with timeout {}", timeout);
        initPath();
        timer = timerService.createSingleActionTimer(timeout, timerConfig);
    }

    @PreDestroy
    @Override
    protected void destroy() {
        super.destroy();
    }

    @Override
    @Timeout
    protected void task() {
        log.info("**** Git timer task triggered ****");
        long startTime = System.currentTimeMillis();
        try {
            handleRepositoryUpdateInformations();
            refreshGitRepositories();

        } catch (Exception ex) {
            log.error("Error in Git Timer!", ex);
        } finally {
            initTimer();
        }
        log.info("**** Git timer task done in {}ms ****", System.currentTimeMillis() - startTime);
    }

    private void initPath() {
        try {
            gitRootPath = sysConfigEJB.getSysConfig(SysConfigKey.GIT_ROOT_DIRECTORY).getConfigValue();
        } catch (NotFoundException ex) {
            log.warn("Using default values in git timer! Insert {} into SYS_CONFIG", SysConfigKey.GIT_ROOT_DIRECTORY, ex);
            return;
        }
        File path = new File(gitRootPath);
        if (!path.canWrite()) {
            log.error("Provided path({}) is invalid. Make sure that jboss has permission to write into that directory.", gitRootPath);
        }
    }

    private Branch getBranch(RepositoryUpdateInformation rui) {
        Branch branch = null;

        try {
            branch = branchEJB.read(rui.getBranchId());
        } catch (NotFoundException nfe) {
            log.error("Cannot find the branch with id {}! Cause: {}", rui.getBranchId(), nfe.getMessage());
            //TODO: Do some clean up when branch is deleted from DB. For example deleting local repository.
        }

        return branch;
    }

    private void handleRepositoryUpdateInformations() {
        Iterator<RepositoryUpdateInformation> it = repositories.values().iterator();
        while (it.hasNext()) {
            RepositoryUpdateInformation rui = it.next();
            Future<String> action = rui.getAction();
            if (!action.isDone()) {
                if (System.currentTimeMillis() - rui.getStartTime() > sysConfigEJB.getValue(SysConfigKey.GIT_ASYNC_REPO_UPDATE_TIMEOUT,
                        DEFAULT_GIT_ASYNC_REPO_UPDATE_TIMEOUT)) {
                    action.cancel(true);
                    log.error("Async operation timeout: {}", rui.getLocalPath());
                    Branch branch = getBranch(rui);
                    if (branch != null) {
                        unsuccessfulHeadFetching(branch);
                    }
                    it.remove();
                }
                continue;
            }

            String fetchHead;
            try {
                fetchHead = action.get();
            } catch (Throwable t) {
                //TODO: Proper exception handling and mapping. Maybe clean up local repository?
                log.error("Async operation returned exception", t);
                it.remove();
                continue;
            }

            Branch branch = getBranch(rui);
            if (branch == null) {
                // TODO: Do some clean up when branch is deleted from DB. For example deleting local repository
                it.remove();
                continue;
            }

            if (StringUtils.isEmpty(fetchHead)) {
                it.remove();
                unsuccessfulHeadFetching(branch);
                continue;
            }

            if (branch.getGitRepositoryStatus() != GitRepositoryStatus.UNINITIALIZED) {
                branch.setGitRepositoryStatus(GitRepositoryStatus.INITIALIZED);
            }
            branch.setGitFailureCounter(null);
            startJobs(rui, fetchHead, branch.getName());
            it.remove();

        }
    }

    private void unsuccessfulHeadFetching(Branch branch) {
        int currentCounterValue = branch.getGitFailureCounter() == null ? 0 : branch.getGitFailureCounter();
        branch.setGitFailureCounter(currentCounterValue + 1);

        log.info("Git fetch failure counter for branch id: {} was increment to {}", branch.getId(), branch.getGitFailureCounter());

        branch.setGitRepositoryStatus(GitRepositoryStatus.UNINITIALIZED);
        gitFetchFailedEvents.fire(branch.getId());
    }

    private void refreshGitRepositories() {
        // Update used setups.
        gitMaxLoad = sysConfigEJB.getValue(SysConfigKey.GIT_MAX_LOAD, DEFAULT_GIT_MAX_LOAD);
        gitFetchLoad = sysConfigEJB.getValue(SysConfigKey.GIT_FETCH_LOAD, DEFAULT_GIT_FETCH_LOAD);
        gitCloneLoad = sysConfigEJB.getValue(SysConfigKey.GIT_CLONE_LOAD, DEFAULT_GIT_CLONE_LOAD);

        // start.
        List<BranchType> branchTypesUsingGit = BranchType.getBranchTypes(RepositoryType.GIT);
        List<Branch> branches = branchEJB.getBranches(branchTypesUsingGit);
        Collections.sort(branches, new BranchByLastGitOperation());
        for (Branch branch : branches) {
            if (StringUtils.isEmpty(branch.getGitRepositoryPath())) {
                log.warn("Branch {} does not have git repository path configured!", branch);
                continue;
            }

            Project project = branch.getProject();
            if (project == null) {
                log.warn("Branch {} does not have project! Skipping...", branch);
                continue;
            }
            Gerrit gerrit = project.getGerrit();
            if (gerrit == null) {
                log.warn("Project {} does not have gerrit! Skipping...", project);
                continue;
            }

            String repositoryURL = generateRepositoryURL(project);
            String localPath = generateLocalPath(branch);

            if (repositories.containsKey(branch)) {
                // Future is already started.
                continue;
            }

            // Determine next status.
            GitRepositoryStatus nextStatus = null;
            if (branch.getGitRepositoryStatus() == GitRepositoryStatus.INITIALIZED
                    || branch.getGitRepositoryStatus() == GitRepositoryStatus.FETCHING) {
                nextStatus = GitRepositoryStatus.FETCHING;
            } else if (branch.getGitRepositoryStatus() == GitRepositoryStatus.UNINITIALIZED
                    || branch.getGitRepositoryStatus() == GitRepositoryStatus.CLONING) {
                nextStatus = GitRepositoryStatus.CLONING;
            } else {
                log.warn("Unknown git repository status for branch {}", branch);
                continue;
            }

            // Determine if there is CPU for required git operation.
            if (!cpuAvailable(nextStatus)) {
                log.info("GIT load estimated to be too high for git operation {}. Operation delayed for branch: {}.", nextStatus, branch);
                continue;
            }

            // Proceed to next status.
            Future<String> action = null;
            if (nextStatus == GitRepositoryStatus.FETCHING) {
                action = fetcher.fetch(repositoryURL, localPath, branch.getName());
            } else if (nextStatus == GitRepositoryStatus.CLONING) {
                action = cloner.clone(repositoryURL, localPath, branch.getName(), gerrit.getSshUserName());
            } else {
                log.error("error when handling git repositories: nextStatus == null");
                continue;
            }

            branch.setGitRepositoryStatus(nextStatus);
            branch.setLastGitOperationStarted(new Date());
            addRepositoryUpdateInformationToMap(branch, localPath, repositoryURL, action);
        }
    }

    /**
     * Creates refspec for job starting.
     *
     * @param branchName Name of branch
     * @return refspec string
     */
    private String createRefSpec(String branchName) {
        StringBuilder sb = new StringBuilder();
        sb.append("+refs/heads/");
        sb.append(branchName);
        sb.append(":refs/remotes/origin/");
        sb.append(branchName);
        return sb.toString();
    }

    /*
     * creates repository path for repository to be cloned,
     * also used in as a key of hashmap
     */
    private String generateRepositoryURL(Project project) {
        Gerrit gerrit = project.getGerrit();
        return GitUtils.buildGitFullURL(gerrit.getSshUserName(), gerrit.getUrl(), gerrit.getPort(), project.getGerritProject());
    }

    private void startJobs(RepositoryUpdateInformation rui, String fetchHead, String branchName) {
        // Start periodic jobs
        List<Job> periodicJobsToTrigger = branchEJB.getPeriodicJobsToTrigger(rui.getBranchId());
        for (Job job : periodicJobsToTrigger) {
            if (job.getPollInterval() == null || job.getPollInterval() <= 0) {
                log.warn("Job {} polling interval value {} is invalid! Skipping job start.", job, job.getPollInterval());
                continue;
            }

            Calendar nextRun = null;
            if (job.getLastRun() != null) {
                nextRun = Calendar.getInstance();
                nextRun.setTime(job.getLastRun());
                nextRun.add(Calendar.MINUTE, job.getPollInterval());
            }

            if (nextRun != null && nextRun.getTime().after(new Date())) {
                // It is not time to run this job.
                continue;
            }

            log.info("Triggering periodic job with poll interval value {}, last run was {}", job.getPollInterval(), job.getLastRun());
            triggerJob(job, rui, fetchHead, branchName);
        }

        // Start scheduled jobs
        List<Job> scheduledJobsToTrigger = branchEJB.getScheduledJobsToTrigger(rui.getBranchId());
        for (Job job : scheduledJobsToTrigger) {
            if (StringUtils.isEmpty(job.getCronExpression())) {
                log.warn("Job {} schedule cron expression value '{}' is invalid! Skipping job start.", job, job.getCronExpression());
                continue;
            }

            try {
                CronExpression cron = new CronExpression(job.getCronExpression());
                TimeZone tz = TimeZone.getDefault();
                if (!StringUtils.isEmpty(job.getCronTimezone())) {
                    tz = TimeZone.getTimeZone(job.getCronTimezone());
                    cron.setTimeZone(tz);
                }

                GregorianCalendar nextRun = null;
                if (job.getLastRun() != null) {
                    nextRun = new GregorianCalendar(tz);
                    Date d;
                    if (job.getLastCronCheck() != null && job.getLastCronCheck().after(job.getLastRun())) {
                        d = cron.getNextValidTimeAfter(job.getLastCronCheck());
                    } else {
                        d = cron.getNextValidTimeAfter(job.getLastRun());
                    }
                    nextRun.setTimeInMillis(d.getTime());
                }

                GregorianCalendar currentDate = new GregorianCalendar(tz);

                if (nextRun != null && nextRun.after(currentDate)) {
                    // It is not time to run this job.
                    continue;
                }

                // Update last cron checking time to prevent triggering builds after branch HEAD state has already been checked once
                try {
                    jobEJB.updateAtomicLastCronCheck(job.getId());
                } catch (NotFoundException e) {
                    log.warn("No job found for id: {}. Could not update last cron check time");
                }
                log.info("Triggering scheduled job with cron value '{}', last run was {}", job.getCronExpression(), job.getLastRun());
                triggerJob(job, rui, fetchHead, branchName);
            } catch (ParseException e) {
                log.warn("Job {} schedule cron expression value '{}' is invalid! Skipping job start.", job, job.getCronExpression());
                continue;
            }
        }
    }

    private void triggerJob(Job job, RepositoryUpdateInformation rui, String fetchHead, String branchName) {
        log.info("Triggering job {} from Git timer", job);
        try {
            List<Change> changes = new ArrayList<Change>();

            if (!StringUtils.isEmpty(job.getLastFetchHead())) {
                // Job has previous fetch head.
                if (job.getLastFetchHead().equals(fetchHead)) {
                    // There is no new fetch head. Do not start jobs.
                    return;
                }

                String from = job.getLastSuccesfullFetchHead();
                if (StringUtils.isEmpty(from)) {
                    from = job.getLastFetchHead();
                }

                Git git = Git.init().setDirectory(new File(rui.getLocalPath())).call();
                Iterable<RevCommit> commits = GitUtils.getCommits(git, from, fetchHead);
                changes = getChanges(git, job, commits);
                git = null;
                Collections.sort(changes);
            }

            //File based triggering check
            if (!jobEJB.getFileTriggerPatterns(job.getId()).isEmpty()) {
                boolean matched = fileTriggerPatternEJB.checkFilePathMatch(job.getId(), changes);
                if (!matched) {
                    // There is no matched file path. Do not start jobs.
                    log.info("Could not match any trigger file path, not starting job {}", job);
                    return;
                }
            }

            jobStartProducer.sendJobStart(job.getId(), createRefSpec(branchName), fetchHead, changes);
            log.info("Job {} sent to start queue successfully", job);
        } catch (Exception ex) {
            log.error("Job start failed for job {}!", job, ex);
        }
    }

    private List<Change> getChanges(Git git, Job j, Iterable<RevCommit> commits) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, GitAPIException, IOException {
        ArrayList<Change> changes = new ArrayList<Change>();
        for (RevCommit revCommit : commits) {
            Change change = new Change();
            change.setProjectId(j.getBranch().getProject().getId());
            change.setAuthorName(revCommit.getCommitterIdent().getName());
            change.setAuthorEmail(revCommit.getCommitterIdent().getEmailAddress());
            change.setCommitTime(new Date(((long) revCommit.getCommitTime()) * 1000));
            change.setCommitId(revCommit.getId().getName());
            change.setSubject(revCommit.getShortMessage());
            change.setMessage(revCommit.getFullMessage());
            change.setStatus(ChangeStatus.MERGED);

            RevCommit[] parents = revCommit.getParents();
            if (parents != null && parents.length > 0) {
                List<Change> parentChanges = new ArrayList<Change>();
                for (RevCommit parent : parents) {
                    Change parentChange = null;
                    try {
                        parentChange = changeEJB.getChangeByCommitId(parent.getId().getName());
                    } catch (NotFoundException ex) {
                        continue;
                    }
                    parentChanges.add(parentChange);
                }
                change.setParentChanges(parentChanges);
            }

            List<ChangeFile> changeFiles = GitUtils.getChangeFiles(git, revCommit, change);

            change.setChangeFiles(changeFiles);
            changes.add(change);
        }
        return changes;
    }

    private void addRepositoryUpdateInformationToMap(Branch branch, String localPath, String repositoryURL, Future<String> action) {
        RepositoryUpdateInformation repositoryUpdateInformation = new RepositoryUpdateInformation();
        repositoryUpdateInformation.setBranchId(branch.getId());
        repositoryUpdateInformation.setLocalPath(localPath);
        repositoryUpdateInformation.setURL(repositoryURL);
        repositoryUpdateInformation.setAction(action);
        repositoryUpdateInformation.setStartTime(System.currentTimeMillis());
        repositories.put(branch, repositoryUpdateInformation);
    }

    private String generateLocalPath(Branch branch) {
        StringBuilder sb = new StringBuilder();
        if (gitRootPath != null) {
            sb.append(gitRootPath);
        }
        sb.append("/")
                .append(branch.getGitRepositoryPath())
                .append("/")
                .append(branch.getId())
                .append("/");
        return sb.toString();
    }

    private int getCurrentLoad() {
        Set<Branch> branches = repositories.keySet();
        int currentLoad = 0;
        for (Branch b : branches) {
            if (b.getGitRepositoryStatus() == GitRepositoryStatus.CLONING) {
                currentLoad += gitCloneLoad;
            } else if (b.getGitRepositoryStatus() == GitRepositoryStatus.FETCHING) {
                currentLoad += gitFetchLoad;
            }
        }

        return currentLoad;
    }

    private boolean cpuAvailable(GitRepositoryStatus requestedStatus) {

        int requestedLoad = 0;

        if (requestedStatus == GitRepositoryStatus.CLONING) {
            requestedLoad = gitCloneLoad;
        } else if (requestedStatus == GitRepositoryStatus.FETCHING) {
            requestedLoad = gitFetchLoad;
        }

        int currentLoad = getCurrentLoad();

        log.info("current gif load: {}. Requesting {} units more", currentLoad, requestedLoad);

        if ((currentLoad + requestedLoad) > gitMaxLoad) {
            log.info("denied request for git load: {}", requestedLoad);
            return false;
        }

        log.info("approved request for git load: {}", requestedLoad);
        return true;
    }
}
