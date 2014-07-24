package com.nokia.ci.ejb.jms;

import com.nokia.ci.ejb.ChangeEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.exception.BackendJMSSysException;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Change;
import java.util.List;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message driven bean for job start messages.
 *
 * @author vrouvine
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/jobStartQueue"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "JobStartNotificationConsumer"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "JobStartNotificationSubscription")
})
public class JobStartConsumer implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(JobStartConsumer.class);
    @Inject
    private JobEJB jobEJB;
    @Inject
    private ChangeEJB changeEJB;

    @Override
    public void onMessage(Message message) {
        long startTime = System.currentTimeMillis();
        log.info("Received job start message {}", message);
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            JobStartMessage jobStartMessage = (JobStartMessage) objectMessage.getObject();
            List<Change> changes = jobStartMessage.getChanges();
            if (changes != null) {
                changes = changeEJB.getChanges(changes);
            }
            jobEJB.start(jobStartMessage.getJobId(), jobStartMessage.getRefspec(), jobStartMessage.getCommitId(), changes);
        } catch (JobStartException ex) {
            log.warn("Handling job start message failed! Cause: {}. Return job start message to queue for retry.", ex.getMessage());
            throw new BackendJMSSysException(ex);
        } catch (NotFoundException nfe) {
            log.error("Can not handle job start message! Cause: {}. Discading message!", nfe.getMessage());
        } catch (JMSException jmsex) {
            log.error("Retrieving object from message " + objectMessage + " failed! Discarding message!", jmsex);
        } finally {
            log.info("Job start message {} done in {}ms", message, System.currentTimeMillis() - startTime);
        }
    }
}
