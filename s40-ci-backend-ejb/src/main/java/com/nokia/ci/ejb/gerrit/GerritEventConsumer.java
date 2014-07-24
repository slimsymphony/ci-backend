/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.gerrit;

import com.google.gson.Gson;
import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.gerrit.model.CurrentPatchSet;
import com.nokia.ci.ejb.gerrit.model.GerritDetail;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.JobTriggerType;
import com.nokia.ci.ejb.util.GerritUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/gerritEventQueue"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "GerritEventConsumer"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "GerritEventSubscription")
})
public class GerritEventConsumer implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(GerritEventConsumer.class);
    @Inject
    private GerritJobManagerEJB jobManager;
    @Inject
    private GerritEJB gerritEJB;

    @Override
    public void onMessage(Message message) {
        long startTime = System.currentTimeMillis();
        log.info("Received gerrit event message {}", message);
        TextMessage msg = (TextMessage) message;

        try {
            JSONObject obj = JSONObject.fromObject(msg.getText());
            String type = obj.getString("type");

            if (type == null) {
                log.error("Could not determine type from message, discarding message");
                return;
            }

            // Check the gerrit event type
            // TODO: What to do with the other messages?
            if (type.equals("patchset-created")) {
                parsePatchsetCreated(obj);
            } else if (type.equals("draft-published")) {
                parseDraftPublished(obj);
            } else if (type.equals("change-abandoned")) {
                log.info("Message type " + type + " not supported, discarding message");
            } else if (type.equals("change-restored")) {
                log.info("Message type " + type + " not supported, discarding message");
            } else if (type.equals("change-merged")) {
                log.info("Message type " + type + " not supported, discarding message");
            } else if (type.equals("comment-added")) {
                log.info("Message type " + type + " not supported, discarding message");
            } else if (type.equals("ref-updated")) {
                log.info("Message type " + type + " not supported, discarding message");
            } else {
                log.warn("Unknown gerrit event type: " + type + ", discarding message");
            }

        } catch (JMSException e) {
            log.error("Could not get text from message, discarding message");
        } finally {
            log.info("Gerrit event message {} done in {}ms", message, System.currentTimeMillis() - startTime);
        }
    }

    private void parseDraftPublished(JSONObject obj) {
        List<GerritDetail> gds = parseChanges(obj);
        int jobs = jobManager.startJobs(gds, JobTriggerType.AUTOMATIC).size();
        log.info("Started {} jobs from the message", jobs);
    }

    private void parsePatchsetCreated(JSONObject obj) {
        List<GerritDetail> gds = parseChanges(obj);
        int jobs = jobManager.startJobs(gds, JobTriggerType.AUTOMATIC).size();
        log.info("Started {} jobs from the message", jobs);
    }

    private List<GerritDetail> parseChanges(JSONObject obj) {
        if (!obj.containsKey("change")) {
            log.error("Message does not contain change object, discarding message");
            return null;
        }

        Gson gson = new Gson();
        GerritDetail d = GerritUtils.parseGerritDetail(obj.getString("change"));
        if (d.getCurrentPatchSet() == null) {
            CurrentPatchSet patchSet = gson.fromJson(obj.getString("patchSet"), CurrentPatchSet.class);
            d.setPatchSet(patchSet);
        }

        List<GerritDetail> changes = null;
        Gerrit gerrit = null;
        try {
            URL url = new URL(d.getUrl());
            String gerritHost = url.getHost();
            gerrit = gerritEJB.getGerritWithHostName(gerritHost);
            if (gerrit != null) {
                if (d.getCurrentPatchSet() != null) {
                    changes = gerritEJB.gerritQuery(d.getCurrentPatchSet().getRevision(), gerrit.getId());
                } else if (d.getPatchSet() != null) {
                    changes = gerritEJB.gerritQuery(d.getPatchSet().getRevision(), gerrit.getId());
                }
            }
        } catch (MalformedURLException e) {
            log.warn("Could not resolve gerrit url {} from message", d.getUrl());
        } catch (NotFoundException e) {
            log.warn("Changes were not found from gerrit {} with revision {}", gerrit, d.getPatchSet().getRevision());
        }
        if (changes == null) {
            changes = new ArrayList<GerritDetail>();
            changes.add(d);
        }

        return changes;
    }
}
