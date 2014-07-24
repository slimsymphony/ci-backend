/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.*;
import com.nokia.ci.ejb.util.RelationUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.SessionContext;
import javax.persistence.Query;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class JobEJBTest extends EJBTestBase {

    private static final long JOB_ID = 1;
    private static final int DEFAULT_SIZE = 5;
    private JobEJB ejb;

    @Override
    @Before
    public void before() {
        super.before();

        ejb = new JobEJB();
        ejb.em = em;
        ejb.cb = cb;
        ejb.context = context;
        ejb.buildEJB = Mockito.mock(BuildEJB.class);
        ejb.ciServerEJB = Mockito.mock(CIServerEJB.class);
        ejb.buildGroupCIServerEJB = Mockito.mock(BuildGroupCIServerEJB.class);
        ejb.productEJB = Mockito.mock(ProductEJB.class);
        ejb.verificationEJB = Mockito.mock(VerificationEJB.class);
        ejb.branchEJB = Mockito.mock(BranchEJB.class);
        ejb.sysUserEJB = Mockito.mock(SysUserEJB.class);
        ejb.buildGroupEJB = Mockito.mock(BuildGroupEJB.class);
        ejb.bvcEJB = Mockito.mock(BuildVerificationConfEJB.class);
        ejb.buildInputParamEJB = Mockito.mock(BuildInputParamEJB.class);
        ejb.ctx = Mockito.mock(SessionContext.class);
    }

    @Test
    public void createJobWithBranch() throws NotFoundException {
        // setup
        Job job = createEntity(Job.class, 1L);

        Branch branch = createEntity(Branch.class, 1L);
        Mockito.when(ejb.branchEJB.read(branch.getId())).thenReturn(branch);

        Project project = createEntity(Project.class, 1L);
        branch.setProject(project);

        SysUser sysUser = createEntity(SysUser.class, 1L);
        Mockito.when(ejb.sysUserEJB.read(sysUser.getId())).thenReturn(sysUser);

        // run
        ejb.create(job, branch.getId(), sysUser.getId());

        // verify
        ArgumentCaptor<Job> persistedEntityImpl = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(em, Mockito.atLeastOnce()).persist(persistedEntityImpl.capture());
        Job result = persistedEntityImpl.getValue();
        Assert.assertTrue(job == result);
        Assert.assertTrue(job.getBranch() == branch);
        Assert.assertTrue(branch.getJobs().contains(job));
        Assert.assertEquals(job.getOwner(), sysUser);

        Mockito.verify(em, Mockito.atLeastOnce()).flush();
    }

    @Test
    public void deleteJob() throws NotFoundException, UnauthorizedException {
        // setup
        Job job = createEntity(Job.class, 1L);
        mockJobRead(job);

        SysUser sysUser = createEntity(SysUser.class, 1L);
        sysUser.setUserRole(RoleType.USER);
        Mockito.when(em.find(SysUser.class, sysUser.getId())).thenReturn(sysUser);

        RelationUtil.relate(sysUser, job);

        // run
        ejb.delete(job, sysUser.getId());

        // verify
        Mockito.verify(em, Mockito.atLeastOnce()).remove(job);
    }

    @Test
    public void copyJob() throws NotFoundException, UnauthorizedException {
        // setup
        Job job = createEntity(Job.class, 1L);
        populateJob(job);
        mockJobRead(job);

        List<JobVerificationConf> confs = createEntityList(JobVerificationConf.class, DEFAULT_SIZE);
        populateJobVerificationConfs(confs, job);
        job.setVerificationConfs(confs);

        Branch branch = createEntity(Branch.class, 1L);
        branch.setType(BranchType.TOOLBOX);
        Mockito.when(ejb.branchEJB.read(branch.getId())).thenReturn(branch);
        RelationUtil.relate(branch, job);

        Project project = createEntity(Project.class, 1L);
        branch.setProject(project);

        SysUser sysUser = createEntity(SysUser.class, 1L);
        populateSysUser(sysUser);
        Mockito.when(ejb.sysUserEJB.getSysUser(sysUser.getLoginName())).thenReturn(sysUser);
        Mockito.when(ejb.sysUserEJB.read(sysUser.getId())).thenReturn(sysUser);
        RelationUtil.relate(sysUser, job);

        // run
        Job newJob = ejb.copyJob(job.getId(), sysUser, job.getDisplayName());

        // verify
        Assert.assertNotNull(newJob);

        Assert.assertEquals(job.getBranch(), newJob.getBranch());
        Assert.assertEquals(job.getName(), newJob.getName());
        Assert.assertEquals(job.getVerificationConfs().size(), newJob.getVerificationConfs().size());
        for (int i = 0; i < job.getVerificationConfs().size(); i++) {
            Assert.assertEquals(job.getVerificationConfs().get(i).getImeiCode(), newJob.getVerificationConfs().get(i).getImeiCode());
            Assert.assertEquals(job.getVerificationConfs().get(i).getTasUrl(), newJob.getVerificationConfs().get(i).getTasUrl());
            Assert.assertEquals(job.getVerificationConfs().get(i).getCardinality(), newJob.getVerificationConfs().get(i).getCardinality());
            Assert.assertEquals(job.getVerificationConfs().get(i).getProduct(), newJob.getVerificationConfs().get(i).getProduct());
            Assert.assertEquals(job.getVerificationConfs().get(i).getVerification(), newJob.getVerificationConfs().get(i).getVerification());
        }
 
        Mockito.verify(em, Mockito.atLeastOnce()).flush();
    }

    @Test(expected = UnauthorizedException.class)
    public void deleteJobUnauthorized() throws NotFoundException, UnauthorizedException {
        // setup
        Job job = createEntity(Job.class, 1L);

        mockJobRead(job);

        SysUser jobOwner = createEntity(SysUser.class, 1L);
        jobOwner.setUserRole(RoleType.USER);
        RelationUtil.relate(jobOwner, job);

        SysUser sysUser = createEntity(SysUser.class, 2L);
        sysUser.setUserRole(RoleType.USER);
        Mockito.when(em.find(SysUser.class, sysUser.getId())).thenReturn(sysUser);

        // run
        ejb.delete(job, sysUser.getId());
    }

    @Test
    public void deleteJobAsSystemAdmin() throws NotFoundException, UnauthorizedException {
        // setup
        Job job = createEntity(Job.class, 1L);

        mockJobRead(job);

        SysUser jobOwner = createEntity(SysUser.class, 1L);
        jobOwner.setUserRole(RoleType.USER);
        RelationUtil.relate(jobOwner, job);

        SysUser sysUser = createEntity(SysUser.class, 2L);
        sysUser.setUserRole(RoleType.SYSTEM_ADMIN);
        Mockito.when(em.find(SysUser.class, sysUser.getId())).thenReturn(sysUser);

        // run
        ejb.delete(job, sysUser.getId());
    }

    @Test
    public void updateJobWithoutRelations() throws NotFoundException {
        // setup
        final Long id = 1L;

        Job originalEntity = createEntity(Job.class, id);
        populateJob(originalEntity);

        Branch originalBranch = createEntity(Branch.class, 1L);
        originalEntity.setBranch(originalBranch);

        SysUser originalOwner = createEntity(SysUser.class, 1L);
        originalEntity.setOwner(originalOwner);

        List<JobVerificationConf> originalJvcs =
                originalEntity.getVerificationConfs();

        Job updateEntity = createEntity(Job.class, id);
        updateEntity.setDisplayName("update display name");
        updateEntity.setName("updated name");
        updateEntity.setOwner(null);
        updateEntity.setBranch(null);
        updateEntity.setVerificationConfs(null);

        mockJobRead(originalEntity);

        // run
        Job result = ejb.updateWithoutRelations(updateEntity);

        // verify
        Assert.assertNotNull(result);

        // update is partial. some fields are meant to change. some meant to
        // stay as same.
        Assert.assertEquals(result.getDisplayName(), updateEntity.getDisplayName());
        Assert.assertEquals(result.getName(), updateEntity.getName());
        Assert.assertEquals(result.getBranch(), originalBranch);
        Assert.assertEquals(result.getOwner(), originalOwner);
        Assert.assertEquals(result.getVerificationConfs(), originalJvcs);
    }

    @Test
    public void getJobsWithName() {
        // constants
        final String jobName = "testJob";

        // setup
        Job j = createEntity(Job.class, 1L);
        j.setName(jobName);

        List<Job> listOfOne = new ArrayList<Job>();
        listOfOne.add(j);

        Query mockQuery = createQueryMock(listOfOne);
        Mockito.when(em.createQuery(Mockito.anyString())).thenReturn(mockQuery);

        // Run
        List<Job> results = ejb.getJobsWithName(jobName);

        // Assert
        verifyJob(j, results.get(0));
    }

    @Test
    public void start() throws BackendAppException {
        final String CREATOR_JOB_NAME = "ci20_job_creator";
        final Long MOCK_OBJECT_ID = 1L;

        // mock job read
        Job job = populateStartableJob(MOCK_OBJECT_ID, "does_not_matter", 2);
        mockJobRead(job);
        
        String userFilesStr = getUserFileUuids(job.getVerificationConfs().get(0).getUserFiles());

        // mock ciserver resolving
        Mockito.when(ejb.ciServerEJB.resolveCIServer(
                Mockito.eq(job.getBranch().getCiServers()), Mockito.anyList())).thenReturn(
                job.getBranch().getCiServers().get(0));

        // run
        ejb.start(job.getId(), null, null);

        // verify
        Map<String, String> map = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();

        List<JobVerificationConf> jvcs = job.getVerificationConfs();
        for (JobVerificationConf jvc : jvcs) {
            if (sb.length() != 0) {
                sb.append(",");
            }
            sb.append(job.getBranch().getProject().getName());
            sb.append("---");
            sb.append(jvc.getVerification().getName());
        }
        map.put(CIParam.JOBS.toString(), sb.toString());

        BuildGroupCIServer buildGroupCIServer = job.getBuildGroups().get(0).getBuildGroupCIServer();

        Mockito.verify(ejb.buildGroupCIServerEJB, Mockito.atLeastOnce()).build(
                buildGroupCIServer, CREATOR_JOB_NAME, map);

        Assert.assertNotNull("BuildGroups is null!", job.getBuildGroups());
        Assert.assertFalse("BuildGroups is empty!", job.getBuildGroups().isEmpty());
        Assert.assertEquals("There should be one buildGroup!", 1, job.getBuildGroups().size());
        Assert.assertEquals("There should be user files in build verification conf!", userFilesStr, 
                job.getBuildGroups().get(0).getBuilds().get(0).getBuildVerificationConf().getUserFiles());
    }

    @Test
    public void createBuilds() throws NotFoundException, JobStartException {
        // setup
        String REFSPEC = "refspec";
        String REVISION = "revision";

        // create job and mock it's read.
        Job job = populateStartableJob(1L, "not_interesting", 3);
        mockJobRead(job);
        
        String userFilesStr = getUserFileUuids(job.getVerificationConfs().get(0).getUserFiles());

        // mock ciserver resolving
        Mockito.when(ejb.ciServerEJB.resolveCIServer(
                Mockito.eq(job.getBranch().getCiServers()), Mockito.anyList())).thenReturn(
                job.getBranch().getCiServers().get(0));

        /**
         * Create following scenario of verification dependecies:
         *
         * user has selected:
         *
         * - jvc1: verification1 for product1 (OPTIONAL) - jvc2: verification6
         * for product1 (MANDATORY) - jvc3: verification6 for product2
         * (MANDATORY)
         *
         * description of dependendies:
         *
         * - verification1 has two parents, verification2 and verification3.
         * verification2 has a single parent, verification4. verification3 has
         * two parents, verification4 and verification5 verification
         *
         * - verification 6 has single parent, verification 5.
         *
         * Visualization of expected build structure:
         *
         * |---(v4p1)---(v2p1)---| | | |---(v1p1) (BUILD_GROUP)---|
         * |---(v3p1)---| | | |---(v5p1)---(v6p1) | |---(v5p2)---(v6p2)
         *
         */
        JobVerificationConf jvc1 = job.getVerificationConfs().get(0);
        jvc1.setCardinality(VerificationCardinality.OPTIONAL);
        Product product1 = jvc1.getProduct();
        Verification verification1 = jvc1.getVerification();

        Verification verification2 = createEntity(Verification.class, 2L);
        populateVerification(verification2);
        RelationUtil.relate(verification2, verification1);

        Verification verification3 = createEntity(Verification.class, 3L);
        populateVerification(verification3);
        RelationUtil.relate(verification3, verification1);

        Verification verification4 = createEntity(Verification.class, 4L);
        populateVerification(verification4);
        RelationUtil.relate(verification4, verification2);
        RelationUtil.relate(verification4, verification3);

        Verification verification5 = createEntity(Verification.class, 5L);
        populateVerification(verification5);
        RelationUtil.relate(verification5, verification3);

        // input parameters for verification 5
        InputParam inputParam1 = createEntity(InputParam.class, 1L);
        populateInputParam(inputParam1);
        RelationUtil.relate(verification5, inputParam1);

        InputParam inputParam2 = createEntity(InputParam.class, 2L);
        populateInputParam(inputParam2);
        RelationUtil.relate(verification5, inputParam2);

        JobVerificationConf jvc2 = job.getVerificationConfs().get(1);
        jvc2.setProduct(jvc1.getProduct());
        jvc2.setCardinality(VerificationCardinality.MANDATORY);
        Verification verification6 = jvc2.getVerification();
        verification6.setId(6L);
        verification6.setDisplayName("verification6");
        verification6.setName("verification6");
        RelationUtil.relate(verification5, verification6);

        Product product2 = createEntity(Product.class, 2L);
        populateProduct(product2);

        JobVerificationConf jvc3 = job.getVerificationConfs().get(2);
        jvc3.setVerification(verification6);
        jvc3.setProduct(product2);
        jvc3.setCardinality(VerificationCardinality.MANDATORY);
        jvc3.setImeiCode("1122334455");
        jvc3.setTasUrl("http://tasserver02:10002");

        // Set all verification configurations to pass checks.
        job.getBranch().getVerificationConfs().clear();
        job.getBranch().getProject().getVerificationConfs().clear();
        for (JobVerificationConf jvc : job.getVerificationConfs()) {
            BranchVerificationConf bvc = new BranchVerificationConf();
            bvc.setProduct(jvc.getProduct());
            bvc.setVerification(jvc.getVerification());
            RelationUtil.relate(job.getBranch(), bvc);

            ProjectVerificationConf pvc = new ProjectVerificationConf();
            pvc.setProduct(jvc.getProduct());
            pvc.setVerification(jvc.getVerification());
            RelationUtil.relate(job.getBranch().getProject(), pvc);

        }

        // run
        BuildGroup buildGroup = ejb.createBuilds(1L, REFSPEC, REVISION, null);

        // verify
        Assert.assertEquals("Builds count should match in build group!", 8, buildGroup.getBuilds().size());

        Build build5 = getBuild(buildGroup.getBuilds(), verification5, product1);
        Assert.assertNotNull(build5);
        Assert.assertTrue("Build should be start node!", build5.getStartNode());
        Assert.assertTrue("Build parents should be empty!", build5.getParentBuilds().isEmpty());
        Assert.assertTrue(build5.getChildBuilds().size() == 2);
        Assert.assertTrue(build5.getBuildVerificationConf().getBuildInputParams().size() == 2);
        Assert.assertEquals(VerificationCardinality.MANDATORY, build5.getBuildVerificationConf().getCardinality());
        Assert.assertEquals("1234567890", build5.getBuildVerificationConf().getImeiCode());
        Assert.assertEquals("http://tasserver01:10001", build5.getBuildVerificationConf().getTasUrl());
        Assert.assertEquals(userFilesStr, build5.getBuildVerificationConf().getUserFiles());

        BuildInputParam buildInputParam1 = build5.getBuildVerificationConf().getBuildInputParams().get(0);
        BuildInputParam buildInputParam2 = build5.getBuildVerificationConf().getBuildInputParams().get(1);
        Assert.assertEquals(inputParam1.getParamKey(), buildInputParam1.getParamKey());
        Assert.assertEquals(inputParam1.getParamValue(), buildInputParam1.getParamValue());
        Assert.assertEquals(inputParam2.getParamKey(), buildInputParam2.getParamKey());
        Assert.assertEquals(inputParam2.getParamValue(), buildInputParam2.getParamValue());

        Build build4 = getBuild(buildGroup.getBuilds(), verification4, product1);
        Assert.assertNotNull(build4);
        Assert.assertTrue("Build should be start node!", build4.getStartNode());
        Assert.assertTrue("Build parents should be empty!", build4.getParentBuilds().isEmpty());
        Assert.assertTrue(build4.getChildBuilds().size() == 2);
        Assert.assertEquals(VerificationCardinality.OPTIONAL, build4.getBuildVerificationConf().getCardinality());
        Assert.assertEquals("1234567890", build4.getBuildVerificationConf().getImeiCode());
        Assert.assertEquals("http://tasserver01:10001", build4.getBuildVerificationConf().getTasUrl());
        Assert.assertEquals(userFilesStr, build4.getBuildVerificationConf().getUserFiles());

        Build build3ViaBuild4 = getBuild(build4.getChildBuilds(), verification3, product1);
        Assert.assertNotNull(build3ViaBuild4);
        Assert.assertFalse("Build should not be start node!", build3ViaBuild4.getStartNode());
        Assert.assertTrue(build3ViaBuild4.getParentBuilds().size() == 2);
        Assert.assertTrue(build3ViaBuild4.getChildBuilds().size() == 1);
        Assert.assertEquals(VerificationCardinality.OPTIONAL, build3ViaBuild4.getBuildVerificationConf().getCardinality());
        Assert.assertEquals("1234567890", build3ViaBuild4.getBuildVerificationConf().getImeiCode());
        Assert.assertEquals("http://tasserver01:10001", build3ViaBuild4.getBuildVerificationConf().getTasUrl());
        Assert.assertEquals(userFilesStr, build3ViaBuild4.getBuildVerificationConf().getUserFiles());

        Build build3ViaBuild5 = getBuild(build5.getChildBuilds(), verification3, product1);
        Assert.assertFalse("Build should not be start node!", build3ViaBuild5.getStartNode());
        Assert.assertTrue(build3ViaBuild4 == build3ViaBuild5); // comparing references on purpose.

        Build build2 = getBuild(build4.getChildBuilds(), verification2, product1);
        Assert.assertNotNull(build2);
        Assert.assertFalse("Build should not be start node!", build2.getStartNode());
        Assert.assertTrue(build2.getParentBuilds().size() == 1);
        Assert.assertTrue(build2.getChildBuilds().size() == 1);
        Assert.assertEquals(VerificationCardinality.OPTIONAL, build2.getBuildVerificationConf().getCardinality());
        Assert.assertEquals("1234567890", build2.getBuildVerificationConf().getImeiCode());
        Assert.assertEquals("http://tasserver01:10001", build2.getBuildVerificationConf().getTasUrl());
        Assert.assertEquals(userFilesStr, build2.getBuildVerificationConf().getUserFiles());

        Build build1ViaBuild2 = getBuild(build2.getChildBuilds(), verification1, product1);
        Assert.assertNotNull(build1ViaBuild2);
        Assert.assertFalse("Build should not be start node!", build1ViaBuild2.getStartNode());
        Assert.assertTrue(build1ViaBuild2.getParentBuilds().size() == 2);
        Assert.assertTrue(build1ViaBuild2.getChildBuilds().isEmpty());
        Assert.assertEquals(VerificationCardinality.OPTIONAL, build1ViaBuild2.getBuildVerificationConf().getCardinality());
        Assert.assertEquals("1234567890", build1ViaBuild2.getBuildVerificationConf().getImeiCode());
        Assert.assertEquals("http://tasserver01:10001", build1ViaBuild2.getBuildVerificationConf().getTasUrl());
        Assert.assertEquals(userFilesStr, build1ViaBuild2.getBuildVerificationConf().getUserFiles());

        Build build1ViaBuild3 = getBuild(build3ViaBuild4.getChildBuilds(), verification1, product1);
        Assert.assertTrue(build1ViaBuild2 == build1ViaBuild3); // comparing references on purpose.

        Build build6ViaBuild5 = getBuild(build5.getChildBuilds(), verification6, product1);
        Assert.assertNotNull(build6ViaBuild5);
        Assert.assertFalse("Build should not be start node!", build6ViaBuild5.getStartNode());
        Assert.assertTrue(build6ViaBuild5.getParentBuilds().size() == 1);
        Assert.assertTrue(build6ViaBuild5.getChildBuilds().isEmpty());

        Build build5p2 = getBuild(buildGroup.getBuilds(), verification5, product2);
        Assert.assertNotNull(build5p2);
        Assert.assertTrue("Build should be start node!", build5p2.getStartNode());
        Assert.assertTrue("Build parents should be empty!", build5p2.getParentBuilds().isEmpty());
        Assert.assertTrue(build5p2.getChildBuilds().size() == 1);
        Assert.assertEquals(VerificationCardinality.MANDATORY, build5p2.getBuildVerificationConf().getCardinality());
        Assert.assertEquals("1122334455", build5p2.getBuildVerificationConf().getImeiCode());
        Assert.assertEquals("http://tasserver02:10002", build5p2.getBuildVerificationConf().getTasUrl());
        Assert.assertEquals(userFilesStr, build5p2.getBuildVerificationConf().getUserFiles());

        Build build6p2 = getBuild(build5p2.getChildBuilds(), verification6, product2);
        Assert.assertNotNull(build6p2);
        Assert.assertFalse("Build should not be start node!", build6p2.getStartNode());
        Assert.assertTrue(build6p2.getParentBuilds().size() == 1);
        Assert.assertTrue(build6p2.getChildBuilds().isEmpty());
        Assert.assertEquals(VerificationCardinality.MANDATORY, build6p2.getBuildVerificationConf().getCardinality());
        Assert.assertEquals("1122334455", build6p2.getBuildVerificationConf().getImeiCode());
        Assert.assertEquals("http://tasserver02:10002", build6p2.getBuildVerificationConf().getTasUrl());
        Assert.assertEquals(userFilesStr, build6p2.getBuildVerificationConf().getUserFiles());
    }

    @Test(expected = NullPointerException.class)
    public void startToolboxJobIdNull() throws BackendAppException {
        // run
        ejb.start(null, "refspec", "patchset");
    }

    @Test(expected = NotFoundException.class)
    public void startToolboxJobNonExistingJob() throws BackendAppException {
        // run
        ejb.start(1L, "refspec", "patchset");
    }

    @Test
    public void getEnabledVerificationConfsFull() throws Exception {
        Job job = populateStartableJob(JOB_ID, "Test_job", 2);
        mockJobRead(job);
        List<JobVerificationConf> enabledVerificationConfs = ejb.getEnabledJobVerificationConfs(JOB_ID);
        Assert.assertEquals(job.getVerificationConfs().size(), enabledVerificationConfs.size());
    }

    @Test
    public void getEnabledVerificationConfsPartial() throws Exception {
        Job job = populateStartableJob(JOB_ID, "Test_job", DEFAULT_SIZE);
        List<ProjectVerificationConf> projectVerificationConfs = job.getBranch().getProject().getVerificationConfs();
        projectVerificationConfs.remove(projectVerificationConfs.size() - 1);
        mockJobRead(job);
        List<JobVerificationConf> enabledVerificationConfs = ejb.getEnabledJobVerificationConfs(JOB_ID);
        Assert.assertEquals(job.getVerificationConfs().size() - 1, enabledVerificationConfs.size());
    }

    @Test
    public void saveVerificationConfs() throws Exception {
        List<JobVerificationConf> confs = createEntityList(JobVerificationConf.class, DEFAULT_SIZE);
        List<JobVerificationConf> removeAllConfs = new ArrayList<JobVerificationConf>();
        Job job = createEntity(Job.class, JOB_ID);
        populateJobVerificationConfs(confs, job);
        job.setVerificationConfs(confs);

        mockJobRead(job);
        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);

        ejb.saveVerificationConfs(job.getId(), removeAllConfs);

        Mockito.verify(em, Mockito.atLeastOnce()).remove(Mockito.any(JobVerificationConf.class));
        Mockito.verify(em, Mockito.atLeastOnce()).merge(captor.capture());
        verifyJobVerificationConfList(confs, captor.getValue().getVerificationConfs());
    }

    @Test
    public void getGerrit() throws Exception {
        long branchId = 1;
        long projectId = 2;
        long gerritId = 3;
        Job job = createEntity(Job.class, JOB_ID);
        Branch branch = createEntity(Branch.class, branchId);
        Project project = createEntity(Project.class, projectId);
        Gerrit gerrit = createEntity(Gerrit.class, gerritId);
        RelationUtil.relate(branch, job);
        RelationUtil.relate(project, branch);
        RelationUtil.relate(gerrit, project);

        mockJobRead(job);
        Gerrit resultGerrit = ejb.getGerrit(JOB_ID);

        verifyGerrit(gerrit, resultGerrit);
    }

    @Test(expected = NotFoundException.class)
    public void getGerritWithoutBranch() throws Exception {
        Job job = createEntity(Job.class, JOB_ID);

        mockJobRead(job);
        ejb.getGerrit(JOB_ID);
    }

    @Test(expected = NotFoundException.class)
    public void getGerritWithoutProject() throws Exception {
        long branchId = 1;
        Job job = createEntity(Job.class, JOB_ID);
        Branch branch = createEntity(Branch.class, branchId);
        RelationUtil.relate(branch, job);

        mockJobRead(job);
        ejb.getGerrit(JOB_ID);
    }

    @Test(expected = NotFoundException.class)
    public void getGerritWithInvalidId() throws Exception {
        Mockito.when(em.find(Job.class, JOB_ID)).thenReturn(null);
        ejb.getGerrit(JOB_ID);
    }

    private void mockJobRead(Job job) {
        Mockito.when(em.find(Job.class, job.getId())).thenReturn(job);
    }

    private Build getBuild(List<Build> builds,
            Verification verification, Product product) {

        for (Build build : builds) {
            BuildVerificationConf bvc = build.getBuildVerificationConf();
            if (bvc.getVerificationName().equals(verification.getName())
                    && bvc.getProductName().equals(product.getName())) {
                return build;
            }
        }

        return null;
    }

    private Job populateStartableJob(Long mockObjectId, String jobName, int confSize) {
        Job job = createEntity(Job.class, mockObjectId);
        populateJob(job);
        job.setName(jobName);

        Branch branch = createEntity(Branch.class, mockObjectId);
        populateBranch(branch);
        RelationUtil.relate(branch, job);

        CIServer ciServer = createEntity(CIServer.class, mockObjectId);
        populateCIServer(ciServer);
        RelationUtil.relate(branch, ciServer);

        Project project = createEntity(Project.class, mockObjectId);
        populateProject(project);
        RelationUtil.relate(project, branch);
        List<JobVerificationConf> jvcs =
                createEntityList(JobVerificationConf.class, confSize);
        populateJobVerificationConfs(jvcs, job);
        job.setVerificationConfs(jvcs);

        List<JobPostVerification> posts = createEntityList(JobPostVerification.class, 0);
        job.setPostVerifications(posts);

        List<JobPreVerification> pres = createEntityList(JobPreVerification.class, 0);
        job.setPreVerifications(pres);

        List<ProjectVerificationConf> pvcs = createEntityList(ProjectVerificationConf.class, confSize);
        populateProjectVerificationConfs(pvcs, project);
        project.setVerificationConfs(pvcs);

        List<BranchVerificationConf> bvcs = createEntityList(BranchVerificationConf.class, confSize);
        populateBranchVerificationConfs(bvcs, branch);
        branch.setVerificationConfs(bvcs);

        Gerrit gerrit = createEntity(Gerrit.class, mockObjectId);
        populateGerrit(gerrit);
        RelationUtil.relate(gerrit, project);

        SysUser owner = createEntity(SysUser.class, mockObjectId);
        RelationUtil.relate(owner, job);
        
        List<UserFile> ufs = createEntityList(UserFile.class, 2);
        populateUserFiles(owner, ufs);
        for(JobVerificationConf jvc : jvcs){
            jvc.setUserFiles(ufs);
        }

        return job;
    }
}
