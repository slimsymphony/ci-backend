/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import static com.nokia.ci.ejb.CITestBase.createEntity;
import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.InvalidPhaseException;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.mail.MailSenderEJB;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildCustomParameter;
import com.nokia.ci.ejb.model.BuildEvent;
import com.nokia.ci.ejb.model.BuildFailure;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildGroupCIServer;
import com.nokia.ci.ejb.model.BuildInputParam;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.CIServer;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.MemConsumption;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.TestCaseStat;
import com.nokia.ci.ejb.model.TestCoverage;
import com.nokia.ci.ejb.model.TestResultType;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationCardinality;
import com.nokia.ci.ejb.model.VerificationFailureReason;
import com.nokia.ci.ejb.util.RelationUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.event.Event;
import javax.persistence.TypedQuery;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class BuildEJBTest extends EJBTestBase {

    private static final Long JOB_ID = 1L;
    private BuildEJB ejb;

    @Override
    @Before
    public void before() {
        super.before();

        ejb = new BuildEJB();
        ejb.em = em;
        ejb.cb = cb;

        ejb.buildGroupCIServerEJB = Mockito.mock(BuildGroupCIServerEJB.class);
        ejb.gerritEJB = Mockito.mock(GerritEJB.class);
        ejb.jobEJB = Mockito.mock(JobEJB.class);
        ejb.mailSenderEJB = Mockito.mock(MailSenderEJB.class);
        ejb.sysConfigEJB = Mockito.mock(SysConfigEJB.class);
        ejb.buildGroupEJB = Mockito.mock(BuildGroupEJB.class);
        ejb.finalizeBuildEJB = Mockito.mock(FinalizeBuildEJB.class);
        ejb.verificationEJB = Mockito.mock(VerificationEJB.class);
        ejb.buildStartedEvent = (Event<Long>) Mockito.mock(Event.class);
    }

    @Test
    public void getBuildsByRefSpec() {
        List<Build> builds = createEntityList(Build.class, 1);
        populateBuilds(builds);

        Build build = builds.get(0);
        TypedQuery typedQuery = createTypedQueryMock(builds);
        mockCriteriaQuery(Build.class, typedQuery, Build.class);

        List<Build> results = ejb.getBuildsByRefSpec(build.getRefSpec());
        verifyBuildList(builds, results);
    }

    @Test
    public void start() throws NotFoundException, JobStartException {
        // setup

        final Long STARTABLE_BUILD_ID = 1L;
        Build build = createStartableBuildWithSingleTarget(STARTABLE_BUILD_ID);

        mockBuildRead(build);

        mockSysConfigEJBToThrowNotFoundException();

        // run
        ejb.start(STARTABLE_BUILD_ID);

        // verify
        BuildVerificationConf bvc = build.getBuildVerificationConf();
        BuildCustomParameter bcp = bvc.getCustomParameters().get(0);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(CIParam.BUILD_ID.toString(), build.getId().toString());
        parameters.put(CIParam.GERRIT_BRANCH.toString(), build.getBuildGroup().getGerritBranch());
        parameters.put(CIParam.GERRIT_PATCHSET_REVISION.toString(), build.getBuildGroup().getGerritPatchSetRevision());
        parameters.put(CIParam.GERRIT_REFSPEC.toString(), build.getBuildGroup().getGerritRefSpec());
        parameters.put(CIParam.GERRIT_URL.toString(), build.getBuildGroup().getGerritUrl());
        parameters.put(CIParam.GERRIT_PROJECT.toString(), build.getBuildGroup().getGerritProject());
        parameters.put(CIParam.PRODUCT.toString(), bvc.getProductName());
        parameters.put(CIParam.RM_CODE.toString(), bvc.getProductRmCode());
        parameters.put(CIParam.PRODUCT_OVERRIDE.toString(), "(imei:" + bvc.getImeiCode() + ";)");
        parameters.put(CIParam.TAS_ADDRESS.toString(), bvc.getTasUrl());
        parameters.put(CIParam.BUILD_GROUP_ID.toString(), build.getBuildGroup().getId().toString());
        parameters.put(CIParam.FETCH_HEAD.toString(), build.getBuildGroup().getGerritPatchSetRevision());
        parameters.put(CIParam.TEST_FILES.toString(), build.getBuildVerificationConf().getUserFiles());
        parameters.put(bcp.getParamKey(), bcp.getParamValue());
        Mockito.verify(ejb.buildGroupCIServerEJB, Mockito.times(1)).build(build.getBuildGroup().getBuildGroupCIServer(),
                build.getJobName(), parameters);
        Assert.assertEquals(build.getPhase(), BuildPhase.STARTED);
    }

    @Test
    public void startFails() throws NotFoundException, JobStartException, InvalidPhaseException {
        // setup

        final Long STARTABLE_BUILD_ID = 1L;

        Build build = createStartableBuildWithSingleTarget(STARTABLE_BUILD_ID);

        mockBuildRead(build);
        for (Build childBuild : build.getChildBuilds()) {
            mockBuildRead(childBuild);
        }
        Mockito.doThrow(new JobStartException()).when(ejb.buildGroupCIServerEJB).build(
                (BuildGroupCIServer) Mockito.anyObject(), Mockito.anyString(), Mockito.anyMap());

        Mockito.when(ejb.jobEJB.read(JOB_ID)).thenReturn(new Job());

        mockSysConfigEJBToThrowNotFoundException();

        // run
        ejb.start(STARTABLE_BUILD_ID);

        // verify
        Mockito.verify(ejb.finalizeBuildEJB, Mockito.times(1)).finalizeBuildAtomic(STARTABLE_BUILD_ID, BuildStatus.NOT_BUILT);
    }

    @Test
    public void addBuildEventsTest() throws NotFoundException {
        // setup
        Long buildId = 1L;
        int buildEventSize = 5;
        String executor = "build_executor";

        Build build = createEntity(Build.class, buildId);
        populateBuild(build);
        mockBuildRead(build);

        List<BuildEvent> buildEvents = new ArrayList<BuildEvent>();
        for (long i = 0; i < buildEventSize; i++) {
            BuildEvent buildEvent = createEntity(BuildEvent.class, i + 1);
            populateBuildEvent(buildEvent);
            buildEvents.add(buildEvent);
        }

        // run
        ejb.addBuildEvents(buildId, buildEvents, executor);

        // verify
        Assert.assertEquals(build.getExecutor(), executor);
        Assert.assertEquals(build.getBuildEvents().size(), buildEventSize);
        for (int i = 0; i < buildEventSize; i++) {
            Assert.assertEquals(build.getBuildEvents().get(i), buildEvents.get(i));
            Assert.assertEquals(build, build.getBuildEvents().get(i).getBuild());
        }
    }

    @Test
    public void addMemConsumptionsTest() throws NotFoundException {
        int memConsumptionSize = 5;
        Build build = createEntity(Build.class, 1L);
        populateBuild(build);
        mockBuildRead(build);

        List<MemConsumption> memConsumptions = new ArrayList<MemConsumption>();
        for (long i = 0; i < memConsumptionSize; i++) {
            MemConsumption mem = createEntity(MemConsumption.class, i + 1);
            populateMemConsumption(mem);
            memConsumptions.add(mem);
        }

        ejb.addMemConsumptions(1L, memConsumptions);

        Assert.assertEquals(memConsumptionSize, build.getMemConsumptions().size());
        for (int i = 0; i < memConsumptionSize; i++) {
            Assert.assertEquals(build, build.getMemConsumptions().get(i).getBuild());
            Assert.assertEquals(memConsumptions.get(i), build.getMemConsumptions().get(i));
        }
    }

    @Test
    public void addTestCoveragesTest() throws NotFoundException {
        int testCoveragesSize = 5;
        Build build = createEntity(Build.class, 1L);
        populateBuild(build);
        mockBuildRead(build);

        List<TestCoverage> testCoverages = new ArrayList<TestCoverage>();
        for (long i = 0; i < testCoveragesSize; i++) {
            TestCoverage cov = createEntity(TestCoverage.class, i + 1);
            populateTestCoverage(cov);
            testCoverages.add(cov);
        }

        ejb.addTestCoverages(1L, testCoverages);

        Assert.assertEquals(testCoveragesSize, build.getTestCoverages().size());
        for (int i = 0; i < testCoveragesSize; i++) {
            Assert.assertEquals(build, build.getTestCoverages().get(i).getBuild());
            Assert.assertEquals(testCoverages.get(i), build.getTestCoverages().get(i));
        }
    }

    @Test
    public void addTestCaseStatsTest() throws NotFoundException {
        int testCaseStatsSize = 5;
        Build build = createEntity(Build.class, 1L);
        populateBuild(build);
        mockBuildRead(build);

        List<TestCaseStat> testCaseStats = new ArrayList<TestCaseStat>();
        for (long i = 0; i < testCaseStatsSize; i++) {
            TestCaseStat stat = createEntity(TestCaseStat.class, i + 1);
            populateTestCaseStat(stat);
            testCaseStats.add(stat);
        }

        ejb.addTestCaseStats(1L, testCaseStats);

        Assert.assertEquals(testCaseStatsSize, build.getTestCaseStats().size());
        for (int i = 0; i < testCaseStatsSize; i++) {
            Assert.assertEquals(build, build.getTestCaseStats().get(i).getBuild());
            Assert.assertEquals(testCaseStats.get(i), build.getTestCaseStats().get(i));
        }
    }

    @Test
    public void addBuildFailures() throws NotFoundException {
        int buildFailuresSize = 5;
        Build build = createEntity(Build.class, 1L);
        populateBuild(build);
        mockBuildRead(build);

        List<BuildFailure> buildFailures = new ArrayList<BuildFailure>();
        for (long i = 0; i < buildFailuresSize; i++) {
            BuildFailure fail = createEntity(BuildFailure.class, i + 1);
            populateBuildFailure(fail);
            buildFailures.add(fail);
        }

        ejb.addBuildFailures(1L, buildFailures);

        Assert.assertEquals(buildFailuresSize, build.getBuildFailures().size());
        for (int i = 0; i < buildFailuresSize; i++) {
            Assert.assertEquals(build, build.getBuildFailures().get(i).getBuild());
            Assert.assertEquals(buildFailures.get(i), build.getBuildFailures().get(i));
        }
    }

    @Test
    public void isClassifable() throws BackendAppException {
        // setup
        Branch branch = createEntity(Branch.class, 1L);
        branch.setType(BranchType.DEVELOPMENT);

        Job job = createEntity(Job.class, 1L);
        job.setBranch(branch);

        BuildGroup bg = createEntity(BuildGroup.class, 1L);
        bg.setJob(job);

        Verification verification = createEntity(Verification.class, 1L);
        List<VerificationFailureReason> reasons = new ArrayList<VerificationFailureReason>();
        VerificationFailureReason reason = createEntity(VerificationFailureReason.class, 1L);
        reasons.add(reason);

        Build build = createEntity(Build.class, 1L);
        populateBuild(build);
        mockBuildRead(build);
        build.setBuildGroup(bg);
        build.setStatus(BuildStatus.UNSTABLE);

        Set<TestResultType> types = new HashSet<TestResultType>();
        types.add(TestResultType.NJUNIT);

        BuildVerificationConf bvc = createEntity(BuildVerificationConf.class, 1L);
        bvc.setCardinality(VerificationCardinality.OPTIONAL);
        bvc.setTestResultTypes(types);
        bvc.setVerificationUuid("1234");
        build.setBuildVerificationConf(bvc);

        Mockito.when(ejb.buildGroupEJB.read(1L)).thenReturn(bg);
        Mockito.when(ejb.jobEJB.read(1L)).thenReturn(job);
        Mockito.when(ejb.verificationEJB.getVerificationByUuid("1234")).thenReturn(verification);
        Mockito.when(ejb.verificationEJB.getFailureReasons(1L)).thenReturn(reasons);

        // run
        boolean result = ejb.isClassifiable(1L);

        // verify
        Assert.assertEquals(true, result);
    }

    @Test
    public void processOutputParameters() throws BackendAppException {
        // setup
        Build parent = createEntity(Build.class, 1L);
        populateBuild(parent);
        mockBuildRead(parent);

        Build build = createEntity(Build.class, 2L);
        populateBuild(build);
        mockBuildRead(build);
        RelationUtil.relate(parent, build);

        BuildVerificationConf parentBvc = createEntity(BuildVerificationConf.class, 1L);
        List<BuildResultDetailsParam> parentInputParams = createEntityList(BuildResultDetailsParam.class, 5);
        populateBuildResultDetailsParams(parentInputParams);
        parentBvc.setBuildResultDetailsParams(parentInputParams);
        parent.setBuildVerificationConf(parentBvc);

        BuildVerificationConf bvc = createEntity(BuildVerificationConf.class, 2L);
        List<BuildInputParam> inputParams = createEntityList(BuildInputParam.class, 5);
        populateInputParams(inputParams);
        bvc.setBuildInputParams(inputParams);
        build.setBuildVerificationConf(bvc);

        Map<String, String> offering = new HashMap<String, String>();
        offering.put("PARAM_KEY_2", "thisIsValueFor2");
        offering.put("PARAM_KEY_3", "thisIsValueFor3");

        // run
        ejb.processOutputParameters(1L, offering);

        // verify
        parentInputParams = parent.getBuildVerificationConf().getBuildResultDetailsParams();
        inputParams = build.getBuildVerificationConf().getBuildInputParams();
        for (int i = 0; i < 5; i++) {
            BuildResultDetailsParam pi = parentInputParams.get(i);
            BuildInputParam p = inputParams.get(i);
            Assert.assertEquals(pi.getParamKey(), p.getParamKey());
            Assert.assertEquals(pi.getParamValue(), p.getParamValue());
        }
    }

    @Test
    public void finalizeBuildTest() throws NotFoundException {
        Build build = createEntity(Build.class, 1L);
        populateBuild(build);
        mockBuildRead(build);

        ejb.finalizeBuild(1L, BuildStatus.SUCCESS);
    }

    @Test
    public void getChildBuildsTest() throws NotFoundException {
        Build b = createStartableBuildWithSingleTarget(1L);
        mockBuildRead(b);

        List<Build> childs = ejb.getChildBuilds(1L);

        Assert.assertTrue(childs.get(0).getId() == 2L);
    }

    private void mockBuildRead(Build build) {
        Mockito.when(em.find(Build.class, build.getId())).thenReturn(build);
    }

    private Build createStartableBuildWithSingleTarget(Long id) {
        Gerrit gerrit = createEntity(Gerrit.class, 1L);
        populateGerrit(gerrit);

        Project project = createEntity(Project.class, 2L);
        populateProject(project);
        RelationUtil.relate(gerrit, project);

        Branch branch = createEntity(Branch.class, 3L);
        populateBranch(branch);
        RelationUtil.relate(project, branch);

        Job job = createEntity(Job.class, 4L);
        populateJob(job);
        RelationUtil.relate(branch, job);

        BuildGroup buildGroup = createEntity(BuildGroup.class, 5L);
        populateBuildGroup(buildGroup);
        RelationUtil.relate(job, buildGroup);

        CIServer ciServer = createEntity(CIServer.class, JOB_ID);
        RelationUtil.relate(branch, ciServer);
        BuildGroupCIServer buildGroupCIServer = createEntity(BuildGroupCIServer.class, JOB_ID);
        RelationUtil.relate(buildGroupCIServer, buildGroup);

        Build build = createEntity(Build.class, id);
        populateBuild(build);
        RelationUtil.relate(buildGroup, build);

        BuildVerificationConf bvc = createEntity(BuildVerificationConf.class, 1L);
        bvc.setProductName("product");
        bvc.setProductRmCode("rm-01");
        bvc.setVerificationName("verification");
        bvc.setImeiCode("1234567890");
        bvc.setTasUrl("http://tasserver01:10001");
        bvc.setUserFiles("file0-uuid,file1-uuid");
        RelationUtil.relate(build, bvc);

        BuildCustomParameter bcp = createEntity(BuildCustomParameter.class, 1L);
        populateBuildCustomParameter(bcp);
        RelationUtil.relate(bvc, bcp);

        Build childBuild = createEntity(Build.class, 2L);
        populateBuild(childBuild);
        childBuild.setJobName(project.getName() + "---" + "verification");
        RelationUtil.relate(buildGroup, childBuild);
        RelationUtil.relate(build, childBuild);

        BuildVerificationConf childBvc = createEntity(BuildVerificationConf.class, 1L);
        childBvc.setProductName("product");
        childBvc.setVerificationName("verification");
        childBvc.setParentStatusThreshold(BuildStatus.NOT_BUILT);
        RelationUtil.relate(childBuild, childBvc);

        return build;
    }

    private void mockSysConfigEJBToThrowNotFoundException() throws NotFoundException {
        Mockito.doThrow(new NotFoundException()).when(
                ejb.sysConfigEJB).getSysConfig(Mockito.anyString());
        Mockito.doThrow(new NotFoundException()).when(
                ejb.sysConfigEJB).getSysConfig((SysConfigKey) Mockito.anyObject());
    }
}
