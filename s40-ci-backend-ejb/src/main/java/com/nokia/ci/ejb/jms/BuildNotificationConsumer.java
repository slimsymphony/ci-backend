package com.nokia.ci.ejb.jms;

import com.nokia.ci.ejb.BuildNotification;
import com.nokia.ci.ejb.Notification;
import com.nokia.ci.ejb.exception.BackendJMSAppException;
import com.nokia.ci.ejb.exception.BackendJMSSysException;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.InvalidPhaseException;
import com.nokia.ci.ejb.exception.NotFoundException;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message driven bean for Build Notification messages.
 *
 * @author vrouvine
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/notificationQueue"),
    @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "type = 'BUILD'"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "BuildNotificationConsumer"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "BuildNotificationSubscription")
})
public class BuildNotificationConsumer extends AbstractNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(BuildNotificationConsumer.class);

    @Override
    protected void handleNotification(Notification notification) throws BackendJMSAppException {
        log.info("Handling Build Notification...");
        BuildNotification buildNotification = (BuildNotification) notification;
        try {
            notificationPluginEJB.processBuildNotification(buildNotification);
        } catch (InvalidArgumentException iae) {
            throw new BackendJMSAppException(iae);
        } catch (NotFoundException nfe) {
            throw new BackendJMSAppException(nfe);
        } catch (InvalidPhaseException ipe) {
            throw new BackendJMSSysException(ipe);
        }
    }
}
