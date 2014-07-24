/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import com.nokia.ci.ejb.model.*;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
public class RelationUtilTest {

    @Test
    public void relateBranchAndJob() {
        Job job = new Job();
        Branch branch = new Branch();

        RelationUtil.relate(branch, job);

        Assert.assertEquals(job.getBranch(), branch);
        Assert.assertEquals(branch.getJobs().get(0), job);
    }

    @Test
    public void unrelateBranchAndJob() {
        Job job = new Job();
        Branch branch = new Branch();

        RelationUtil.relate(branch, job);
        RelationUtil.unrelate(branch, job);

        Assert.assertFalse(branch.getJobs().contains(job));
        Assert.assertNull(job.getBranch());
    }

    @Test
    public void unrelateBranchAndJobs() {
        List<Job> jobs = new ArrayList<Job>();
        Job job1 = new Job();
        Job job2 = new Job();
        jobs.add(job1);
        jobs.add(job2);

        Branch branch = new Branch();

        RelationUtil.relate(branch, job1);
        RelationUtil.relate(branch, job2);

        RelationUtil.unrelateJobs(branch);

        Assert.assertTrue(branch.getJobs().isEmpty());
        Assert.assertNull(job1.getBranch());
        Assert.assertNull(job2.getBranch());
    }

    @Test
    public void relateBranchAndCIServer() {
        Branch branch = new Branch();
        CIServer ciServer = new CIServer();

        RelationUtil.relate(branch, ciServer);

        Assert.assertTrue(branch.getCiServers().contains(ciServer));
        Assert.assertTrue(ciServer.getBranches().contains(branch));
    }

    @Test
    public void unrelateBranchAndCIServer() {
        Branch branch = new Branch();
        CIServer ciServer = new CIServer();

        RelationUtil.relate(branch, ciServer);
        RelationUtil.unrelate(branch, ciServer);

        Assert.assertFalse(branch.getCiServers().contains(ciServer));
        Assert.assertFalse(ciServer.getBranches().contains(branch));
    }

    @Test
    public void unrelateBranchAndCIServers() {
        List<CIServer> servers = new ArrayList<CIServer>();
        CIServer s1 = new CIServer();
        CIServer s2 = new CIServer();
        servers.add(s1);
        servers.add(s2);

        Branch branch = new Branch();

        RelationUtil.relate(branch, s1);
        RelationUtil.relate(branch, s2);

        RelationUtil.unrelateCIServers(branch);

        Assert.assertTrue(branch.getCiServers().isEmpty());
        Assert.assertTrue(s1.getBranches().isEmpty());
        Assert.assertTrue(s2.getBranches().isEmpty());
    }

    @Test
    public void relateProjectAndBranch() {
        Project project = new Project();
        Branch branch = new Branch();

        RelationUtil.relate(project, branch);

        Assert.assertEquals(project.getBranches().get(0), branch);
        Assert.assertEquals(branch.getProject(), project);
    }

    @Test
    public void unrelateProjectAndBranch() {
        Project project = new Project();
        Branch branch = new Branch();

        RelationUtil.relate(project, branch);
        RelationUtil.unrelate(project, branch);

        Assert.assertFalse(project.getBranches().contains(branch));
        Assert.assertNull(branch.getProject());
    }

    @Test
    public void relateJobAndVerificationConf() {
        Job job = new Job();
        JobVerificationConf jvc = new JobVerificationConf();

        RelationUtil.relate(job, jvc);

        Assert.assertEquals(job.getVerificationConfs().get(0), jvc);
        Assert.assertEquals(jvc.getJob(), job);
    }

    @Test
    public void relateSysUserAndJob() {
        SysUser sysUser = new SysUser();
        Job job = new Job();

        RelationUtil.relate(sysUser, job);

        Assert.assertEquals(sysUser.getJobs().get(0), job);
        Assert.assertEquals(job.getOwner(), sysUser);
    }

    @Test
    public void unrelateSysUserAndJob() {
        SysUser sysUser = new SysUser();
        Job job = new Job();

        RelationUtil.relate(sysUser, job);
        RelationUtil.unrelate(sysUser, job);

        Assert.assertFalse(sysUser.getJobs().contains(job));
        Assert.assertNull(job.getOwner());
    }

    @Test
    public void relateProjectAndProduct() {
        Project project = new Project();
        Product product = new Product();

        RelationUtil.relate(project, product);

        Assert.assertEquals(project.getProducts().get(0), product);
        Assert.assertEquals(product.getProjects().get(0), project);
    }

    @Test
    public void relateProjectAndVerification() {
        Project project = new Project();
        Verification verification = new Verification();

        RelationUtil.relate(project, verification);

        Assert.assertEquals(project.getVerifications().get(0), verification);
        Assert.assertEquals(verification.getProjects().get(0), project);
    }

    @Test
    public void relateProjectAndProjectVerificationConf() {
        Project project = new Project();
        ProjectVerificationConf pvc = new ProjectVerificationConf();

        RelationUtil.relate(project, pvc);

        Assert.assertEquals(project.getVerificationConfs().get(0), pvc);
        Assert.assertEquals(pvc.getProject(), project);
    }

    @Test
    public void relateBuildVerificationConfAndBuildCustomParameter() {
        BuildVerificationConf bvc = new BuildVerificationConf();
        BuildCustomParameter bcp = new BuildCustomParameter();

        RelationUtil.relate(bvc, bcp);

        Assert.assertEquals(bvc.getCustomParameters().get(0), bcp);
        Assert.assertEquals(bcp.getBuildVerificationConf(), bvc);
    }

    @Test
    public void relateBuildAndBuild() {
        Build parentBuild = new Build();
        Build childBuild = new Build();

        RelationUtil.relate(parentBuild, childBuild);

        Assert.assertEquals(parentBuild.getChildBuilds().get(0), childBuild);
        Assert.assertTrue(parentBuild.getParentBuilds().isEmpty());
        Assert.assertEquals(childBuild.getParentBuilds().get(0), parentBuild);
        Assert.assertTrue(childBuild.getChildBuilds().isEmpty());
    }
    
    @Test
    public void relateVerificationAndVerification() {
        Verification parent = new Verification();
        Verification child = new Verification();

        RelationUtil.relate(parent, child);

        Assert.assertEquals(parent.getChildVerifications().get(0), child);
        Assert.assertTrue(parent.getParentVerifications().isEmpty());
        Assert.assertEquals(child.getParentVerifications().get(0), parent);
        Assert.assertTrue(child.getChildVerifications().isEmpty());        
    }
    
    @Test
    public void relateVerificationAndInputParameter() {
        Verification verification = new Verification();
        InputParam inputParam = new InputParam();

        RelationUtil.relate(verification, inputParam);

        Assert.assertEquals(verification.getInputParams().get(0), inputParam);
        Assert.assertEquals(inputParam.getVerification(), verification);
    }
    
    @Test
    public void relateBuildVerificationConfAndBuildInputParameter() {
        BuildVerificationConf bvc = new BuildVerificationConf();
        BuildInputParam buildInputParam = new BuildInputParam();

        RelationUtil.relate(bvc, buildInputParam);

        Assert.assertEquals(bvc.getBuildInputParams().get(0), buildInputParam);
        Assert.assertEquals(buildInputParam.getBuildVerificationConf(), bvc);        
    }
    
    @Test
    public void relateBuildAndBuildEvent() {
        Build build = new Build();
        BuildEvent buildEvent = new BuildEvent();

        RelationUtil.relate(build, buildEvent);

        Assert.assertEquals(build.getBuildEvents().get(0), buildEvent);
        Assert.assertEquals(buildEvent.getBuild(), build);
    }    
    
    @Test
    public void relateBuildGroupAndRelease() {
        BuildGroup buildGroup = new BuildGroup();
        Release release = new Release();

        RelationUtil.relate(buildGroup, release);
        
        Assert.assertEquals(buildGroup.getRelease(), release);
        Assert.assertEquals(release.getBuildGroup(), buildGroup);        
    }
}
