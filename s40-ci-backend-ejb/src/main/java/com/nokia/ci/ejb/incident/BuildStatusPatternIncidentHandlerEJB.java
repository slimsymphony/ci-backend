package com.nokia.ci.ejb.incident;

import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.event.BuildGroupFinishedEvent;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.StatusTriggerPattern;
import com.nokia.ci.ejb.util.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ejb.Asynchronous;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 3/8/13 Time: 2:00 PM To change
 * this template use File | Settings | File Templates.
 */
@Stateless
@LocalBean
public class BuildStatusPatternIncidentHandlerEJB extends BaseIncidentHandler {

    private static Logger log = LoggerFactory.getLogger(BuildStatusPatternIncidentHandlerEJB.class);
    private String DESC = "BuildGroup status trigger pattern '%s' was reach for verification '%s'";
    @EJB
    private JobEJB jobEJB;
    @EJB
    private BuildGroupEJB buildGroupEJB;

    @Asynchronous
    public void buildGroupFinished(@Observes(during = TransactionPhase.AFTER_SUCCESS) @BuildGroupFinishedEvent Long id) {
        log.info("Handling status pattern for buildGroup: {}", id);
        try {
            BuildGroup bg = buildGroupEJB.read(id);
            log.info("Loaded job : {}", bg.getJob());
            handle(bg.getJob());
        } catch (NotFoundException e) {
            log.error("BuildGroup id : {} not found", id);
        }
    }

    private void handle(Job job) {
        List<StatusTriggerPattern> statusTriggerPatterns = jobEJB.getStatusTriggerPatterns(job.getId());
        if (statusTriggerPatterns != null && statusTriggerPatterns.size() > 0) {
            for (StatusTriggerPattern statusTriggerPattern : statusTriggerPatterns) {
                List<BuildGroup> buildHistory = jobEJB.getLastBuildGroups(job.getId(), statusTriggerPattern.getPattern().length());

                if (hasBuildsSequence(buildHistory, statusTriggerPattern)) {
                    createLogEntryAndSave(job, statusTriggerPattern);
                }
            }
        }
    }

    private List<BuildStatus> createPatternList(String pattern) {
        int patternLength = pattern.length();
        List<BuildStatus> ret = new ArrayList<BuildStatus>();;
        for (int i = 0; i < patternLength; i++) {
            if (pattern.charAt(i) == 'F') {
                ret.add(BuildStatus.FAILURE);
            } else {
                ret.add(BuildStatus.SUCCESS);
            }
        }
        return ret;
    }

    private void createLogEntryAndSave(Job job, StatusTriggerPattern sequence) {
        String jobName = job.getDisplayName();
        String description = String.format(DESC, sequence.getPattern(), jobName);
        createIncident(IncidentType.DELIVERY_CHAIN, description);
        log.info("Log entry has been created for sequence {} and jobName {}", sequence.getPattern(), jobName);
    }

    private boolean hasBuildsSequence(List<BuildGroup> buildGroups, StatusTriggerPattern statusTriggerPattern) {
        String pattern = statusTriggerPattern.getPattern();
        List<BuildStatus> expectedBuildStatuses = createPatternList(pattern);
        log.info("Comparing buildHistory against expected pattern {}", pattern);
        Collections.reverse(expectedBuildStatuses);
        if (buildGroups.size() != expectedBuildStatuses.size()) {
            return false;
        }
        for (int i = 0; i < buildGroups.size(); i++) {
            log.info("Reading build group status {}", buildGroups.get(i).getStatus());
            if (!buildGroups.get(i).getStatus().equals(expectedBuildStatuses.get(i))) {
                return false;
            }
        }
        return true;
    }
}
