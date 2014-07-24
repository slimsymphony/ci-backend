package com.nokia.ci.ejb.jms;

import com.nokia.ci.ejb.Notification;
import com.nokia.ci.ejb.NotificationPluginEJB;
import com.nokia.ci.ejb.exception.BackendJMSAppException;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message handling base class for Notification messages.
 *
 * @author vrouvine
 */
public abstract class AbstractNotificationConsumer implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractNotificationConsumer.class);
    @Inject
    protected NotificationPluginEJB notificationPluginEJB;

    @Override
    public void onMessage(Message message) {
        long startTime = System.currentTimeMillis();
        log.info("Received notification message {}", message);
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Notification notification = (Notification) objectMessage.getObject();
            handleNotification(notification);
        } catch (BackendJMSAppException bjae) {
            log.error("Notification message can not be handled! Discarding message " + objectMessage, bjae);
        } catch (JMSException jmsex) {
            log.error("Retrieving object from message failed! Discarding message " + objectMessage, jmsex);
        }
        log.info("Notification message {} done in {}ms", message, System.currentTimeMillis() - startTime);
    }

    protected abstract void handleNotification(Notification notification) throws BackendJMSAppException;
}
