/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ejb.changetracker;

import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.EJBTestBase;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.changetracking.ChangeTrackingEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeTracker;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.util.RelationUtil;
import java.util.Date;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class ChangeTrackingEJBTest extends EJBTestBase {

    private ChangeTrackingEJB testSubject;

    @Override
    @Before
    public void before() {
        super.before();

        testSubject = new ChangeTrackingEJB();
        testSubject.buildGroupEJB = Mockito.mock(BuildGroupEJB.class);
        testSubject.projectEJB = Mockito.mock(ProjectEJB.class);
    }

    @Test
    public void buildGroupReleased() throws NotFoundException {
        // setup.
        String commitId = "12345";

        BuildGroup bg = createStructure(commitId, BranchType.MASTER);
        mockBuildGroupRead(bg);
        mockGetChangeTrackers(bg.getJob().getBranch().getProject().getChangeTrackers());

        // run
        testSubject.buildGroupReleased(bg.getId());

        // verify.
        Project project = bg.getJob().getBranch().getProject();
        Assert.assertEquals(bg.getChanges().size(), project.getChangeTrackers().size());
        for (int i = 0; i < bg.getChanges().size(); i++) {
            Assert.assertEquals(bg.getChanges().get(i).getCommitId(), 
                    project.getChangeTrackers().get(i).getCommitId());
            Assert.assertNotNull(project.getChangeTrackers().get(i).getReleased());
        }
    }

    @Test
    public void buildGroupStarted() throws NotFoundException {
        // setup
        String commitId = "12345";

        BuildGroup bg = createStructure(commitId, BranchType.SINGLE_COMMIT);
        mockBuildGroupRead(bg);
        mockGetChangeTrackers(bg.getJob().getBranch().getProject().getChangeTrackers());

        // run
        testSubject.buildGroupStarted(bg.getId());

        // verify
        Project project = bg.getJob().getBranch().getProject();
        Assert.assertEquals(bg.getChanges().size(), project.getChangeTrackers().size());
        for (int i = 0; i < bg.getChanges().size(); i++) {
            Assert.assertEquals(bg.getChanges().get(i).getCommitId(), 
                    project.getChangeTrackers().get(i).getCommitId());
            Assert.assertNotNull(project.getChangeTrackers().get(i).getScvStart());
        }
    }

    @Test
    public void buildGroupFinished() throws NotFoundException {
        // setup
        String commitId = "12345";

        BuildGroup bg = createStructure(commitId, BranchType.SINGLE_COMMIT);
        bg.setStatus(BuildStatus.SUCCESS);
        mockBuildGroupRead(bg);
        mockGetChangeTrackers(bg.getJob().getBranch().getProject().getChangeTrackers());

        // run
        testSubject.buildGroupFinished(bg.getId());

        // verify
        Project project = bg.getJob().getBranch().getProject();
        Assert.assertEquals(bg.getChanges().size(), project.getChangeTrackers().size());
        for (int i = 0; i < bg.getChanges().size(); i++) {
            Assert.assertEquals(bg.getChanges().get(i).getCommitId(), 
                    project.getChangeTrackers().get(i).getCommitId());
            Assert.assertNotNull(project.getChangeTrackers().get(i).getScvEnd());
        }
    }

    @Test
    public void recordVerificationEndFailed() throws NotFoundException {
        // setup
        String commitId = "12345";

        BuildGroup bg = createStructure(commitId, BranchType.SINGLE_COMMIT);
        bg.setStatus(BuildStatus.FAILURE);
        mockBuildGroupRead(bg);
        mockGetChangeTrackers(bg.getJob().getBranch().getProject().getChangeTrackers());

        // run
        testSubject.buildGroupFinished(bg.getId());

        // verify
        Project project = bg.getJob().getBranch().getProject();
        Assert.assertEquals(bg.getChanges().size(), project.getChangeTrackers().size());
        for (int i = 0; i < bg.getChanges().size(); i++) {
            Assert.assertEquals(bg.getChanges().get(i).getCommitId(), 
                    project.getChangeTrackers().get(i).getCommitId());
            Assert.assertNull(project.getChangeTrackers().get(i).getScvEnd());
        }
    }

    private BuildGroup createStructure(String commitId, BranchType bt) {
        Project project = createEntity(Project.class, 1L);

        Branch branch = createEntity(Branch.class, 1L);
        branch.setType(bt);
        RelationUtil.relate(project, branch);

        Job job = createEntity(Job.class, 1L);
        RelationUtil.relate(branch, job);

        BuildGroup bg = createEntity(BuildGroup.class, 1L);
        RelationUtil.relate(job, bg);

        Change change = createEntity(Change.class, 1L);
        change.setCommitId(commitId);
        change.setCommitTime(new Date());
        RelationUtil.relate(bg, change);

        ChangeTracker ct = createEntity(ChangeTracker.class, 1L);
        ct.setCommitId(commitId);
        RelationUtil.relate(project, ct);

        return bg;
    }

    private void mockBuildGroupRead(BuildGroup bg) throws NotFoundException {
        Mockito.when(testSubject.buildGroupEJB.read(Mockito.anyLong())).thenReturn(bg);
    }

    private void mockGetChangeTrackers(List<ChangeTracker> changeTrackers) throws NotFoundException {
        Mockito.when(testSubject.projectEJB.getChangeTrackers(
                Mockito.anyLong(), Mockito.anyList())).thenReturn(changeTrackers);
    }
}
