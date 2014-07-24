/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.it;

import com.nokia.ci.ejb.AuthEJB;
import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.IncidentEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.exception.AuthException;
import com.nokia.ci.ejb.git.GitTimer;
import com.nokia.ci.ejb.incident.SystemUsageIncidentHandlerEJB;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.GitRepositoryStatus;
import com.nokia.ci.ejb.model.Incident;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.ejb.model.StatusTriggerPattern;
import com.nokia.ci.ejb.model.SysConfig;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.reaper.BuildGroupReaper;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptAfter;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.TransactionMode;
import org.jboss.arquillian.persistence.Transactional;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@ApplyScriptAfter(value = {"set_referential_integrity_false.sql"})
@UsingDataSet(value = {"incident_dataset.yml"})
public class IncidentEJBIT {

    private static final Logger log = LoggerFactory.getLogger(IncidentEJBIT.class);
    private static final int TEST_TIMEOUT_MILLIS = 10000;
    @Inject
    private BuildGroupReaper buildGroupReaper;
    @Inject
    private IncidentEJB incidentEJB;
    @Inject
    private BuildEJB buildEJB;
    @Inject
    private SysConfigEJB sysConfigEJB;
    @Inject
    private GitTimer gitTimer;
    @Inject
    private SystemUsageIncidentHandlerEJB systemUsageEJB;
    @Inject
    private BranchEJB branchEJB;
    @Inject
    private AuthEJB authEJB;
    @Inject
    private JobEJB jobEJB;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return EJBDeploymentCreator.createEJBDeployment("pom.xml");
    }

    @After
    public void clearIncidents() throws Exception {
        List<Incident> incidents = new ArrayList<Incident>();
        incidents.addAll(incidentEJB.readAll());
        for (Incident i : incidents) {
            incidentEJB.delete(i);
        }
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void statusTriggerIncident() throws Exception {
        // Setup
        List<StatusTriggerPattern> patterns = new ArrayList<StatusTriggerPattern>();
        StatusTriggerPattern p = new StatusTriggerPattern();
        p.setPattern("FFFF");
        patterns.add(p);
        jobEJB.saveStatusTriggerPatterns(1L, patterns);

        Long buildId = 3L;
        Build build = buildEJB.read(buildId);
        build.setPhase(BuildPhase.FINISHED);
        build.setStatus(BuildStatus.FAILURE);

        // Run
        buildEJB.finalizeBuild(buildId, BuildStatus.FAILURE);

        // Verify
        List<Incident> incidents = waitIncidents(1, IncidentType.DELIVERY_CHAIN);
        Assert.assertNotNull(incidents);
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void buildGroupReaperIncident() throws Exception {
        // Run
        buildGroupReaper.fire();

        // Verify
        List<Incident> incidents = waitIncidents(1, IncidentType.DELIVERY_CHAIN);
        Assert.assertNotNull(incidents);
        Build b = buildEJB.read(3L);
        Assert.assertEquals(b.getStatus(), BuildStatus.ABORTED);
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void unsuccesfulLoginIncident() throws Exception {
        // Run
        try {
            SysUser user = authEJB.authenticate("admin", "noGoodPassword");
        } catch (AuthException e) {
            log.info("Authentication failed, waiting for incident report...");
        }

        // Verify
        List<Incident> incidents = waitIncidents(1, IncidentType.SECURITY);
        Assert.assertNotNull(incidents);
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void gitFailureIncident() throws Exception {
        // Setup
        Branch branch = branchEJB.read(2L);
        branch.setGitRepositoryPath("/");
        branch.setGitRepositoryStatus(GitRepositoryStatus.UNINITIALIZED);
        branch.setGitFailureCounter(0);
        branchEJB.update(branch);

        // Run
        gitTimer.fire();

        // Verify
        List<Incident> incidents = waitIncidents(1, IncidentType.SYSTEM);
        Assert.assertNotNull(incidents);
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void systemUsageIncidents() throws Exception {
        // Setup
        SysConfig cpuSample = sysConfigEJB.getSysConfig(SysConfigKey.SYSTEM_LOAD_STACK_SAMPLING_SIZE);
        cpuSample.setConfigValue("1");
        sysConfigEJB.update(cpuSample);

        SysConfig memSample = sysConfigEJB.getSysConfig(SysConfigKey.MEM_USAGE_STACK_SAMPLING_SIZE);
        memSample.setConfigValue("1");
        sysConfigEJB.update(memSample);

        SysConfig cpuThreshold = sysConfigEJB.getSysConfig(SysConfigKey.SYSTEM_LOAD_THRESHOLD_IN_PERCENT);
        cpuThreshold.setConfigValue("0");
        sysConfigEJB.update(cpuThreshold);

        SysConfig memThreshold = sysConfigEJB.getSysConfig(SysConfigKey.MEM_USAGE_THRESHOLD_IN_PERCENT);
        memThreshold.setConfigValue("0");
        sysConfigEJB.update(memThreshold);

        // Run
        systemUsageEJB.fire();

        // Verify
        List<Incident> incidents = waitIncidents(2, IncidentType.SYSTEM);
        Assert.assertNotNull(incidents);
    }

    private List<Incident> waitIncidents(int incidentCount, IncidentType type) throws InterruptedException {
        long start = System.currentTimeMillis();
        log.info("Waiting " + incidentCount + " incidents with type {}", type);
        List<Incident> incidents = new ArrayList<Incident>();
        while (true) {
            if ((System.currentTimeMillis() - start) > TEST_TIMEOUT_MILLIS) {
                Assert.fail("Incident should be reported. " + incidents.size() + " incidents reported in " + TEST_TIMEOUT_MILLIS + "ms");
                break;
            }

            incidents = incidentEJB.getUncheckedIncidentsByType(type);

            if (incidents.size() >= incidentCount) {
                break;
            }

            Thread.sleep(100);
        }

        log.info("Found {} incident(s)", incidents.size());
        Assert.assertEquals("Incidents size not match expected incident count!", incidentCount, incidents.size());
        return incidents;
    }
}
