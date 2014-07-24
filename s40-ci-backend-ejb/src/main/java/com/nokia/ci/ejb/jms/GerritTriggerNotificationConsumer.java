package com.nokia.ci.ejb.jms;

import com.nokia.ci.ejb.GerritTriggerNotification;
import com.nokia.ci.ejb.Notification;
import com.nokia.ci.ejb.exception.BackendJMSAppException;
import com.nokia.ci.ejb.exception.BackendJMSSysException;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message driven bean for Gerrit Trigger Notification messages.
 *
 * @author vrouvine
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/notificationQueue"),
    @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "type = 'GERRIT_TRIGGER'"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "GerritTriggerNotificationConsumer"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "GerritTriggerNotificationSubscription")
})
public class GerritTriggerNotificationConsumer extends AbstractNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(GerritTriggerNotificationConsumer.class);

    @Override
    protected void handleNotification(Notification notification) throws BackendJMSAppException {
        log.info("Handling Gerrit Trigger Notification...");
        GerritTriggerNotification gerritTriggerNotification = (GerritTriggerNotification) notification;
        try {
            notificationPluginEJB.processGerritTriggerNotification(gerritTriggerNotification);
        } catch (InvalidArgumentException ex) {
            throw new BackendJMSAppException(ex);
        } catch (NotFoundException ex) {
            throw new BackendJMSAppException(ex);
        } catch (JobStartException ex) {
            log.warn("Handling gerrit notification failed! Cause: {}. Return notification message to queue for retry.", ex.getMessage());
            throw new BackendJMSSysException(ex);
        }
    }
}
