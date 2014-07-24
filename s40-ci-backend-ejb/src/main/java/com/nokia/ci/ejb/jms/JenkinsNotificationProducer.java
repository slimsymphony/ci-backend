package com.nokia.ci.ejb.jms;

import com.nokia.ci.ejb.Notification;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
public class JenkinsNotificationProducer {
    
    private static Logger log = LoggerFactory.getLogger(JenkinsNotificationProducer.class);
    
    private static final String MESSAGE_TYPE_PROPERTY = "type";
    
    @Resource(mappedName = "java:/ConnectionFactory")
    protected QueueConnectionFactory notificationFactory;
    @Resource(mappedName = "java:jboss/exported/jms/queue/notificationQueue")
    private Queue notificationQueue;
    
    
    public void sendNotification(NotificationMessageType type, Notification notification) throws JMSException {
        Connection connection = null;
        Session session = null;
        try {
            long startTime = System.currentTimeMillis();
            log.debug("Sending {} notification to queue: {}.", type, notification);
            connection = notificationFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(notificationQueue);
            ObjectMessage message = session.createObjectMessage(notification);
            message.setStringProperty(MESSAGE_TYPE_PROPERTY, type.toString());
            log.info("Sending new notification message type={} to queue!", type);
            messageProducer.send(message);
            log.info("Message {} sent successfully in {}ms", message, System.currentTimeMillis() - startTime);
        } finally {
            log.debug("Finalizing JMS session...");
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    log.error("Cannot close session", e);
                }
            }
            if (connection != null) {
                connection.close();
            }
            log.debug("JMS session closed.");
        }
        
    }
}
