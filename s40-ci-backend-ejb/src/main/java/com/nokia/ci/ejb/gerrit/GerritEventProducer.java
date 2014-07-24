/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.gerrit;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class GerritEventProducer {
    private static Logger log = LoggerFactory.getLogger(GerritEventProducer.class);
    @Resource(mappedName = "java:/ConnectionFactory")
    protected QueueConnectionFactory connectionFactory;
    @Resource(mappedName = "java:jboss/exported/jms/queue/gerritEventQueue")
    private Queue gerritEventQueue;
    
    public void queueEvent(String event) {
        Connection connection = null;
        Session session = null;
        try {
            log.info("Sending gerrit event " + event);
            long startTime = System.currentTimeMillis();
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(gerritEventQueue);
            TextMessage message = session.createTextMessage(event);
            messageProducer.send(message);
            log.info("Message {} sent successfully in {}ms", message, System.currentTimeMillis() - startTime);
        } catch(JMSException e) {
            log.error("Could not access queue, stack trace follows {}", e);
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
                try {
                    connection.close();
                } catch(JMSException e) {
                    log.error("JMS queue could not be closed, stack trace follows {}", e);
                }
            }
            log.debug("JMS session closed.");
        }
    }
    
}
