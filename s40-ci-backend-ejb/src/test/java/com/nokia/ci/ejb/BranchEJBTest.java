package com.nokia.ci.ejb;

import static com.nokia.ci.ejb.CITestBase.createEntity;
import static com.nokia.ci.ejb.CITestBase.createEntityList;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchVerificationConf;
import com.nokia.ci.ejb.model.CIServer;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.util.RelationUtil;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.ListAttribute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * BranchEJB unit tests.
 *
 * @author vrouvine
 */
public class BranchEJBTest extends EJBTestBase {

    private static final int DEFAULT_SIZE = 5;
    private static final long BRANCH_ID = 1;
    private BranchEJB ejb;

    @Override
    @Before
    public void before() {
        super.before();

        ejb = new BranchEJB();
        ejb.em = em;
        ejb.cb = cb;
        ejb.context = context;
        ejb.projectEJB = Mockito.mock(ProjectEJB.class);
    }

    @Test
    public void getJobsWithoutUserId() throws Exception {
        List<Job> jobs = createEntityList(Job.class, DEFAULT_SIZE);
        populateJobs(jobs);
        TypedQuery typedQuery = createTypedQueryMock(jobs);

        mockFindBranch();
        CriteriaQuery query = Mockito.mock(CriteriaQuery.class);
        ListJoin joinMock = Mockito.mock(ListJoin.class);
        Root root = Mockito.mock(Root.class);
        Mockito.when(cb.createQuery(Job.class)).thenReturn(query);
        Mockito.when(query.from(Branch.class)).thenReturn(root);
        Mockito.when(em.createQuery(query)).thenReturn(typedQuery);
        Mockito.when(root.join(Mockito.any(ListAttribute.class))).thenReturn(joinMock);

        List<Job> results = ejb.getJobs(BRANCH_ID, null);

        Mockito.verify(ejb.cb, Mockito.times(2)).and(Mockito.any(Expression.class), Mockito.any(Expression.class));
        Mockito.verify(ejb.cb, Mockito.never()).or(Mockito.any(Expression.class), Mockito.any(Expression.class));
        Assert.assertEquals(results.size(), DEFAULT_SIZE);
        verifyJobList(jobs, results);
    }

    @Test
    public void getJobsWithUserId() throws Exception {
        List<Job> jobs = createEntityList(Job.class, DEFAULT_SIZE);
        populateJobs(jobs);
        TypedQuery typedQuery = createTypedQueryMock(jobs);

        mockFindBranch();
        CriteriaQuery query = Mockito.mock(CriteriaQuery.class);
        ListJoin joinMock = Mockito.mock(ListJoin.class);
        Root root = Mockito.mock(Root.class);
        Mockito.when(cb.createQuery(Job.class)).thenReturn(query);
        Mockito.when(query.from(Branch.class)).thenReturn(root);
        Mockito.when(em.createQuery(query)).thenReturn(typedQuery);
        Mockito.when(root.join(Mockito.any(ListAttribute.class))).thenReturn(joinMock);

        List<Job> results = ejb.getJobs(BRANCH_ID, Long.MAX_VALUE);

        Mockito.verify(ejb.cb, Mockito.times(1)).or(Mockito.any(Expression.class), Mockito.any(Expression.class));
        Mockito.verify(ejb.cb, Mockito.times(2)).and(Mockito.any(Expression.class), Mockito.any(Expression.class));
        Assert.assertEquals(results.size(), DEFAULT_SIZE);
        verifyJobList(jobs, results);
    }

    @Test
    public void setJobs() throws Exception {
        // setup
        Branch branch = mockFindBranch();

        final int oldJobCount = 2;
        for (int i = 0; i < oldJobCount; i++) {
            RelationUtil.relate(branch, new Job());
        }

        final int newJobCount = 5;
        List<Job> newJobs = new ArrayList<Job>();
        for (int i = 0; i < newJobCount; i++) {
            Job job = new Job();
            job.setId(Long.valueOf(i));
            newJobs.add(job);

            Mockito.when(em.find(Job.class, Long.valueOf(i))).thenReturn(job);
        }

        // run
        ejb.setJobs(BRANCH_ID, newJobs);

        // verify
        Assert.assertTrue(branch.getJobs().size() == newJobCount);

        for (int i = 0; i < newJobCount; i++) {
            Job job = newJobs.get(i);
            Assert.assertTrue(branch.getJobs().contains(job));
            Assert.assertTrue(job.getBranch() == branch);
        }
    }

    @Test
    public void setCIServers() throws Exception {
        // setup
        Branch branch = mockFindBranch();

        final int oldServerCount = 2;
        for (int i = 0; i < oldServerCount; i++) {
            RelationUtil.relate(branch, new CIServer());
        }

        final int newServerCount = 5;
        List<CIServer> newServers = new ArrayList<CIServer>();
        for (int i = 0; i < newServerCount; i++) {
            CIServer server = new CIServer();
            server.setId(Long.valueOf(i));
            newServers.add(server);

            Mockito.when(em.find(CIServer.class, Long.valueOf(i))).thenReturn(server);
        }

        // run
        ejb.setCIServers(BRANCH_ID, newServers);

        // verify
        Assert.assertTrue(branch.getCiServers().size() == newServerCount);

        for (int i = 0; i < newServerCount; i++) {
            CIServer server = newServers.get(i);
            Assert.assertTrue(branch.getCiServers().contains(server));
            Assert.assertTrue(server.getBranches().contains(branch));
        }
    }

    @Test
    public void createBranchTest() {
        List<BranchVerificationConf> newConfs = createEntityList(BranchVerificationConf.class, DEFAULT_SIZE);
        Branch b = new Branch();

        ejb.create(b, newConfs);

        Assert.assertArrayEquals(b.getVerificationConfs().toArray(), newConfs.toArray());
    }

    @Test
    public void branchVerificationConfTest() throws NotFoundException {
        List<BranchVerificationConf> oldConfs = createEntityList(BranchVerificationConf.class, 3);
        List<BranchVerificationConf> newConfs = createEntityList(BranchVerificationConf.class, DEFAULT_SIZE);
        Branch b = mockFindBranch();
        b.setVerificationConfs(oldConfs);
        Mockito.when(ejb.update(b)).thenReturn(b);

        Branch result = ejb.updateFromUI(b, newConfs);

        Assert.assertEquals(result.getVerificationConfs().size(), newConfs.size());
        for (BranchVerificationConf bvc : newConfs) {
            Assert.assertEquals(bvc.getBranch(), result);
        }
    }

    @Test
    public void checkAdminRightsAdmin() throws BackendAppException {
        SysUser user = createEntity(SysUser.class, 1L);
        user.setUserRole(RoleType.SYSTEM_ADMIN);

        Branch b = mockFindBranch();

        ejb.checkAdminRights(b.getId(), user);
    }

    @Test(expected = UnauthorizedException.class)
    public void checkAdminRightsUser() throws BackendAppException {
        SysUser user = createEntity(SysUser.class, 1L);
        user.setUserRole(RoleType.USER);
        Branch b = mockFindBranch();

        ejb.checkAdminRights(b.getId(), user);
    }

    @Test
    public void checkAdminRightsProjectAdmin() throws BackendAppException {
        SysUser user = createEntity(SysUser.class, 1L);
        user.setUserRole(RoleType.PROJECT_ADMIN);

        List<SysUser> adminUsers = new ArrayList<SysUser>();
        adminUsers.add(user);

        Branch b = mockFindBranch();
        b.setProject(createEntity(Project.class, 1L));
        Mockito.when(ejb.projectEJB.getAdmins(1L)).thenReturn(adminUsers);

        ejb.checkAdminRights(b.getId(), user);
    }

    @Test(expected = UnauthorizedException.class)
    public void checkAdminRightsProjectAdminUnauthorized() throws BackendAppException {
        SysUser user = createEntity(SysUser.class, 1L);
        user.setUserRole(RoleType.PROJECT_ADMIN);

        Branch b = mockFindBranch();
        b.setProject(createEntity(Project.class, 1L));

        ejb.checkAdminRights(b.getId(), user);
    }

    private Branch mockFindBranch() {
        Branch b = createEntity(Branch.class, BRANCH_ID);
        Mockito.when(em.find(Branch.class, BRANCH_ID)).thenReturn(b);
        return b;
    }
}
