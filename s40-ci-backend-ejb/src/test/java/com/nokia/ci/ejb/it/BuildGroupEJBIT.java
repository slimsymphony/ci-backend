package com.nokia.ci.ejb.it;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.exception.InvalidPhaseException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.BuildStatus;
import java.util.List;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptAfter;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.TransactionMode;
import org.jboss.arquillian.persistence.Transactional;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for BuilGroupEJB class. Tests are running inside container.
 *
 * @author vrouvine
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@ApplyScriptAfter(value = {"set_referential_integrity_false.sql"})
@UsingDataSet(value = {"build_group_dataset.yml"})
public class BuildGroupEJBIT {

    private static final Logger log = LoggerFactory.getLogger(BuildGroupEJBIT.class);
    @Inject
    private BuildGroupEJB buildGroupEJB;
    @Inject
    private BuildEJB buildEJB;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return EJBDeploymentCreator.createEJBDeployment("pom.xml");
    }

    @Test
    public void getBuildResultDetailsParams() {
        List<BuildResultDetailsParam> buildResultDetailsParams = buildGroupEJB.getBuildResultDetailsParams(1);
        Assert.assertEquals("BuildResultDetailsParams count does not match!", 3, buildResultDetailsParams.size());
        Assert.assertEquals("BuildResultDetailsParams buildVerificationConf verificationDisplayName does not match!", "verification1", buildResultDetailsParams.get(0).getBuildVerificationConf().getVerificationDisplayName());
        Assert.assertEquals("BuildResultDetailsParams buildVerificationConf productDisplayName does not match!", "product1", buildResultDetailsParams.get(0).getBuildVerificationConf().getProductDisplayName());
    }

    @Test
    public void getStartNodes() {
        List<Build> startNodes = buildGroupEJB.getStartNodes(1L);
        Assert.assertEquals("Should be only one start node!", 1, startNodes.size());
        Assert.assertEquals("Start node id not match!", Long.valueOf(1), startNodes.get(0).getId());
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void updateStatus() throws NotFoundException, InvalidPhaseException {
        final long BUILD_GROUP_ID = 4L;

        buildGroupEJB.updateStatus(BUILD_GROUP_ID, BuildStatus.SUCCESS);

        BuildGroup buildGroup = buildGroupEJB.read(BUILD_GROUP_ID);
        Assert.assertEquals("Build group status not match!", BuildStatus.SUCCESS, buildGroup.getStatus());
        Assert.assertEquals("Build group phase not match!", BuildPhase.STARTED, buildGroup.getPhase());
        // Change last build status and phase
        Build build = buildEJB.read(9L);
        build.setStatus(BuildStatus.FAILURE);
        build.setPhase(BuildPhase.FINISHED);
        buildEJB.update(build);

        buildGroupEJB.updateStatus(BUILD_GROUP_ID, BuildStatus.FAILURE);
        buildGroup = buildGroupEJB.read(BUILD_GROUP_ID);
        Assert.assertEquals("Build group status not match!", BuildStatus.FAILURE, buildGroup.getStatus());
        Assert.assertEquals("Build group phase not match!", BuildPhase.FINISHED, buildGroup.getPhase());
    }

    @Test(expected = InvalidPhaseException.class)
    @Transactional(TransactionMode.DISABLED)
    public void updateStatusAlreadyFinnished() throws Exception {
        buildGroupEJB.updateStatus(1L, BuildStatus.SUCCESS);
    }
}