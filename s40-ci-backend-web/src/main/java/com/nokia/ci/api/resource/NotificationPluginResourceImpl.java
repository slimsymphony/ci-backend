/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.resource;

import com.google.gson.Gson;
import com.nokia.ci.api.util.NotificationPluginUtil;
import com.nokia.ci.client.model.notificationplugin.NpJobView;
import com.nokia.ci.client.rest.NotificationPluginResource;
import com.nokia.ci.ejb.BuildNotification;
import com.nokia.ci.ejb.CreatorNotification;
import com.nokia.ci.ejb.GerritTriggerNotification;
import com.nokia.ci.ejb.Notification;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.BackendJMSSysException;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.jms.JenkinsNotificationProducer;
import com.nokia.ci.ejb.jms.NotificationMessageType;
import com.nokia.ci.ejb.model.BuildPhase;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Named
@RequestScoped
public class NotificationPluginResourceImpl implements NotificationPluginResource {

    private static Logger log = LoggerFactory.getLogger(NotificationPluginResourceImpl.class);
    @Inject
    JenkinsNotificationProducer jenkinsNotificationProducer;

    /**
     * Rest entry point.
     */
    @Override
    public Response processNotification(byte[] data) {
        BuildNotification n = toBuildNotification(data);

        if (n == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        /**
         * Workaround. Discard phase "COMPLETED" to avoid race condition with
         * phase "FINISHED" event.
         */
        if (n.getBuildPhase() == BuildPhase.COMPLETED) {
            log.info("Discarding COMPLETED phase message");
            return Response.ok().build();
        }

        try {
            // act based on input data
            checkBuildNotificationValidity(n);
        } catch (BackendAppException ex) {
            log.error("Build notification was not valid: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        pushToJMS(NotificationMessageType.BUILD, n);

        // respond
        return Response.ok().build();
    }

    @Override
    public Response processCreatorNotification(byte[] data) {
        CreatorNotification n = toCreatorNotification(data);

        if (n == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        /**
         * Workaround. Discard phase "COMPLETED" to avoid race condition with
         * phase "FINISHED" event.
         */
        if (n.getBuildPhase() == BuildPhase.COMPLETED) {
            log.info("Discarding COMPLETED phase message");
            return Response.ok().build();
        }

        try {
            // act based on input data
            checkCreatorNotificationValidity(n);
        } catch (BackendAppException ex) {
            log.error("Creator notification was not valid: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        pushToJMS(NotificationMessageType.CREATOR, n);

        // respond
        return Response.ok().build();
    }

    @Override
    public Response processGerritTriggerNotification(byte[] data) {
        GerritTriggerNotification n = toGerritTriggerNotification(data);

        if (n == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        /**
         * Discard all non-STARTED phase messages.
         */
        if (n.getBuildPhase() != BuildPhase.STARTED) {
            log.info("Discarding phase message. Trigger listener only interested about STARTED phase");
            return Response.ok().build();
        }

        try {
            // act based on input data            
            checkGerritTriggerNotificationValidity(n);
        } catch (BackendAppException ex) {
            log.error("Gerrit trigger notification was not valid: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        pushToJMS(NotificationMessageType.GERRIT_TRIGGER, n);

        // respond
        return Response.ok().build();
    }

    private BuildNotification toBuildNotification(byte[] data) {
        NpJobView jobView = getJobView(data);
        if (jobView == null) {
            return null;
        }

        BuildNotification n = NotificationPluginUtil.toBuildNotification(jobView);
        return n;
    }

    private CreatorNotification toCreatorNotification(byte[] data) {
        NpJobView jobView = getJobView(data);
        if (jobView == null) {
            return null;
        }

        CreatorNotification n = NotificationPluginUtil.toCreatorNotification(jobView);
        return n;
    }

    private GerritTriggerNotification toGerritTriggerNotification(byte[] data) {
        NpJobView jobView = getJobView(data);
        if (jobView == null) {
            return null;
        }

        GerritTriggerNotification n =
                NotificationPluginUtil.toGerritTriggerNotification(jobView);

        return n;
    }

    private NpJobView getJobView(byte[] data) {
        String jsonContent = new String(data);
        log.info("Processing notification json. Content: {}", jsonContent);

        // parse from json to java pojos
        Gson g = new Gson();

        NpJobView jobView = null;
        try {
            jobView = g.fromJson(jsonContent, NpJobView.class);
        } catch (com.google.gson.JsonSyntaxException e) {
        }

        if (jobView == null || jobView.getBuild() == null) {
            log.warn("Bad json content: {}", jsonContent);
            return null;
        }

        return jobView;
    }

    private void checkCreatorNotificationValidity(BuildNotification n) throws InvalidArgumentException {
        if (n == null) {
            throw new InvalidArgumentException(
                    "Validity check failed. notification is null");
        }

        if (n.getBuildPhase() == null) {
            throw new InvalidArgumentException(
                    "Validity check failed. build phase is null");
        }

        if (n.getBuildGroupId() == null) {
            throw new InvalidArgumentException("CI20_BUILD_GROUP_ID not specified for creator notification");
        }
    }

    private void checkBuildNotificationValidity(BuildNotification n)
            throws InvalidArgumentException {

        if (n == null) {
            throw new InvalidArgumentException(
                    "Validity check failed. notification is null");
        }

        if (n.getBuildPhase() == null) {
            throw new InvalidArgumentException(
                    "Validity check failed. build phase is null");
        }
    }

    private void checkGerritTriggerNotificationValidity(GerritTriggerNotification n)
            throws InvalidArgumentException {

        if (n == null) {
            throw new InvalidArgumentException(
                    "Validity check failed. notification is null");
        }

        if (n.getBuildPhase() == null) {
            throw new InvalidArgumentException(
                    "Validity check failed. build phase is null");
        }

        if (n.getMonitor() == null) {
            throw new InvalidArgumentException(
                    "Validity check failed. monitor is null");
        }

        if (n.getTrigger() == null) {
            throw new InvalidArgumentException(
                    "Validity check failed.  trigger is null");
        }

        if (n.getParameters() == null) {
            throw new InvalidArgumentException(
                    "Validity check failed.  parameter map is null");
        }
    }

    /**
     * Push to queue.
     */
    private void pushToJMS(NotificationMessageType type, Notification notification) {        
        try {
            jenkinsNotificationProducer.sendNotification(type, notification);
        } catch (JMSException ex) {
            log.error("Error sending notification into JMS failed!", ex);
            throw new BackendJMSSysException(ex);
        }
    }
}
