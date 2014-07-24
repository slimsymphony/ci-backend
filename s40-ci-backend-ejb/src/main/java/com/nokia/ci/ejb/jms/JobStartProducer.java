package com.nokia.ci.ejb.jms;

import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Change;

import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
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
public class JobStartProducer {

    private static Logger log = LoggerFactory.getLogger(JobStartProducer.class);
    @Resource(mappedName = "java:/ConnectionFactory")
    protected QueueConnectionFactory connectionFactory;
    @Resource(mappedName = "java:jboss/exported/jms/queue/jobStartQueue")
    private Queue jobStartQueue;
    @EJB
    private JobEJB jobEJB;

    public void sendJobStart(Long jobId, String refspec, String commitId, List<Change> changes) throws JobStartException {
        Connection connection = null;
        Session session = null;
        try {
            long startTime = System.currentTimeMillis();
            // set last run timestamp
            jobEJB.updateAtomicLastRun(jobId);
            log.debug("Sending job start message to queue: JobId={}, Changes: {}", jobId, changes);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(jobStartQueue);
            JobStartMessage jobStartMessage = new JobStartMessage(jobId, refspec, commitId, changes);
            ObjectMessage message = session.createObjectMessage(jobStartMessage);
            log.info("Sending new job start message job id {} to queue!", jobId);
            messageProducer.send(message);
            log.info("Message {} sent successfully in {}ms", message, System.currentTimeMillis() - startTime);
        } catch (JMSException e) {
            log.error("Could not access queue, stack trace follows {}", e);
            throw new JobStartException(e.getMessage());
        } catch (NotFoundException e) {
            log.error("Could not find job, stack trace follows {}", e);
            throw new JobStartException(e.getMessage());
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
                } catch (JMSException e) {
                    log.error("JMS queue could not be closed, stack trace follows {}", e);
                    throw new JobStartException(e.getMessage());
                }
            }
            log.debug("JMS session closed.");
        }

    }
}
