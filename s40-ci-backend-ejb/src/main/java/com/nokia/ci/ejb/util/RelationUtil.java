/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import com.nokia.ci.ejb.model.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
public class RelationUtil {

    private static Logger log = LoggerFactory.getLogger(RelationUtil.class);

    private RelationUtil() {
    }

    public static void relate(SlaveMachine slaveMachine, SlaveInstance slaveInstance) {
        logRelate(slaveMachine, slaveInstance);
        slaveMachine.getSlaveInstances().add(slaveInstance);
        slaveInstance.setSlaveMachine(slaveMachine);
    }

    public static void unrelate(SlaveMachine slaveMachine, SlaveInstance slaveInstance) {
        logUnrelate(slaveMachine, slaveInstance);
        slaveMachine.getSlaveInstances().remove(slaveInstance);
        slaveInstance.setSlaveMachine(null);
    }

    public static void relate(SlaveInstance slaveInstance, SlavePool slavePool) {
        logRelate(slaveInstance, slavePool);
        slaveInstance.getSlavePools().add(slavePool);
        slavePool.getSlaveInstances().add(slaveInstance);
    }

    public static void unrelate(SlaveInstance slaveInstance, SlavePool slavePool) {
        logUnrelate(slaveInstance, slavePool);
        slaveInstance.getSlavePools().remove(slavePool);
        slavePool.getSlaveInstances().remove(slaveInstance);
    }

    public static void relate(SlaveInstance slaveInstance, SlaveLabel slaveLabel) {
        logRelate(slaveInstance, slaveLabel);
        slaveInstance.getSlaveLabels().add(slaveLabel);
        slaveLabel.getSlaveInstances().add(slaveInstance);
    }

    public static void unrelate(SlaveInstance slaveInstance, SlaveLabel slaveLabel) {
        logUnrelate(slaveInstance, slaveLabel);
        slaveInstance.getSlaveLabels().add(slaveLabel);
        slaveLabel.getSlaveInstances().add(slaveInstance);
    }

    public static void relate(Branch branch, Job job) {
        logRelate(branch, job);
        branch.getJobs().add(job);
        job.setBranch(branch);
    }

    public static void unrelate(Branch branch, Job job) {
        logUnrelate(branch, job);
        branch.getJobs().remove(job);
        job.setBranch(null);
    }

    public static void unrelateJobs(Branch branch) {
        logUnrelate(branch, branch.getJobs());
        List<Job> oldJobList = new ArrayList<Job>();
        oldJobList.addAll(branch.getJobs());
        for (Job j : oldJobList) {
            RelationUtil.unrelate(branch, j);
        }
    }

    public static void relate(Project project, Branch branch) {
        logRelate(project, branch);
        project.getBranches().add(branch);
        branch.setProject(project);
    }

    public static void unrelate(Project project, Branch branch) {
        logUnrelate(project, branch);
        project.getBranches().remove(branch);
        branch.setProject(null);
    }

    public static void relate(Project project, ProjectVerificationConf conf) {
        logRelate(project, conf);
        project.getVerificationConfs().add(conf);
        conf.setProject(project);
    }

    public static void relate(Branch branch, BranchVerificationConf conf) {
        logRelate(branch, conf);
        branch.getVerificationConfs().add(conf);
        conf.setBranch(branch);
    }

    public static void relate(Project project, Product product) {
        logRelate(project, product);
        project.getProducts().add(product);
        product.getProjects().add(project);
    }

    public static void relate(Project project, Verification verification) {
        logRelate(project, verification);
        project.getVerifications().add(verification);
        verification.getProjects().add(project);
    }

    public static void relate(ProjectGroup projectGroup, Project project) {
        logRelate(projectGroup, project);
        projectGroup.getProjects().add(project);
        project.setProjectGroup(projectGroup);
    }

    public static void unrelateProjectGroup(Project project) {
        logUnrelate(project, project.getProjectGroup());
        ProjectGroup projectGroup = project.getProjectGroup();
        if (projectGroup == null) {
            return;
        }
        project.setProjectGroup(null);
        projectGroup.getProjects().remove(project);
    }

    public static void relate(Gerrit gerrit, Project project) {
        logRelate(gerrit, project);
        gerrit.getProjects().add(project);
        project.setGerrit(gerrit);
    }

    public static void unrelateGerrit(Project project) {
        logUnrelate(project, project.getGerrit());
        Gerrit gerrit = project.getGerrit();
        if (gerrit == null) {
            return;
        }
        project.setGerrit(null);
        gerrit.getProjects().remove(project);
    }

    public static void relate(Job job, JobVerificationConf conf) {
        logRelate(job, conf);
        job.getVerificationConfs().add(conf);
        conf.setJob(job);
    }

    public static void relate(Job job, JobPostVerification verification) {
        logRelate(job, verification);
        job.getPostVerifications().add(verification);
        verification.setJob(job);
    }

    public static void unrelate(Job job, JobPostVerification verification) {
        logUnrelate(job, verification);
        job.getPostVerifications().remove(verification);
        verification.setVerification(null);
    }

    public static void relate(Job job, JobPreVerification verification) {
        logRelate(job, verification);
        job.getPreVerifications().add(verification);
        verification.setJob(job);
    }

    public static void unrelate(Job job, JobPreVerification verification) {
        logUnrelate(job, verification);
        job.getPreVerifications().remove(verification);
        verification.setVerification(null);
    }

    public static void unrelate(Job job, JobPrePostVerification verification) {
        logUnrelate(job, verification);
        verification.setJob(null);
        verification.setVerification(null);
    }

    public static void relate(JobPreVerification preVerification,
            Verification verification) {
        logRelate(preVerification, verification);
        preVerification.setVerification(verification);
    }

    public static void relate(JobPostVerification postVerification,
            Verification verification) {
        logRelate(postVerification, verification);
        postVerification.setVerification(verification);
    }

    public static void relate(SysUser sysUser, Job job) {
        logRelate(sysUser, job);
        sysUser.getJobs().add(job);
        job.setOwner(sysUser);
    }

    public static void unrelate(SysUser sysUser, Job job) {
        logUnrelate(sysUser, job);
        sysUser.getJobs().remove(job);
        job.setOwner(null);
    }

    public static void relate(Branch branch, CIServer ciServer) {
        logRelate(branch, ciServer);
        branch.getCiServers().add(ciServer);
        ciServer.getBranches().add(branch);
    }

    public static void unrelate(Branch branch, CIServer ciServer) {
        logUnrelate(branch, ciServer);
        branch.getCiServers().remove(ciServer);
        ciServer.getBranches().remove(branch);
    }

    public static void unrelateCIServers(Branch branch) {
        logUnrelate(branch, branch.getCiServers());
        List<CIServer> oldCIServerList = new ArrayList<CIServer>();
        oldCIServerList.addAll(branch.getCiServers());
        for (CIServer s : oldCIServerList) {
            RelationUtil.unrelate(branch, s);
        }
    }

    public static void relate(BuildGroup buildGroup, Build build) {
        logRelate(buildGroup, build);
        buildGroup.getBuilds().add(build);
        build.setBuildGroup(buildGroup);
    }

    public static void relate(JobCustomVerification customVerification, CustomVerificationConf conf) {
        logRelate(customVerification, conf);
        customVerification.getCustomVerificationConfs().add(conf);
        conf.setJobCustomVerification(customVerification);
    }

    public static void relate(JobCustomVerification customVerification, CustomVerificationParam param) {
        logRelate(customVerification, param);
        customVerification.getCustomVerificationParams().add(param);
        param.setCustomVerification(customVerification);
    }

    public static void relate(Job job, JobCustomVerification customVerification) {
        logRelate(job, customVerification);
        job.getCustomVerifications().add(customVerification);
        customVerification.setJob(job);
    }

    public static void relate(Build parentBuild, Build childBuild) {
        logRelate(parentBuild, childBuild);
        parentBuild.getChildBuilds().add(childBuild);
        childBuild.getParentBuilds().add(parentBuild);
    }

    public static void relate(Build build, BuildVerificationConf bvc) {
        logRelate(build, bvc);
        build.setBuildVerificationConf(bvc);
        bvc.setBuild(build);
    }

    public static void relate(BuildGroupCIServer buildGroupCIServer, BuildGroup buildGroup) {
        logRelate(buildGroupCIServer, buildGroup);
        buildGroupCIServer.setBuildGroup(buildGroup);
        buildGroup.setBuildGroupCIServer(buildGroupCIServer);
    }

    public static void relate(BuildGroup buildGroup, Change change) {
        logRelate(buildGroup, change);
        buildGroup.getChanges().add(change);
        change.getBuildGroups().add(buildGroup);
    }

    public static void relate(BuildVerificationConf bvc, BuildCustomParameter bcp) {
        logRelate(bvc, bcp);
        bvc.getCustomParameters().add(bcp);
        bcp.setBuildVerificationConf(bvc);
    }

    public static void relate(Verification parentVerification, Verification childVerification) {
        logRelate(parentVerification, childVerification);
        parentVerification.getChildVerifications().add(childVerification);
        childVerification.getParentVerifications().add(parentVerification);
    }

    public static void relate(Verification verification, InputParam inputParam) {
        logRelate(verification, inputParam);
        verification.getInputParams().add(inputParam);
        inputParam.setVerification(verification);
    }

    public static void relate(Verification verification, ResultDetailsParam resultDetailsParam) {
        logRelate(verification, resultDetailsParam);
        verification.getResultDetailsParams().add(resultDetailsParam);
        resultDetailsParam.setVerification(verification);
    }

    public static void relate(BuildVerificationConf bvc, BuildInputParam buildInputParam) {
        logRelate(bvc, buildInputParam);
        bvc.getBuildInputParams().add(buildInputParam);
        buildInputParam.setBuildVerificationConf(bvc);
    }

    public static void relate(BuildVerificationConf bvc, BuildResultDetailsParam buildResultDetailsParam) {
        logRelate(bvc, buildResultDetailsParam);
        bvc.getBuildResultDetailsParams().add(buildResultDetailsParam);
        buildResultDetailsParam.setBuildVerificationConf(bvc);
    }

    public static void relate(Build build, BuildEvent buildEvent) {
        logRelate(build, buildEvent);
        build.getBuildEvents().add(buildEvent);
        buildEvent.setBuild(build);
    }

    public static void relate(BuildGroup buildGroup, Release release) {
        buildGroup.setRelease(release);
        release.setBuildGroup(buildGroup);
    }

    public static void relate(Job job, BuildGroup buildGroup) {
        logRelate(job, buildGroup);
        job.getBuildGroups().add(buildGroup);
        buildGroup.setJob(job);
    }

    public static void relate(Project project, ChangeTracker ct) {
        logRelate(project, ct);
        project.getChangeTrackers().add(ct);
        ct.getProjects().add(project);
    }

    public static void unrelate(Job job, StatusTriggerPattern pattern) {
        logUnrelate(job, pattern);
        job.getStatusTriggerPatterns().remove(pattern);
        pattern.setJob(null);
    }

    public static void relate(Job job, StatusTriggerPattern pattern) {
        logRelate(job, pattern);
        job.getStatusTriggerPatterns().add(pattern);
        pattern.setJob(job);
    }
    
    public static void unrelate(Job job, FileTriggerPattern pattern) {
        logUnrelate(job, pattern);
        job.getFileTriggerPatterns().remove(pattern);
        pattern.setJob(null);
    }

    public static void relate(Job job, FileTriggerPattern pattern) {
        logRelate(job, pattern);
        job.getFileTriggerPatterns().add(pattern);
        pattern.setJob(job);
    }

    public static void relate(SysUser user, Project project) {
        logRelate(user, project);
        user.getProjectAccess().add(project);
        project.getUserAccess().add(user);
    }

    public static void unrelate(SysUser user, Project project) {
        logUnrelate(user, project);
        user.getProjectAccess().remove(project);
        project.getUserAccess().remove(user);
    }

    public static void relate(BuildGroup buildGroup, Component component) {
        logRelate(buildGroup, component);
        buildGroup.getComponents().add(component);
        component.setBuildGroup(buildGroup);
    }

    public static void relate(Build build, MemConsumption memConsumption) {
        logRelate(build, memConsumption);
        build.getMemConsumptions().add(memConsumption);
        memConsumption.setBuild(build);
    }

    public static void partialRelate(Component component, MemConsumption memConsumption) {
        logRelate(component, memConsumption);
        memConsumption.setComponent(component);
    }

    public static void partialRelate(Component component, TestCaseStat testCaseStat) {
        logRelate(component, testCaseStat);
        testCaseStat.setComponent(component);
    }

    public static void relate(SysUser user, Widget widget) {
        logRelate(user, widget);
        user.getWidgets().add(widget);
        widget.setSysUser(user);
    }

    public static void unrelate(SysUser user, Widget widget) {
        logUnrelate(user, widget);
        user.getWidgets().remove(widget);
        widget.setSysUser(null);
    }

    public static void relate(Widget widget, WidgetSetting setting) {
        logRelate(widget, setting);
        widget.getSettings().add(setting);
        setting.setWidget(widget);
    }

    public static void unrelate(Widget widget, WidgetSetting setting) {
        logUnrelate(widget, setting);
        widget.getSettings().remove(setting);
        setting.setWidget(null);
    }

    public static void relate(Change change, ChangeFile changeFile) {
        logRelate(change, changeFile);
        change.getChangeFiles().add(changeFile);
        changeFile.setChange(change);
    }

    public static void relate(Job job, JobCustomParameter jobCustomParameter) {
        logRelate(job, jobCustomParameter);
        job.getCustomParameters().add(jobCustomParameter);
        jobCustomParameter.setJob(job);
    }

    public static void unrelate(Job job, JobCustomParameter jobCustomParameter) {
        logUnrelate(job, jobCustomParameter);
        job.getCustomParameters().remove(jobCustomParameter);
        jobCustomParameter.setJob(null);
    }

    public static void relate(BuildGroup buildGroup, BuildGroupCustomParameter bgcp) {
        logRelate(buildGroup, bgcp);
        buildGroup.getCustomParameters().add(bgcp);
        bgcp.setBuildGroup(buildGroup);
    }

    public static void relate(Build build, TestCaseStat testCaseStat) {
        logRelate(build, testCaseStat);
        build.getTestCaseStats().add(testCaseStat);
        testCaseStat.setBuild(build);
    }

    public static void partialRelate(Component component, TestCoverage testCoverage) {
        logRelate(component, testCoverage);
        testCoverage.setComponent(component);
    }

    public static void relate(Build build, TestCoverage testCoverage) {
        logRelate(build, testCoverage);
        build.getTestCoverages().add(testCoverage);
        testCoverage.setBuild(build);
    }

    public static void relate(SlaveLabel slaveLabel, SlaveStatPerLabel slaveStatPerLabel) {
        logRelate(slaveLabel, slaveStatPerLabel);
        slaveLabel.getSlaveStatsPerLabel().add(slaveStatPerLabel);
        slaveStatPerLabel.setSlaveLabel(slaveLabel);
    }

    public static void relate(SlavePool slavePool, SlaveStatPerPool slaveStatPerPool) {
        logRelate(slavePool, slaveStatPerPool);
        slavePool.getSlaveStatsPerPool().add(slaveStatPerPool);
        slaveStatPerPool.setSlavePool(slavePool);
    }

    public static void relate(SlavePool slavePool, SlaveStatPerLabel slaveStatPerNullLabel) {
        logRelate(slavePool, slaveStatPerNullLabel);
        slavePool.getSlaveStatsPerLabel().add(slaveStatPerNullLabel);
        slaveStatPerNullLabel.setSlavePool(slavePool);
    }

    public static void relate(SlaveMachine slaveMachine, SlaveStatPerMachine slaveStatPerMachine) {
        logRelate(slaveMachine, slaveStatPerMachine);
        slaveMachine.getSlaveStatsPerMachine().add(slaveStatPerMachine);
        slaveStatPerMachine.setSlaveMachine(slaveMachine);
    }

    public static void relate(Template template, TemplateVerificationConf tvc) {
        logRelate(template, tvc);
        template.getVerificationConfs().add(tvc);
        tvc.setTemplate(template);
    }

    public static void relate(Template template, TemplateCustomVerification tcv) {
        logRelate(template, tcv);
        template.getCustomVerifications().add(tcv);
        tcv.setTemplate(template);
    }

    public static void relate(TemplateCustomVerification tcv, TemplateCustomVerificationConf tcvc) {
        logRelate(tcv, tcvc);
        tcv.getCustomVerificationConfs().add(tcvc);
        tcvc.setCustomVerification(tcv);
    }

    public static void relate(Build b, BuildFailure bf) {
        logRelate(b, bf);
        b.getBuildFailures().add(bf);
        bf.setBuild(b);
    }

    public static void unrelate(Build b, BuildFailure bf) {
        logUnrelate(b, bf);
        b.getBuildFailures().remove(bf);
        bf.setBuild(null);
    }

    public static void relate(BuildFailure bf, BuildFailureReason bfr) {
        logRelate(bf, bfr);
        bf.setFailureReason(bfr);
        bfr.setBuildFailure(bf);
    }

    public static void unrelate(BuildFailure bf, BuildFailureReason bfr) {
        logUnrelate(bf, bfr);
        bf.setFailureReason(null);
        bfr.setBuildFailure(null);
    }

    private static void logRelate(Object o, Object o1) {
        log.debug("Relate {} <> {}", o, o1);
    }

    private static void logUnrelate(Object o, Object o1) {
        log.debug("Unrelate {} <> {}", o, o1);
    }
    
    public static void relate(CustomVerificationConf customVerificationConf, UserFile userFile) {
        logRelate(customVerificationConf, userFile);
        customVerificationConf.getUserFiles().add(userFile);
        userFile.getCustomVerificationConfs().add(customVerificationConf);
    }
    
    public static void relate(JobVerificationConf jobVerificationConf, UserFile userFile) {
        logRelate(jobVerificationConf, userFile);
        jobVerificationConf.getUserFiles().add(userFile);
        userFile.getJobVerificationConfs().add(jobVerificationConf);
    }
    
    public static void relate(SysUser sysUser, UserFile userFile) {
        logRelate(sysUser, userFile);
        sysUser.getUserFiles().add(userFile);
        userFile.setOwner(sysUser);
    }

}
