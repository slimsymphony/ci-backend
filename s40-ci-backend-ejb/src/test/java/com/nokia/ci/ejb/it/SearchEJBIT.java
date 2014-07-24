package com.nokia.ci.ejb.it;

import com.nokia.ci.ejb.SearchEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.index.SearchIndexCreator;
import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysUser;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import org.infinispan.Cache;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptAfter;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for SearchEJB class. Tests are running inside container.
 *
 * @author vrouvine
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@ApplyScriptAfter(value = {"set_referential_integrity_false.sql"})
@UsingDataSet(value = {"search_dataset.yml"})
public class SearchEJBIT {

    private static final Logger log = LoggerFactory.getLogger(SearchEJBIT.class);
    @Inject
    private SearchIndexCreator searchIndexCreator;
    @Inject
    private SearchEJB searchEJB;
    @Inject
    private SysUserEJB sysUserEJB;
    @Resource(lookup = "java:jboss/infinispan/cache/ci20/project-access-cache")
    private Cache<String, SysUser> sysUserCache;
    private SecurityClient securityClient;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return EJBDeploymentCreator.createEJBDeployment("pom.xml");
    }

    @Before
    public void init() throws Exception {
        searchIndexCreator.buildIndex();
        while (!searchIndexCreator.checkWorkers()) {
            Thread.sleep(100);
        }
        securityClient = SecurityClientFactory.getSecurityClient();
    }

    @After
    public void tearDown() {
        securityClient.logout();
    }

    @Test
    public void searchAsAdminUser() throws Exception {
        login("admin");
        // Search exact project name
        search("project_x", new ExpectedResultSet(Project.class, 1));
        search("project_y", new ExpectedResultSet(Project.class, 2));
        search("project_z", new ExpectedResultSet(Project.class, 3));

        // Search by email
        search("test.email", new ExpectedResultSet(Change.class, 1, 2, 3),
                new ExpectedResultSet(SysUser.class, 1, 2, 3, 4), new ExpectedResultSet(BuildGroup.class, 1, 2, 3));
        // Search by email only users
        search(SysUser.class, "test.email", new ExpectedResultSet(SysUser.class, 1, 2, 3, 4));
        // Search by email only changes
        search(Change.class, "test.email", new ExpectedResultSet(Change.class, 1, 2, 3));

        // Search by 'worm'
        search("worm", new ExpectedResultSet(SysUser.class, 4), new ExpectedResultSet(Change.class, 3));

        // Search by exact file path
        search("/base/folder/games/src/worm.cpp", new ExpectedResultSet(Change.class, 3));

        // Search by partial file path
        search("/base/folder/games/", new ExpectedResultSet(Change.class, 3));
    }

    @Test
    public void searchAsNormalUser() throws Exception {
        // Has access to projects 1 and 2
        login("user");
        // Search exact project name
        search("project_x", new ExpectedResultSet(Project.class, 1));
        search("project_y", new ExpectedResultSet(Project.class, 2));
        search("project_z");

        // Search by email
        search("test.email", new ExpectedResultSet(Change.class, 1, 2),
                new ExpectedResultSet(SysUser.class, 1, 2, 3, 4), new ExpectedResultSet(BuildGroup.class, 1, 2));
        // Search by email only users
        search(SysUser.class, "test.email", new ExpectedResultSet(SysUser.class, 1, 2, 3, 4));
        // Search by email only changes
        search(Change.class, "test.email", new ExpectedResultSet(Change.class, 1, 2));

        // Search by 'worm'
        search("worm", new ExpectedResultSet(SysUser.class, 4));

        // Search by exact file path
        search("/base/folder/games/src/worm.cpp");

        // Search by partial file path
        search("/base/folder/games/");
    }

    @Test
    public void searchAsFooBarUser() throws Exception {
        // Has access to projects 1 and 3
        login("foo");
        // Search exact project name
        search("project_x", new ExpectedResultSet(Project.class, 1));
        search("project_y");
        search("project_z", new ExpectedResultSet(Project.class, 3));

        // Search by email
        search("test.email", new ExpectedResultSet(Change.class, 1, 3),
                new ExpectedResultSet(SysUser.class, 1, 2, 3, 4), new ExpectedResultSet(BuildGroup.class, 1, 3));
        // Search by email only users
        search(SysUser.class, "test.email", new ExpectedResultSet(SysUser.class, 1, 2, 3, 4));
        // Search by email only changes
        search(Change.class, "test.email", new ExpectedResultSet(Change.class, 1, 3));

        // Search by 'worm'
        search("worm", new ExpectedResultSet(SysUser.class, 4), new ExpectedResultSet(Change.class, 3));

        // Search by exact file path
        search("/base/folder/games/src/worm.cpp", new ExpectedResultSet(Change.class, 3));

        // Search by partial file path
        search("/base/folder/games/", new ExpectedResultSet(Change.class, 3));
    }

    private void login(String username) throws LoginException, NotFoundException {
        log.info("Login {}", username);
        securityClient.setSimple(username, username);
        securityClient.login();
        SysUser sysUser = sysUserEJB.getSysUser(username);
        sysUser.setProjectAccess(sysUserEJB.getProjectAccess(sysUser.getId()));
        sysUserCache.put(username, sysUser);
    }

    private void search(String query, ExpectedResultSet... expectedResultSets) {
        search(null, query, expectedResultSets);
    }

    private void search(Class type, String query, ExpectedResultSet... expectedResultSets) {
        List<BaseEntity> results = searchEJB.search(type, query);
        assertResults(results, expectedResultSets);
    }

    private void assertResults(List<BaseEntity> results, ExpectedResultSet... expectedResultSets) {
        logResults(results);

        if (expectedResultSets == null || expectedResultSets.length < 1) {
            Assert.assertFalse("Results was not expected!", !results.isEmpty());
            return;
        }

        Assert.assertTrue("Results was expected!", !results.isEmpty());

        int totalExpectedResults = 0;
        for (ExpectedResultSet expected : expectedResultSets) {
            expected.assertMatch(results);
            totalExpectedResults += expected.getResultCount();
        }

        Assert.assertEquals("Expected results and actual results count does not match!", totalExpectedResults, results.size());
    }

    private void logResults(List<BaseEntity> results) {
        log.info("*** Search results ***");
        for (BaseEntity entity : results) {
            log.info("{}", entity);
        }
        log.info("**********************");
    }

    private class ExpectedResultSet {

        private Class<? extends BaseEntity> type;
        private List<Long> ids = new ArrayList<Long>();

        public ExpectedResultSet(Class<? extends BaseEntity> type, int... ids) {
            this.type = type;
            if (ids != null) {
                for (int id : ids) {
                    this.ids.add(Long.valueOf(id));
                }
            }
        }

        public void assertMatch(List<BaseEntity> results) {
            int matchingCounter = 0;
            for (BaseEntity result : results) {
                if (!type.isInstance(result)) {
                    continue;
                }
                Assert.assertTrue("Result " + result + " was not expected!", ids.contains(result.getId()));
                matchingCounter++;
            }
            Assert.assertEquals("Expected results count for " + type + " does not match!", ids.size(), matchingCounter);
        }

        public int getResultCount() {
            return ids.size();
        }
    }
}
