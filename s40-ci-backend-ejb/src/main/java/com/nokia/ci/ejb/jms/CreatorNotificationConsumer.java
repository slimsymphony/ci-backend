package com.nokia.ci.ejb.jms;

import com.nokia.ci.ejb.CreatorNotification;
import com.nokia.ci.ejb.Notification;
import com.nokia.ci.ejb.exception.BackendJMSSysException;
import com.nokia.ci.ejb.exception.NotFoundException;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message driven bean for Creator Notification messages.
 *
 * @author vrouvine
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/notificationQueue"),
    @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "type = 'CREATOR'"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "CreatorNotificationConsumer"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "CreatorNotificationSubscription")
})
public class CreatorNotificationConsumer extends AbstractNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(CreatorNotificationConsumer.class);

    @Override
    protected void handleNotification(Notification notification) {
        log.info("Handling Creator Notification...");
        CreatorNotification creatorNotification = (CreatorNotification) notification;
        try {
            notificationPluginEJB.processCreatorNotification(creatorNotification);
        } catch (NotFoundException ex) {
            /*
             * If resource not found we want to redeliver this message. It might be that database is not ready yet
             * when there is lots of changes and files involved.
             */
            throw new BackendJMSSysException("Data might not be consistent yet. Message will be redelivered.", ex);
        }
    }
}
