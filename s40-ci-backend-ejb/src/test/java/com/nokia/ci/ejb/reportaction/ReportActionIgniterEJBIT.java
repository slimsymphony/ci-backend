package com.nokia.ci.ejb.reportaction;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.it.EJBDeploymentCreator;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptAfter;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 * Integration tests for ReportActionIgniterEJB class. Tests are running inside
 * container.
 *
 * @author jajuutin
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@ApplyScriptAfter(value = {"set_referential_integrity_false.sql"})
@UsingDataSet(value = {"build_group_dataset.yml"})
public class ReportActionIgniterEJBIT {

    private static final Logger log = LoggerFactory.getLogger(ReportActionIgniterEJBIT.class);
    @Inject
    private ReportActionIgniterEJB raiEJB;
    private Wiser smtp;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return EJBDeploymentCreator.createEJBDeployment("pom.xml");
    }

    @After
    public void tearDown() {
        if (smtp != null) {
            smtp.stop();
        }
    }

    @Test(expected = NotFoundException.class)
    public void igniteBuildGroupReportActionsNotFound() throws NotFoundException {
        raiEJB.igniteBuildGroupReportActions(Long.MIN_VALUE);
    }

    @Test
    public void igniteBuildGroupReportActions() throws NotFoundException, MessagingException {
        startSmtp();

        raiEJB.igniteBuildGroupReportActions(2L);

        Assert.assertEquals("Mail count should match!", 1, smtp.getMessages().size());
        WiserMessage message = smtp.getMessages().get(0);
        MimeMessage mimeMessage = message.getMimeMessage();
        Address[] recipients = mimeMessage.getAllRecipients();
        Assert.assertEquals("Recipient count should match!", 1, recipients.length);
        Assert.assertEquals("Recipient should match!", "some@test.mail", recipients[0].toString());
        Assert.assertEquals("Subject 3", mimeMessage.getSubject());
    }

    @Test
    public void performEmailReportActionStatusNotChanged() throws NotFoundException, MessagingException {
        startSmtp();
        raiEJB.igniteBuildGroupReportActions(3L);
        Assert.assertEquals("Mail count should match!", 0, smtp.getMessages().size());
    }
    
    @Test 
    public void igniteNotificationActionsNoUnstableMerge() throws NotFoundException, MessagingException {
        
        startSmtp();
        raiEJB.igniteNotificationActions(5L);
        Assert.assertEquals("Mail count should match!", 0, smtp.getMessages().size());
    }
    
    @Test
    public void igniteNotificationActionsWithUnstableMerge() throws NotFoundException, MessagingException {
        
        startSmtp();
        raiEJB.igniteNotificationActions(6L);
        Assert.assertEquals("Mail count should match!", 1, smtp.getMessages().size());
        WiserMessage message = smtp.getMessages().get(0);
        MimeMessage mimeMessage = message.getMimeMessage();
        Address[] recipients = mimeMessage.getAllRecipients();
        Assert.assertEquals("Recipient count should match!", 1, recipients.length);
        Assert.assertEquals("Recipient should match!", "some@test.mail", recipients[0].toString());
        Assert.assertEquals("Subject 4", mimeMessage.getSubject());
        
    }

    private void startSmtp() {
        // Start smtp server listening in port 2525
        smtp = new Wiser();
        smtp.setPort(2525);
        smtp.start();
    }
}