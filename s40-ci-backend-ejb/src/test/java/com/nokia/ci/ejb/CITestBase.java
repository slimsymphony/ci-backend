/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchVerificationConf;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildCustomParameter;
import com.nokia.ci.ejb.model.BuildEvent;
import com.nokia.ci.ejb.model.BuildEventPhase;
import com.nokia.ci.ejb.model.BuildFailure;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildInputParam;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.CIServer;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeStatus;
import com.nokia.ci.ejb.model.CustomParam;
import com.nokia.ci.ejb.model.CustomParamValue;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Incident;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.ejb.model.InputParam;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobVerificationConf;
import com.nokia.ci.ejb.model.MemConsumption;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SlaveInstance;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.SlaveMachine;
import com.nokia.ci.ejb.model.SlavePool;
import com.nokia.ci.ejb.model.SysConfig;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.TestCaseStat;
import com.nokia.ci.ejb.model.TestCoverage;
import com.nokia.ci.ejb.model.UserFile;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationCardinality;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

/**
 *
 * @author jajuutin
 */
public class CITestBase {

    /**
     * Create entity with given type and id.
     *
     * @param <T>
     * @param clazz
     * @param id
     * @return
     */
    protected static <T extends BaseEntity> T createEntity(Class<T> clazz, Long id) {
        T t = null;

        try {
            t = (T) clazz.newInstance();
            t.setId(id);
            t.setCreated(new Date());
        } catch (InstantiationException ex) {
            Assert.assertTrue(false);
        } catch (IllegalAccessException ex) {
            Assert.assertTrue(false);
        }

        return t;
    }

    /**
     * Create list of entities with given type. Id's are created sequentially.
     *
     * @param <T>
     * @param clazz
     * @param size
     * @return
     */
    protected static <T extends BaseEntity> List<T> createEntityList(Class<T> clazz,
            int size) {

        List<T> entities = new ArrayList<T>();

        for (int i = 0; i < size; i++) {
            T t = createEntity(clazz, Long.valueOf(i + 1));
            entities.add(t);
        }

        return entities;
    }

    protected static void populateJob(Job job) {
        job.setName("job" + job.getId());
        job.setDisplayName("displayname" + job.getId());
        job.setModified(new Date());
    }

    protected static void populateJobs(List<Job> jobs) {
        for (int i = 0; i < jobs.size(); i++) {
            populateJob(jobs.get(i));
        }
    }

    protected static void populateBranch(Branch branch) {
        branch.setName("branch" + branch.getId());
        branch.setModified(new Date());
    }

    protected static void populateUserFiles(SysUser owner, List<UserFile> userFiles) {
        for (int i = 0; i < userFiles.size(); i++) {
            userFiles.get(i).setModified(new Date());
            userFiles.get(i).setName("file" + i);
            userFiles.get(i).setUuid("file" + i + "-uuid");
            userFiles.get(i).setOwner(owner);
        }
        owner.setUserFiles(userFiles);
    }

    protected static String getUserFileUuids(List<UserFile> userFiles) {

        StringBuilder userFilesStrBuilder = new StringBuilder("");
        for (int i = 0; i < userFiles.size(); i++) {
            userFilesStrBuilder.append(userFiles.get(i).getUuid());
            if (i < userFiles.size() - 1) {
                userFilesStrBuilder.append(",");
            }
        }
        return userFilesStrBuilder.toString();
    }

    protected static void populateBranch(Branch branch, Project project) {
        populateBranch(branch);
        branch.setProject(project);
    }

    protected static void populateBranches(List<Branch> branches) {
        for (int i = 0; i < branches.size(); i++) {
            populateBranch(branches.get(i));
        }
    }

    protected static void populateBranches(List<Branch> branches, Project project) {
        for (int i = 0; i < branches.size(); i++) {
            populateBranch(branches.get(i), project);
        }
    }

    protected static void populateBuildGroup(BuildGroup buildGroup) {
        buildGroup.setJobName("job_name");
        buildGroup.setJobDisplayName("Job Name");
        buildGroup.setGerritBranch("branch");
        buildGroup.setGerritPatchSetRevision("revision");
        buildGroup.setGerritRefSpec("refspec");
        buildGroup.setGerritUrl("url");
        buildGroup.setGerritProject("project");
    }

    protected static void populateBuild(Build build) {
        build.setBuildNumber(build.getId().intValue());
        build.setPhase(BuildPhase.STARTED);
        build.setStatus(BuildStatus.SUCCESS);
        build.setModified(new Date());
        build.setRefSpec("refs/changes/87/3787/" + build.getId());
        build.setStartTime(new Date(System.currentTimeMillis() - (build.getId() % 10) * 10000));
        build.setEndTime(new Date());
    }

    protected static void populateBuilds(List<Build> builds) {
        populateBuildsWithBuildVerificationConfs(builds, null);
    }

    protected static void populateBuildWithBuildVerificationConf(Build build, BuildVerificationConf buildVerificationConf) {
        populateBuild(build);
        if (buildVerificationConf == null) {
            build.setJobDisplayName("Verification" + build.getId() + "---Product" + build.getId());
            return;
        }
        buildVerificationConf.setProductDisplayName("Product " + build.getId());
        buildVerificationConf.setVerificationDisplayName("Verification " + build.getId());
        buildVerificationConf.setCardinality(VerificationCardinality.values()[build.getId().intValue() % 2]);
        buildVerificationConf.setBuild(build);
        build.setBuildVerificationConf(buildVerificationConf);
    }

    protected static void populateBuildsWithBuildVerificationConfs(List<Build> builds, List<BuildVerificationConf> buildVerificationConf) {
        for (int i = 0; i < builds.size(); i++) {
            if (buildVerificationConf == null || buildVerificationConf.size() < i) {
                populateBuildWithBuildVerificationConf(builds.get(i), null);
            } else {
                populateBuildWithBuildVerificationConf(builds.get(i), buildVerificationConf.get(i));
            }
        }
    }

    /**
     * Verifies that lists are not null and sizes matches.
     *
     * @param list1 List instance
     * @param list2 List instance
     */
    protected static void verifyLists(List list1, List list2) {
        Assert.assertNotNull(list1);
        Assert.assertNotNull(list2);
        Assert.assertEquals(list1.size(), list2.size());
    }

    protected void populateProject(Project project) {
        project.setName("Project" + project.getId());
        project.setDisplayName("Project display name" + project.getId());
        project.setDescription("Description of project" + project.getId());
    }

    protected void populateProjects(List<Project> projects) {
        for (int i = 0; i < projects.size(); i++) {
            populateProject(projects.get(i));
        }
    }

    protected void populateProduct(Product product) {
        product.setName("Product" + product.getId());
    }

    protected void populateProducts(List<Product> products) {
        for (Product p : products) {
            populateProduct(p);
        }
    }

    protected void populateInputParams(List<BuildInputParam> params) {
        for (BuildInputParam p : params) {
            populateInputParam(p);
        }
    }

    protected void populateInputParam(BuildInputParam p) {
        p.setParamKey("PARAM_KEY_" + p.getId());
        p.setParamValue("https://test.jenkins.server.com:8080/job/test_project---test_verification_1/" + p.getId());
    }

    protected static void populateChanges(List<Change> changes) {
        for (int i = 0; i < changes.size(); i++) {
            populateChange(changes.get(i));
        }
    }

    protected static void populateChange(Change change) {
        change.setAuthorName("Change Author " + change.getId());
        change.setAuthorEmail("change.author." + change.getId());
        change.setCommitId(StringUtils.substring(change.getId() + "1234567890abcdef1234567890abcdef12345678", 0, 40));
        change.setCommitTime(new Date());
        change.setCreated(new Date());
        change.setModified(new Date());
        change.setStatus(ChangeStatus.NEW);
        change.setSubject("This is change subject for commit " + change.getId() + " and here is some more text in it");
        change.setMessage("This is message for commit " + change.getId() + " and here is some more text in it");
    }

    protected void populateBuildResultDetailsParam(BuildResultDetailsParam param) {
        param.setDisplayName("DisplayName " + param.getId());
        param.setParamKey("PARAM_KEY_" + param.getId());
        param.setDisplayName("Link to Jenkins build " + param.getId());
        param.setParamValue("https://test.jenkins.server.com:8080/job/test_project---test_verification_1/" + param.getId());
        param.setDescription("This build points to Jenkins where the build " + param.getId() + " was made.");
    }

    protected void populateBuildResultDetailsParams(List<BuildResultDetailsParam> params) {
        for (BuildResultDetailsParam param : params) {
            populateBuildResultDetailsParam(param);
        }
    }

    protected void populateCustomParam(CustomParam param) {
        param.setDisplayName("DisplayName " + param.getId());
        param.setParamKey("PARAM_KEY_" + param.getId());
    }

    protected void populateCustomParams(List<CustomParam> params) {
        for (CustomParam param : params) {
            populateCustomParam(param);
        }
    }

    protected void populateCustomParamValue(CustomParamValue value) {
        value.setParamValue("Value" + value.getId());
    }

    protected void populateCustomParamValues(List<CustomParamValue> values) {
        for (CustomParamValue value : values) {
            populateCustomParamValue(value);
        }
    }

    protected void populateVerification(Verification verification) {
        verification.setName("Verification" + verification.getId());
    }

    protected void populateVerifications(List<Verification> verifications) {
        for (Verification v : verifications) {
            populateVerification(v);
        }
    }

    protected void populateProjectVerificationConf(ProjectVerificationConf conf,
            Project project, long productId, long verificationId) {
        conf.setProject(project);
        conf.setProduct(createEntity(Product.class, productId));
        populateProduct(conf.getProduct());
        conf.setVerification(createEntity(Verification.class, verificationId));
        populateVerification(conf.getVerification());
    }

    protected void populateProjectVerificationConfs(List<ProjectVerificationConf> confs, Project project) {
        long i = 1;
        for (ProjectVerificationConf conf : confs) {
            populateProjectVerificationConf(conf, project, i, i);
            i++;
        }
    }

    protected void populateBranchVerificationConf(BranchVerificationConf conf,
            Branch branch, long productId, long verificationId) {
        conf.setBranch(branch);
        conf.setProduct(createEntity(Product.class, productId));
        populateProduct(conf.getProduct());
        conf.setVerification(createEntity(Verification.class, verificationId));
        populateVerification(conf.getVerification());
    }

    protected void populateBranchVerificationConfs(List<BranchVerificationConf> confs, Branch branch) {
        long i = 1;
        for (BranchVerificationConf conf : confs) {
            populateBranchVerificationConf(conf, branch, i, i);
            i++;
        }
    }

    protected void populateJobVerificationConf(JobVerificationConf conf,
            Job job, long productId, long verificationId) {
        conf.setJob(job);
        conf.setProduct(createEntity(Product.class, productId));
        populateProduct(conf.getProduct());
        conf.setImeiCode("1234567890");
        conf.setTasUrl("http://tasserver01:10001");
        conf.setVerification(createEntity(Verification.class, verificationId));
        populateVerification(conf.getVerification());
    }

    protected void populateJobVerificationConfs(List<JobVerificationConf> confs, Job job) {
        long i = 1;
        for (JobVerificationConf conf : confs) {
            populateJobVerificationConf(conf, job, i, i);
            i++;
        }
    }

    protected void populateCIServer(CIServer ciServer) {
        ciServer.setUsername("USERNAME");
        ciServer.setPassword("PASSWORD");
        ciServer.setUrl("URL");
        ciServer.setPort(9999);
    }

    protected void populateSysConfig(SysConfig sysConfig) {
        sysConfig.setConfigValue("CONFIG_VALUE");
        sysConfig.setConfigKey("CONFIG_KEY");
    }

    protected void populateSysUser(SysUser user) {
        user.setLoginName("testName" + user.getId());
        user.setEmail("test@Name" + user.getId());
        user.setUserRole(RoleType.USER);
        user.setSecretKey("abcdefghijklmnopqrstuvwxyz");
    }

    protected void populateSysConfigs(List<SysConfig> sysConfigs) {
        for (SysConfig sc : sysConfigs) {
            populateSysConfig(sc);
        }
    }

    protected void populateGerrit(Gerrit gerrit) {
        gerrit.setPort(12345);
        gerrit.setPrivateKeyPath("PRIVATE_KEY_PATH");
        gerrit.setSshUserName("SSH_USERNAME");
        gerrit.setUrl("GERRIT_URL");
        gerrit.setProjectAccessHost("PROJECT_ACCESS_HOST");
        gerrit.setProjectAccessPort(22);
    }

    protected void populateBuildCustomParameter(BuildCustomParameter bcp) {
        bcp.setParamKey("key" + bcp.getId());
        bcp.setParamValue("value" + bcp.getId());
    }

    protected void populateInputParam(InputParam inputParam) {
        inputParam.setParamKey("key" + inputParam.getId());
        inputParam.setParamValue("value" + inputParam.getId());
    }

    protected void populateBuildEvent(BuildEvent buildEvent) {
        buildEvent.setName("NAME" + buildEvent.getId().toString());
        buildEvent.setPhase(BuildEventPhase.START);
        buildEvent.setDescription("DESCRIPTION" + buildEvent.getId().toString());
        buildEvent.setTimestamp(buildEvent.getId() * 1000);
    }

    protected void populateMemConsumption(MemConsumption mem) {
        mem.setRam(1.0f);
        mem.setRom(1.0f);
    }

    protected void populateTestCoverage(TestCoverage cov) {
        cov.setCondCov(1.0f);
        cov.setStmtCov(1.0f);
    }

    protected void populateBuildFailure(BuildFailure fail) {
        fail.setMessage("Build failed message");
        fail.setTestcaseName("Testcase " + fail.getId());
        fail.setType("Failure type");
    }

    protected void populateTestCaseStat(TestCaseStat stat) {
        stat.setPassedCount(1);
        stat.setTotalCount(1);
    }

    protected void populateSlaveMachines(List<SlaveMachine> slaveMachines) {
        for (SlaveMachine slaveMachine : slaveMachines) {
            populateSlaveMachine(slaveMachine);
        }
    }

    protected void populateSlaveMachine(SlaveMachine slaveMachine) {
        slaveMachine.setCreated(new Date());
        slaveMachine.setEndScript("/scripts/endScript.sh");
        slaveMachine.setStartScript("");
        slaveMachine.setMaxSlaveInstanceAmount(new Long(5));
        slaveMachine.setModified(new Date());
        slaveMachine.setPort(22);
        slaveMachine.setStartScript("/scripts/startScript.sh");
        slaveMachine.setUrl("localhost" + slaveMachine.getId());
        slaveMachine.setWorkspace("/local/groups/workspace");
    }

    protected void populateSlaveInstances(List<SlaveInstance> slaveInstances) {
        for (SlaveInstance slaveInstance : slaveInstances) {
            populateSlaveInstance(slaveInstance);
        }
    }

    protected void populateSlaveInstance(SlaveInstance slaveInstance) {
        slaveInstance.setCurrentMaster(null);
        slaveInstance.setReserved(null);
        slaveInstance.setModified(new Date());
        slaveInstance.setCreated(new Date());
    }

    protected void populateSlavePools(List<SlavePool> slavePools) {
        for (SlavePool slavePool : slavePools) {
            populateSlavePool(slavePool);
        }
    }

    protected void populateSlavePool(SlavePool slavePool) {
        slavePool.setCreated(new Date());
        slavePool.setModified(new Date());
        slavePool.setName("pool_" + slavePool.getId());
    }

    protected void populateSlaveLabels(List<SlaveLabel> slaveLabels) {
        for (SlaveLabel slaveLabel : slaveLabels) {
            populateSlaveLabel(slaveLabel);
        }
    }

    protected void populateSlaveLabel(SlaveLabel slaveLabel) {
        slaveLabel.setCreated(new Date());
        slaveLabel.setModified(new Date());
        slaveLabel.setName("label_" + slaveLabel.getId());
    }

    protected static void populateIncident(Incident incident) {
        incident.setType(IncidentType.DELIVERY_CHAIN);
        incident.setDescription("incidentDesc" + incident.getId());
        incident.setTime(new Date());
        incident.setCheckTime(new Date());
        incident.setCheckUser("user");

    }
}
