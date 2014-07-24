package com.nokia.ci.ejb;

import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.ejb.event.BuildStartedEvent;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.mail.MailSenderEJB;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildCustomParameter;
import com.nokia.ci.ejb.model.BuildEvent;
import com.nokia.ci.ejb.model.BuildFailure;
import com.nokia.ci.ejb.model.BuildFailureReason_;
import com.nokia.ci.ejb.model.BuildFailure_;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildGroupCIServer;
import com.nokia.ci.ejb.model.BuildGroupCustomParameter;
import com.nokia.ci.ejb.model.BuildInputParam;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.Build_;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.MemConsumption;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.TestCaseStat;
import com.nokia.ci.ejb.model.TestCoverage;
import com.nokia.ci.ejb.model.TestResultType;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationCardinality;
import com.nokia.ci.ejb.model.VerificationFailureReason;
import com.nokia.ci.ejb.model.VerificationFailureReasonSeverity;
import com.nokia.ci.ejb.model.VerificationType;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link Build} object operations.
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
public class BuildEJB extends CrudFunctionality<Build> implements Serializable {

    private static final int DEFAULT_MAXIMUM_OWNER_EMAILS_PARAM_LENGTH = 2000;
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(BuildEJB.class);
    @EJB
    JobEJB jobEJB;
    @EJB
    BuildGroupCIServerEJB buildGroupCIServerEJB;
    @EJB
    GerritEJB gerritEJB;
    @EJB
    MailSenderEJB mailSenderEJB;
    @EJB
    SysConfigEJB sysConfigEJB;
    @EJB
    BuildGroupEJB buildGroupEJB;
    @EJB
    FinalizeBuildEJB finalizeBuildEJB;
    @EJB
    VerificationEJB verificationEJB;
    @Inject
    @BuildStartedEvent
    Event<Long> buildStartedEvent;

    public BuildEJB() {
        super(Build.class);
    }

    /**
     * Finds builds matching given refSpec string.
     *
     * @param refSpec given refSpec string
     * @return list of matching builds
     */
    public List<Build> getBuildsByRefSpec(String refSpec) {
        log.debug("Finding builds where refSpec={}", refSpec);
        CriteriaQuery<Build> criteria = cb.createQuery(Build.class);
        Root<Build> build = criteria.from(Build.class);
        criteria.where(cb.equal(build.get(Build_.refSpec), refSpec));
        List<Build> builds = em.createQuery(criteria).getResultList();
        log.debug("Found {} builds", builds.size());
        return builds;
    }

    public List<Build> getChildBuilds(Long id) throws NotFoundException {
        Build build = read(id);
        Set<Build> childBuilds = getChildBuilds(build);
        return new ArrayList(childBuilds);
    }

    public List<BuildFailure> getBuildFailures(Long id) throws NotFoundException {
        return getJoinList(id, Build_.buildFailures);
    }

    public List<BuildEvent> getBuildEvents(Long id) {
        List<BuildEvent> buildEvents = getJoinList(id, Build_.buildEvents);
        return buildEvents;
    }

    public void finalizeBuild(Long id, BuildStatus status) throws NotFoundException {
        Build build = read(id);
        finalizeBuild(build, status);
    }

    public void processOutputParameters(Long id, Map<String, String> inputParameters) throws NotFoundException {
        Build parentBuild = read(id);
        for (Build childBuild : parentBuild.getChildBuilds()) {
            offerInputParameters(childBuild, inputParameters);
        }
        processResultDetailsParameters(parentBuild, inputParameters);
    }

    public Build addBuildEvents(Long id, List<BuildEvent> buildEvents) throws NotFoundException {
        log.debug("Received {} build events for build {}", buildEvents.size(), id);
        long startTime = System.currentTimeMillis();

        Build build = read(id);

        // Add build events.
        for (BuildEvent buildEvent : buildEvents) {
            RelationUtil.relate(build, buildEvent);
        }

        log.debug("Finished adding build events for build {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return build;
    }

    public Build addBuildEvents(Long id, List<BuildEvent> buildEvents, String executor) throws NotFoundException {
        Build build = addBuildEvents(id, buildEvents);
        build.setExecutor(executor);
        return build;
    }

    public boolean isClassifiable(Long id) throws NotFoundException {
        Build build = read(id);
        BuildGroup bg = buildGroupEJB.read(build.getBuildGroup().getId());
        Job job = jobEJB.read(bg.getJob().getId());
        return isClassifiable(build, bg, job);
    }
    
    public boolean isClassifiable(Build build, BuildGroup bg, Job job) throws NotFoundException {
        if (build == null || bg == null || job == null) {
            log.error("Could not fetch classification data: build, buildGroup or job was null");
            return false;
        }

        // Only for SCV and DBV
        if (job == null || job.getBranch() == null) {
            return false;
        }

        if (job.getBranch().getType() != BranchType.DEVELOPMENT && job.getBranch().getType() != BranchType.SINGLE_COMMIT) {
            return false;
        }

        // Only for builds that are UNSTABLE
        if (build.getStatus() != BuildStatus.UNSTABLE) {
            return false;
        }

        BuildVerificationConf bvc = build.getBuildVerificationConf();
        if (bvc == null) {
            return false;
        }

        // Only for Optional verifications
        if (bvc.getCardinality() == VerificationCardinality.MANDATORY) {
            return false;
        }

        // Only for NJUnit
        Set<TestResultType> types = getTestResultTypes(build);
        if (!types.contains(TestResultType.NJUNIT)) {
            return false;
        }

        Verification verification = verificationEJB.getVerificationByUuid(bvc.getVerificationUuid());
        if (verification == null) {
            return false;
        }

        // If no failure reasons created for this verification
        List<VerificationFailureReason> reasons = verificationEJB.getFailureReasons(verification.getId());
        if (reasons.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Build is classified ok if it is classifiable, and all of its
     * BuildFailures have their severity set to NON_BLOCKING.
     *
     * @param id
     * @return
     * @throws NotFoundException
     */
    public boolean isClassifiedOk(Long id) throws NotFoundException {

        if (id == null) {
            return false;
        }
        if (!isClassifiable(id)) {
            return false;
        }

        List<BuildFailure> failures = getBuildFailures(id);
        if (failures == null || failures.isEmpty()) {
            return false;
        }

        for (BuildFailure failure : failures) {
            if (failure == null || failure.getFailureReason() == null
                    || failure.getFailureReason().getName() == null
                    || failure.getFailureReason().getSeverity() == null
                    || failure.getFailureReason().getSeverity() == VerificationFailureReasonSeverity.BLOCKING) {
                return false;
            }
        }
        return true;
    }

    public Set<TestResultType> getTestResultTypes(Build build) {
        // *** This is dirty hack because JPA returns Strings instead TestResultType enums
        // *** for some reason.
        Set<TestResultType> types = new HashSet<TestResultType>();
        Set bvc_types = build.getBuildVerificationConf().getTestResultTypes();
        for (Object o : bvc_types) {
            TestResultType t = EnumUtils.getEnum(TestResultType.class, o.toString());
            if (t != null) {
                types.add(t);
            }
        }
        return types;
    }

    public void start(Long id) throws NotFoundException {
        Build build = read(id);

        log.info("Starting {}", build);

        // Sanity checks for mandatory relations.
        BuildGroup buildGroup = build.getBuildGroup();
        if (buildGroup == null) {
            throw new NotFoundException("No build group found for build "
                    + build.getId());
        }

        BuildGroupCIServer buildGroupCIServer = buildGroup.getBuildGroupCIServer();
        if (buildGroupCIServer == null) {
            throw new NotFoundException("No server found for build "
                    + build.getId());
        }

        BuildVerificationConf bvc = build.getBuildVerificationConf();
        if (bvc == null) {
            throw new NotFoundException("No build verification conf found for build "
                    + build.getId());
        }

        // Startup checks.
        if (!isParentsFinished(build)) {
            log.info("Not starting build {}. reason: all parent builds are not finished.", build.getId());
            return;
        }

        if (!isParentThresholdMet(build)) {
            log.info("Not starting build {}. reason: parent status threshold exceeded.", build.getId());
            finalizeBuild(build, BuildStatus.NOT_BUILT);
            return;
        }

        // Starting.
        if (!run(build)) {
            log.error("Build startup failed for build {}", build.getId());
            finalizeBuild(build, BuildStatus.NOT_BUILT);
            return;
        }

        // Send event.
        buildStartedEvent.fire(build.getId());
    }

    private void finalizeBuild(Build build, BuildStatus status)
            throws NotFoundException {

        log.info("Finalizing build {} with status {}", build.getId(), status);

        // update build status and phase
        updateBuildStatus(build, status);
        
        // starts new transaction
        // this one includes buildGroupEJB.updateStatus()
        finalizeBuildEJB.finalizeBuildAtomic(build.getId(), status);

        // refresh the updated build group from DB
        try {
            em.refresh(build.getBuildGroup());
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Could not refresh build group with id: " + build.getBuildGroup().getId() + ", build group not found");
        }
        
        // Start child builds.
        for (Build childBuild : build.getChildBuilds()) {
            start(childBuild.getId());
        }
    }
    
    public void updateBuildStatus(Long id, BuildStatus status) throws NotFoundException {
        Build build = read(id);
        updateBuildStatus(build, status);
    }
    
    private void updateBuildStatus(Build build, BuildStatus status) {
        if (build.getStartTime() == null) {
            build.setStartTime(new Date());
        }

        // do not change if already set.
        if (build.getEndTime() == null) {
            build.setEndTime(new Date());
        }

        build.setStatus(status);
        build.setPhase(BuildPhase.FINISHED);    
    }

    private void offerInputParameters(Build build,
            Map<String, String> inputParameters) {

        BuildVerificationConf bvc = build.getBuildVerificationConf();

        if (bvc == null) {
            return;
        }

        for (BuildInputParam bip : bvc.getBuildInputParams()) {
            if (!inputParameters.containsKey(bip.getParamKey())) {
                continue;
            }

            // parameter found from build's parameter list. accept parameter.
            String value = inputParameters.get(bip.getParamKey());
            bip.setParamValue(value);

            // log.
            StringBuilder sb = new StringBuilder();
            sb.append("saved parameter ").append(bip.getParamKey());
            sb.append(" with value ").append(bip.getParamValue());
            sb.append(" for build ").append(build.getId());
            log.info(sb.toString());
        }
    }

    private void processResultDetailsParameters(Build build, Map<String, String> inputParameters) {

        BuildVerificationConf bvc = build.getBuildVerificationConf();

        if (bvc == null) {
            return;
        }

        for (BuildResultDetailsParam brdp : bvc.getBuildResultDetailsParams()) {
            // Check if received parameters contains pre-defined result details parameter
            if (!inputParameters.containsKey(brdp.getParamKey())) {
                continue;
            }

            // Parameter found from build's parameter list. Adding it for build group result params.
            String value = inputParameters.get(brdp.getParamKey());
            brdp.setParamValue(value);

            // log.
            StringBuilder sb = new StringBuilder();
            sb.append("saved result detail parameter ").append(brdp.getParamKey());
            sb.append(" with value ").append(brdp.getParamValue());
            sb.append(" for build ").append(build.getId());
            log.info(sb.toString());
        }
    }

    private Set<Build> getChildBuilds(Build parentBuild) {
        Set<Build> childBuilds = new HashSet<Build>();
        childBuilds.addAll(parentBuild.getChildBuilds());
        return childBuilds;
    }

    private boolean isParentsFinished(Build build) {
        for (Build parentBuild : build.getParentBuilds()) {
            if (parentBuild.getPhase() != BuildPhase.FINISHED) {
                log.info("Parent build {} of build {} not finished.",
                        parentBuild.getId(), build.getId());
                return false;
            }
        }

        return true;
    }

    private boolean isParentThresholdMet(Build build) {
        BuildVerificationConf bvc = build.getBuildVerificationConf();

        if (bvc.getParentStatusThreshold() == null) {
            return true;
        }

        // if build type was post build, check build group status
        // otherwise check build status of parent builds
        if (bvc.getVerificationType() == VerificationType.POST_BUILD) {
            if (build.getBuildGroup().getStatus().worstThan(bvc.getParentStatusThreshold())) {
                log.info("Parent status threshold exceeded for build {} by build group {}.",
                        build.getId(), build.getBuildGroup().getId());
                return false;
            }

            return true;
        }

        for (Build parentBuild : build.getParentBuilds()) {
            if (parentBuild.getStatus().worstThan(bvc.getParentStatusThreshold())) {
                log.info("Parent status threshold exceeded for build {} by parent build {}.",
                        build.getId(), parentBuild.getId());
                return false;
            }
        }

        return true;
    }

    private boolean run(Build build) {
        BuildGroup buildGroup = build.getBuildGroup();
        BuildVerificationConf bvc = build.getBuildVerificationConf();

        // Construct run parameters

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(CIParam.BUILD_ID.toString(), build.getId().toString());

        List<Change> changes = buildGroup.getChanges();
        if (changes != null && changes.size() > 0) {
            int nextUser = 0;
            Set<String> changeOwners = new HashSet<String>();
            for (Change change : changes) {
                changeOwners.add(change.getAuthorEmail());
                if (Boolean.TRUE.equals(change.getHasNextUser())) {
                    nextUser = 1;
                }
            }
            StringBuilder ownersEmails = new StringBuilder();
            // This is to check that parameters length does not get too large if many authors
            int maxOwnerEmailsParamLength = sysConfigEJB.getValue(SysConfigKey.MAXIMUM_OWNER_EMAILS_PARAM_LENGTH, DEFAULT_MAXIMUM_OWNER_EMAILS_PARAM_LENGTH);
            for (String owner : changeOwners) {
                if (ownersEmails.length() + owner.length() > maxOwnerEmailsParamLength) {
                    break;
                }
                if (ownersEmails.length() > 0) {
                    ownersEmails.append(",");
                }
                if (!StringUtils.isEmpty(owner)) {
                    ownersEmails.append(owner);
                }
            }
            parameters.put(CIParam.GERRIT_CHANGE_OWNER_EMAIL.toString(), ownersEmails.toString());
            parameters.put(CIParam.NEXTUSER.toString(), Integer.toString(nextUser));
        }

        parameters.put(CIParam.GERRIT_BRANCH.toString(), buildGroup.getGerritBranch());
        parameters.put(CIParam.GERRIT_PATCHSET_REVISION.toString(), buildGroup.getGerritPatchSetRevision());
        parameters.put(CIParam.GERRIT_REFSPEC.toString(), buildGroup.getGerritRefSpec());
        parameters.put(CIParam.GERRIT_URL.toString(), buildGroup.getGerritUrl());
        parameters.put(CIParam.GERRIT_PROJECT.toString(), buildGroup.getGerritProject());
        parameters.put(CIParam.PRODUCT.toString(), bvc.getProductName());
        parameters.put(CIParam.BUILD_GROUP_ID.toString(), buildGroup.getId().toString());

        if (StringUtils.isNotEmpty(bvc.getUserFiles())) {
            parameters.put(CIParam.TEST_FILES.toString(), bvc.getUserFiles());
        }

        String baseUrl = sysConfigEJB.getValue(SysConfigKey.BASE_URL, "");
        if (baseUrl != null && !baseUrl.isEmpty()) {
            parameters.put(CIParam.BUILD_URL.toString(), baseUrl + "/page/build/" + buildGroup.getId());
        } else {
            log.warn("No base URL is set, could not add build URL as build parameter!");
        }

        for (BuildInputParam buildInputParameter : bvc.getBuildInputParams()) {
            String paramValue = buildInputParameter.getParamValue();
            // make sure that null is interpret as empty string.
            if (paramValue == null) {
                paramValue = "";
            }
            parameters.put(buildInputParameter.getParamKey(), paramValue);
        }

        for (BuildCustomParameter bcp : bvc.getCustomParameters()) {
            parameters.put(bcp.getParamKey(), bcp.getParamValue());
        }

        for (BuildGroupCustomParameter bgcp : buildGroup.getCustomParameters()) {
            String paramValue = bgcp.getParamValue();
            // make sure that null is interpret as empty string.
            if (paramValue == null) {
                paramValue = "";
            }
            parameters.put(bgcp.getParamKey(), paramValue);
        }

        if (!StringUtils.isEmpty(bvc.getProductRmCode())) {
            parameters.put(CIParam.RM_CODE.toString(), bvc.getProductRmCode());
        }

        if (!StringUtils.isEmpty(bvc.getImeiCode())) {
            StringBuilder imei = new StringBuilder("(imei:");
            imei.append(bvc.getImeiCode());
            imei.append(";)");
            parameters.put(CIParam.PRODUCT_OVERRIDE.toString(), imei.toString());
        }

        if (!StringUtils.isEmpty(bvc.getTasUrl())) {
            parameters.put(CIParam.TAS_ADDRESS.toString(), bvc.getTasUrl());
        }

        // Set FETCH_HEAD parameter to GerritPatchSetRevision value if not set to something else before.
        // This is needed in DBV builds to ensure right commit is checked out in Jenkins build,
        // otherwise current head is checked out.
        if (parameters.get(CIParam.FETCH_HEAD.toString()) == null && !StringUtils.isEmpty(buildGroup.getGerritPatchSetRevision())) {
            parameters.put(CIParam.FETCH_HEAD.toString(), buildGroup.getGerritPatchSetRevision());
        }

        // Run
        try {
            buildGroupCIServerEJB.build(buildGroup.getBuildGroupCIServer(),
                    build.getJobName(), parameters);
        } catch (JobStartException ex) {
            log.error("Build startup failed. Reason: {}", ex.getMessage());
            return false;
        }

        return true;
    }

    public Build addMemConsumptions(Long id, List<MemConsumption> memConsumptions) throws NotFoundException {
        log.debug("Received {} memory consumptions for build {}", memConsumptions.size(), id);
        long startTime = System.currentTimeMillis();

        Build build = read(id);

        // Add build events.
        for (MemConsumption memConsumption : memConsumptions) {
            RelationUtil.relate(build, memConsumption);
        }

        log.debug("Finished adding memory consumptions for build {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return build;
    }

    public Build addTestCaseStats(Long id, List<TestCaseStat> testCaseStats) throws NotFoundException {
        log.debug("Received {} test case stats for build {}", testCaseStats.size(), id);
        long startTime = System.currentTimeMillis();

        Build build = read(id);

        // Add test case stats.
        for (TestCaseStat testCaseStat : testCaseStats) {
            RelationUtil.relate(build, testCaseStat);
        }

        log.debug("Finished adding test case stats for build {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return build;
    }

    public Build addTestCoverages(Long id, List<TestCoverage> testCoverages) throws NotFoundException {
        log.debug("Received {} test coverages for build {}", testCoverages.size(), id);
        long startTime = System.currentTimeMillis();

        Build build = read(id);

        // Add test coverages.
        for (TestCoverage testCoverage : testCoverages) {
            RelationUtil.relate(build, testCoverage);
        }

        log.debug("Finished adding test coverages for build {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return build;
    }
    
    public Build addBuildFailures(Long id, List<BuildFailure> buildFailures) throws NotFoundException {
        log.debug("Received {} build failures for build {}", buildFailures.size(), id);
        long startTime = System.currentTimeMillis();

        Build build = read(id);

        // Add build failures.
        for (BuildFailure buildFailure : buildFailures) {
            RelationUtil.relate(build, buildFailure);
        }

        log.debug("Finished adding build failures for build {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return build;
    }
}
