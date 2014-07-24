package com.nokia.ci.ejb.it;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.incident.BaseIncidentHandler;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import java.util.List;
import javax.inject.Inject;
import javax.mail.Message;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 * Integration tests for BuildEJB class. Tests are running inside container.
 *
 * @author vrouvine
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@ApplyScriptAfter(value = {"set_referential_integrity_false.sql"})
@UsingDataSet(value = {"build_dataset.yml"})
public class BuildEJBIT {

    private static final Logger log = LoggerFactory.getLogger(BuildEJBIT.class);
    private static final int TEST_TIMEOUT_MILLIS = 10000;
    @Inject
    private BuildEJB buildEJB;
    private Wiser smtp;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return EJBDeploymentCreator.createEJBDeployment("pom.xml");
    }

    @Before
    public void before() {
        log.info("Starting smtp to port 2525");
        smtp = new Wiser(2525);
        smtp.start();
    }

    @After
    public void after() {
        log.info("Stopping smtp");
        smtp.stop();
    }

    @Test
    public void finalizeBuild() throws Exception {
        Long buildId = 4L;
        // Set build status for same as it is coming through notificationPluginEJB
        Build build = buildEJB.read(buildId);
        build.setPhase(BuildPhase.FINISHED);
        build.setStatus(BuildStatus.SUCCESS);

        buildEJB.finalizeBuild(buildId, BuildStatus.SUCCESS);
        List<WiserMessage> messages = waitMails(1);
        Assert.assertNotNull(messages);
        Assert.assertEquals("Messages size not match!", 1, messages.size());
        WiserMessage message = messages.get(0);
        Assert.assertEquals("Message subject should match!", "Success Subject", message.getMimeMessage().getSubject());
        Assert.assertEquals("Receiver should match!", "success@test.mail", 
                message.getMimeMessage().getRecipients(Message.RecipientType.TO)[0].toString());
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void finalizeBuildPreBuildFailureWithParentThreshold() throws Exception {
        Long buildId = 1L;
        // Set build status for same as it is coming through notificationPluginEJB
        Build build = buildEJB.read(buildId);
        build.setPhase(BuildPhase.FINISHED);
        build.setStatus(BuildStatus.FAILURE);
        buildEJB.update(build);

        buildEJB.finalizeBuild(buildId, BuildStatus.FAILURE);
        List<WiserMessage> messages = waitMails(1);
        Assert.assertNotNull(messages);      
        
        // Check delivered mails... EmailReportAction for FAILURE
        for (WiserMessage message : messages) {
            String recipient = message.getMimeMessage().getRecipients(Message.RecipientType.TO)[0].toString();
            if ("failure@test.mail".equals(recipient)) {
                Assert.assertEquals("Message subject should match!", "Failure Subject", message.getMimeMessage().getSubject());
                continue;
            }
            Assert.fail("Recipient " + recipient + " should not have email!");
        }
        //Check that child builds are in finished with NOT_BUILT status.
        List<Build> childBuilds = buildEJB.getChildBuilds(buildId);
        Assert.assertEquals("Child build count should match!", 2, childBuilds.size());
        for (Build child : childBuilds) {
            Assert.assertEquals("Child status should match!", BuildStatus.NOT_BUILT, child.getStatus());
            Assert.assertEquals("Child phase should match!", BuildPhase.FINISHED, child.getPhase());
        }
    }

    private List<WiserMessage> waitMails(int mailCount) throws InterruptedException {
        long start = System.currentTimeMillis();
        log.info("Waiting " + mailCount + " mails...");
        while (smtp.getMessages().size() < mailCount) {
            if ((System.currentTimeMillis() - start) > TEST_TIMEOUT_MILLIS) {
                Assert.fail("Mail should be delivered! Waiting mail timeouted within " + TEST_TIMEOUT_MILLIS + "ms");
                break;
            }
            Thread.sleep(100);
        }
        log.info("Found {} mail(s)", smtp.getMessages().size());
        Assert.assertEquals("Messages size not match expected mail count!", mailCount, smtp.getMessages().size());
        return smtp.getMessages();
    }
}
