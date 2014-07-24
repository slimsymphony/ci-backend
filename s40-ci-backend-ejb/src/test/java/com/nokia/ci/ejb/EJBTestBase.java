package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.CustomParam;
import com.nokia.ci.ejb.model.CustomParamValue;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobVerificationConf;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.SlaveInstance;
import com.nokia.ci.ejb.model.SysConfig;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.Verification;
import java.security.Principal;
import java.util.List;
import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;

/**
 * Unit test base class for EJB tests.
 *
 * @author vrouvine
 */
public abstract class EJBTestBase extends CITestBase {

    protected EntityManager em;
    protected CriteriaBuilder cb;
    protected SessionContext context;
    protected CrudFunctionality crud;

    @Before
    public void before() {
        em = Mockito.mock(EntityManager.class);
        cb = Mockito.mock(CriteriaBuilder.class);
        context = Mockito.mock(SessionContext.class);

        CallerPrincipalMock p = new CallerPrincipalMock();
        Mockito.when(context.getCallerPrincipal()).thenReturn(p);
    }

    protected void mockCriteriaQuery(Class fromClass, TypedQuery typedQuery,
            Class selection) {

        CriteriaQuery query = Mockito.mock(CriteriaQuery.class);
        Root root = Mockito.mock(Root.class);
        Mockito.when(cb.createQuery(selection)).thenReturn(query);
        Mockito.when(query.from(fromClass)).thenReturn(root);
        Mockito.when(em.createQuery(query)).thenReturn(typedQuery);
    }

    protected static TypedQuery createTypedQueryMock(Object result) {
        TypedQuery mockQuery = Mockito.mock(TypedQuery.class);
        setupQueryMocks(mockQuery, result);
        return mockQuery;
    }

    protected static Query createQueryMock(Object result) {
        Query mockQuery = Mockito.mock(Query.class);
        setupQueryMocks(mockQuery, result);
        return mockQuery;
    }

    protected static void setupQueryMocks(Query query, Object result) {
        if (result instanceof Throwable) {
            Mockito.when(query.getSingleResult()).thenThrow((Exception) result);
            Mockito.when(query.getResultList()).thenThrow((Exception) result);
        } else if (result instanceof List) {
            Mockito.when(query.getResultList()).thenReturn((List) result);
        } else {
            Mockito.when(query.getSingleResult()).thenReturn(result);
        }
    }

    protected static void verifyBranch(Branch branch1, Branch branch2) {
        Assert.assertNotNull(branch1);
        Assert.assertNotNull(branch2);
        Assert.assertEquals("Id mismatch", branch1.getId(), branch2.getId());
        Assert.assertEquals("Name mismatch", branch1.getName(), branch2.getName());
    }

    protected static void verifyBranchList(List<Branch> branches1, List<Branch> branches2) {
        verifyLists(branches1, branches2);
        for (int i = 0; i < branches1.size(); i++) {
            verifyBranch(branches1.get(i), branches2.get(i));
        }
    }

    protected static void verifyBuild(Build build1, Build build2) {
        Assert.assertNotNull(build1);
        Assert.assertNotNull(build2);

        Assert.assertEquals("Id mismatch", build1.getId(), build2.getId());
        Assert.assertEquals("BuildGroup mismatch", build1.getBuildGroup(), build2.getBuildGroup());
        Assert.assertEquals("Number mismatch", build1.getBuildNumber(), build2.getBuildNumber());
        Assert.assertEquals("Phase mismatch", build1.getPhase(), build2.getPhase());
        Assert.assertEquals("Status mismatch", build1.getStatus(), build2.getStatus());
    }

    protected static void verifyBuildList(List<Build> builds1, List<Build> builds2) {
        verifyLists(builds1, builds2);
        for (int i = 0; i < builds1.size(); i++) {
            verifyBuild(builds1.get(i), builds2.get(i));
        }
    }

    protected static void verifyJob(Job job1, Job job2) {
        Assert.assertNotNull(job1);
        Assert.assertNotNull(job2);

        Assert.assertEquals("branch mismatch", job1.getBranch(), job2.getBranch());
        Assert.assertEquals("id mismatch", job1.getId(), job2.getId());
        Assert.assertEquals("name mismatch", job1.getName(), job2.getName());
        Assert.assertEquals("owner mismatch", job1.getOwner(), job2.getOwner());
    }

    protected static void verifyJobList(List<Job> jobs1, List<Job> jobs2) {
        verifyLists(jobs1, jobs2);
        for (int i = 0; i < jobs1.size(); i++) {
            verifyJob(jobs1.get(i), jobs2.get(i));
        }
    }

    protected static void verifyProduct(Product product1, Product product2) {
        Assert.assertNotNull(product1);
        Assert.assertNotNull(product2);

        Assert.assertEquals(product1.getId(), product2.getId());
        Assert.assertEquals(product1.getName(), product2.getName());
    }

    protected static void verifyProductList(List<Product> products1, List<Product> products2) {
        verifyLists(products1, products2);
        for (int i = 0; i < products1.size(); i++) {
            verifyProduct(products1.get(i), products2.get(i));
        }
    }

    protected static void verifyCustomParam(CustomParam param1, CustomParam param2) {
        Assert.assertNotNull(param1);
        Assert.assertNotNull(param2);

        Assert.assertEquals(param1.getId(), param2.getId());
        Assert.assertEquals(param1.getDisplayName(), param2.getDisplayName());
        Assert.assertEquals(param1.getParamKey(), param2.getParamKey());
    }

    protected static void verifyCustomParamList(List<CustomParam> params1, List<CustomParam> params2) {
        verifyLists(params1, params2);
        for (int i = 0; i < params1.size(); i++) {
            verifyCustomParam(params1.get(i), params2.get(i));
        }
    }

    protected static void verifyCustomParamValue(CustomParamValue value1, CustomParamValue value2) {
        Assert.assertNotNull(value1);
        Assert.assertNotNull(value2);

        Assert.assertEquals(value1.getId(), value2.getId());
        Assert.assertEquals(value1.getParamValue(), value2.getParamValue());
    }

    protected static void verifyCustomParamValueList(List<CustomParamValue> values1, List<CustomParamValue> values2) {
        verifyLists(values1, values2);
        for (int i = 0; i < values1.size(); i++) {
            verifyCustomParamValue(values1.get(i), values2.get(i));
        }
    }

    protected static void verifyVerification(Verification verification1, Verification verification2) {
        Assert.assertNotNull(verification1);
        Assert.assertNotNull(verification2);

        Assert.assertEquals(verification1.getId(), verification2.getId());
        Assert.assertEquals(verification1.getName(), verification2.getName());
    }

    protected static void verifyVerificationList(List<Verification> verifications1, List<Verification> verifications2) {
        verifyLists(verifications1, verifications2);
        for (int i = 0; i < verifications1.size(); i++) {
            verifyVerification(verifications1.get(i), verifications2.get(i));
        }
    }

    protected static void verifyProjectVerificationConf(ProjectVerificationConf conf1, ProjectVerificationConf conf2) {
        Assert.assertNotNull(conf1);
        Assert.assertNotNull(conf2);

        Assert.assertEquals(conf1.getId(), conf2.getId());
        verifyProject(conf1.getProject(), conf2.getProject());
        verifyProduct(conf1.getProduct(), conf2.getProduct());
        verifyVerification(conf1.getVerification(), conf2.getVerification());
    }

    protected static void verifyProjectVerificationConfList(List<ProjectVerificationConf> confs1, List<ProjectVerificationConf> confs2) {
        verifyLists(confs1, confs2);
        for (int i = 0; i < confs1.size(); i++) {
            verifyProjectVerificationConf(confs1.get(i), confs2.get(i));
        }
    }

    protected static void verifyJobVerificationConf(JobVerificationConf conf1, JobVerificationConf conf2) {
        Assert.assertNotNull(conf1);
        Assert.assertNotNull(conf2);

        Assert.assertEquals(conf1.getId(), conf2.getId());
        verifyJob(conf1.getJob(), conf2.getJob());
        verifyProduct(conf1.getProduct(), conf2.getProduct());
        verifyVerification(conf1.getVerification(), conf2.getVerification());
    }

    protected static void verifyJobVerificationConfList(List<JobVerificationConf> confs1, List<JobVerificationConf> confs2) {
        verifyLists(confs1, confs2);
        for (int i = 0; i < confs1.size(); i++) {
            verifyJobVerificationConf(confs1.get(i), confs2.get(i));
        }
    }

    protected static void verifyProject(Project project1, Project project2) {
        Assert.assertNotNull(project1);
        Assert.assertNotNull(project2);

        Assert.assertEquals(project1.getId(), project2.getId());
        Assert.assertEquals(project1.getName(), project2.getName());
    }

    protected static void verifySysConfig(SysConfig sc1, SysConfig sc2) {
        Assert.assertNotNull(sc1);
        Assert.assertNotNull(sc2);

        Assert.assertEquals(sc1.getId(), sc2.getId());
        Assert.assertEquals(sc1.getConfigKey(), sc2.getConfigKey());
        Assert.assertEquals(sc1.getConfigValue(), sc2.getConfigValue());
    }

    protected static void verifySysUser(SysUser u1, SysUser u2) {
        Assert.assertNotNull(u1);
        Assert.assertNotNull(u2);

        Assert.assertEquals(u1.getLoginName(), u2.getLoginName());
        Assert.assertEquals(u1.getEmail(), u2.getEmail());
        Assert.assertEquals(u2.getSecretKey(), u2.getSecretKey());
    }

    protected static void verifyGerrit(Gerrit gerrit1, Gerrit gerrit2) {
        Assert.assertNotNull(gerrit1);
        Assert.assertNotNull(gerrit2);

        Assert.assertEquals(gerrit1.getId(), gerrit2.getId());
        Assert.assertEquals(gerrit1.getUrl(), gerrit2.getUrl());
        Assert.assertEquals(gerrit1.getPort(), gerrit2.getPort());
        Assert.assertEquals(gerrit1.getPrivateKeyPath(), gerrit2.getPrivateKeyPath());
        Assert.assertEquals(gerrit1.getSshUserName(), gerrit2.getSshUserName());
    }

    protected static void verifySlaveInstanceList(List<SlaveInstance> slaves1, List<SlaveInstance> slaves2) {
        verifyLists(slaves1, slaves2);
        for (int i = 0; i < slaves1.size(); i++) {
            verifySlaveInstance(slaves1.get(i), slaves2.get(i));
        }
    }

    protected static void verifySlaveInstance(SlaveInstance slaveInstance, SlaveInstance slaveInstance2) {
        Assert.assertNotNull(slaveInstance);
        Assert.assertNotNull(slaveInstance2);

        Assert.assertEquals(slaveInstance.getId(), slaveInstance2.getId());
    }
}
