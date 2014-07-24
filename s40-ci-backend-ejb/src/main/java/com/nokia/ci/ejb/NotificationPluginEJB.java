/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.ejb.event.IncidentEventContent;
import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.InvalidPhaseException;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildGroupCIServer;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.CIServer;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.util.RelationUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link Build} object operations.
 *
 * @author vrouvine
 */
@LocalBean
@Stateless
public class NotificationPluginEJB {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(NotificationPluginEJB.class);
    @EJB
    BuildEJB buildEJB;
    @EJB
    JobEJB jobEJB;
    @EJB
    BuildGroupEJB buildGroupEJB;
    @EJB
    CIServerEJB ciServerEJB;
    @EJB
    BuildGroupCIServerEJB buildGroupCIServerEJB;
    @Resource
    protected SessionContext ctx;
    @Inject
    Event<IncidentEventContent> incidentEvents;
    private IncidentEventContent event;
    private static String B_INVALID_PHASE_INCIDENT = "Invalid build PHASE. Build not found when FINISHED notification received: %s";
    private static String B_FINISHED_BEFORE_STARTED_INCIDENT = "Build was FINISHED before STARTED notification received: %s";
    private static String B_MULTIPLE_FINISHED_NOTIFICATIONS = "Multiple FINISHED notifications received for build (%s): %s";
    private static String BG_FINISHED_BEFORE_STARTED_INCIDENT = "BuildGroup was FINISHED before STARTED notification received: %s";

    /**
     * This notification is received when Jenkins has loaded requested job
     * configurations and is ready to run actual jobs.
     */
    public void processCreatorNotification(CreatorNotification n) throws NotFoundException {
        log.info("Processing creator notification: {}", n.toString());

        if (n.getBuildPhase() != BuildPhase.FINISHED) {
            log.info("Discarding notification");
            return;
        }
        buildGroupEJB.start(n.getBuildGroupId());
    }

    public BuildGroup processGerritTriggerNotification(GerritTriggerNotification n)
            throws InvalidArgumentException, NotFoundException, JobStartException {

        log.info("Processing gerrit trigger notification: {}", n.toString());

        // find monitor job
        Job monitorJob = jobEJB.getJobWithName(n.getMonitor());

        // resolve server to do work
        Branch branch = monitorJob.getBranch();
        if (branch == null) {
            throw new NotFoundException("No branch found for job:" + n.getMonitor());
        }
        List<CIServer> ciServers = branch.getCiServers();
        CIServer ciServer = ciServerEJB.resolveCIServer(ciServers);
        if (ciServer == null) {
            throw new NotFoundException("No server found for branch:" + branch);
        }

        // create build group
        BuildGroup buildGroup = new BuildGroup();
        buildGroup.setPhase(BuildPhase.STARTED);
        buildGroup.setProjectId(branch.getProject().getId());
        buildGroup.setStatus(BuildStatus.SUCCESS);
        buildGroup.setStartTime(new Date());
        buildGroup.setJobName(monitorJob.getName());
        buildGroup.setJobDisplayName(monitorJob.getDisplayName());
        buildGroup.setGroupUid(UUID.randomUUID().toString());
        buildGroup.setBranchType(monitorJob.getBranch().getType());
        RelationUtil.relate(monitorJob, buildGroup);
        buildGroupEJB.create(buildGroup);

        //create build group ci server
        BuildGroupCIServer buildGroupCIServer = new BuildGroupCIServer();
        buildGroupCIServer.setUrl(ciServer.getUrl());
        buildGroupCIServer.setPort(ciServer.getPort());
        buildGroupCIServer.setUsername(ciServer.getUsername());
        buildGroupCIServer.setPassword(ciServer.getPassword());
        buildGroupCIServer.setTestResultStorage(ciServer.getTestResultStorage());
        buildGroupCIServer.setProxyServerUrl(ciServer.getProxyServerUrl());
        RelationUtil.relate(buildGroupCIServer, buildGroup);

        // start trigger job.
        Map<String, String> params = new HashMap<String, String>();
        params.putAll(n.getParameters());
        params.put(CIParam.BUILD_GROUP_ID.toString(), buildGroup.getId().toString());

        buildGroupCIServerEJB.build(buildGroupCIServer, n.getTrigger(), params);

        // done.
        return buildGroup;
    }

    public Build processBuildNotification(BuildNotification n) throws
            InvalidArgumentException, NotFoundException, InvalidPhaseException {

        log.info("Processing build notification: {}", n.toString());

        if (n.getBuildId() != null && n.getBuildId() > 0) {
            return processBuildNotificationWithId(n);
        }

        if (n.getMonitor() != null) {
            return processBuildNotificationWithMonitor(n);
        }

        throw new InvalidArgumentException("No necessary parameters found");
    }

    private Build processBuildNotificationWithId(BuildNotification n)
            throws NotFoundException {

        log.info("processing build notification with build id {}.",
                n.getBuildId());

        // read
        Build build = buildEJB.read(n.getBuildId());

        // update according to notification
        updateBuild(build, n);

        // if finished then trigger finalization
        if (build.getPhase() == BuildPhase.FINISHED) {
            if (n.getParameters() != null) {
                buildEJB.processOutputParameters(build.getId(), n.getParameters());
            }

            buildEJB.finalizeBuild(build.getId(), build.getStatus());
        }

        return build;
    }

    /**
     * Handle custom notification created for september 2012 sprint. This method
     * handles notifications that have build monitor specified. These
     * notifications originate from SCV and DBV builds.
     *
     * This method is to be removed in future sprints when job creation is
     * controlled by BE. Strictly restrict this code from normal notification
     * handling and logic for easy and regression free future removal.
     */
    private Build processBuildNotificationWithMonitor(BuildNotification n)
            throws NotFoundException, InvalidArgumentException, InvalidPhaseException {

        if (n.getBuildGroupId() == null) {
            throw new InvalidArgumentException("Monitor specified but build group not found");
        }

        log.info("Processing build notification with monitor {} and build group {}",
                n.getMonitor(), n.getBuildGroupId());

        BuildGroup buildGroup = buildGroupEJB.read(n.getBuildGroupId());

        if (buildGroup.getJobName().equals(n.getJobName())) {
            // Notification is for monitor. Update build group status.
            updateBuildGroup(buildGroup, n);
            return null;
        }

        /**
         * retrieve target build.
         *
         * monitor build group is guaranteed to exist since it is created when
         * gerrit trigger arrives. if this notification is not for monitor, then
         * make sure that build exists.
         */
        Build build;

        try {
            build = getBuild(buildGroup, n.getJobName());
        } catch (NotFoundException ex) {
            if (n.getBuildPhase() == BuildPhase.FINISHED) {
                String msg = String.format(B_INVALID_PHASE_INCIDENT, n.toString());
                printErrorAndReportIncident(msg);
            }

            log.info("Creating new build cause: {}", ex.getMessage());
            build = new Build();
            RelationUtil.relate(buildGroup, build);
        }

        // Update build.
        updateBuild(build, n);

        log.info("Ended processing build notification with monitor {} and build group {}",
                n.getMonitor(), n.getBuildGroupId());

        // Return modified.
        return build;
    }

    public String createJobURL(String notificationJobURL, String notificationBuildURL) {
        if (StringUtils.isEmpty(notificationJobURL) || StringUtils.isEmpty(notificationBuildURL)) {
            return "";
        }
        int i = notificationBuildURL.indexOf(notificationJobURL);
        if (i < 0) {
            return "";
        }
        return notificationBuildURL.substring(0, i) + notificationJobURL;
    }

    private void updateBuild(Build build, BuildNotification n) {
        if (n.getBuildPhase() == BuildPhase.STARTED) {
            // Handle STARTED.

            // Set start time. do not change if already set.
            if (build.getStartTime() == null) {
                build.setStartTime(new Date());
            }

            // Handle situation where notifications are received in
            // reverse order (STARTED after FINISHED). This prevents
            // changing FINISHED status to STARTED.
            if (build.getPhase() != BuildPhase.FINISHED) {
                build.setPhase(BuildPhase.STARTED);
            } else {
                String msg = String.format(B_FINISHED_BEFORE_STARTED_INCIDENT, n.toString());
                printErrorAndReportIncident(msg);
            }
        } else if (n.getBuildPhase() == BuildPhase.FINISHED) {
            // Handle FINISHED.

            if (build.getPhase() == BuildPhase.FINISHED) {
                String msg = String.format(B_MULTIPLE_FINISHED_NOTIFICATIONS, build.toString(), n.toString());
                printErrorAndReportIncident(msg);
            }

            /*
             * BE might have missed start event(e.g. downtime). if so then
             * update starttime also.
             */
            if (build.getStartTime() == null) {
                build.setStartTime(new Date());
            }

            // do not change if already set.
            if (build.getEndTime() == null) {
                build.setEndTime(new Date());
            }

            build.setStatus(n.getBuildStatus());
            build.setPhase(BuildPhase.FINISHED);
        }

        // Handle cases where end time is before start time (negative durations in UI)
        if (build.getEndTime() != null && build.getStartTime() != null
                && build.getEndTime().before(build.getStartTime())) {
            build.setStartTime(build.getEndTime());
        }

        // Update these fields with both STARTED and FINISHED.
        build.setUrl(n.getBuildUrl());
        build.setBuildNumber(n.getBuildNumber());
        String jobUrl = createJobURL(n.getJobUrl(), n.getBuildUrl());
        build.setJobUrl(jobUrl);
        build.setJobName(n.getJobName());
        build.setJobDisplayName(n.getJobDisplayName());
    }

    private void updateBuildGroup(BuildGroup buildGroup, BuildNotification n) {
        if (n.getBuildPhase() == BuildPhase.STARTED) {
            // Handle STARTED.

            // Set start time. do not change if already set.
            if (buildGroup.getStartTime() == null) {
                buildGroup.setStartTime(new Date());
            }

            // Handle situation where notifications are received in
            // reverse order (STARTED after FINISHED). This prevents
            // changing FINISHED status to STARTED.
            if (buildGroup.getPhase() != BuildPhase.FINISHED) {
                buildGroup.setPhase(BuildPhase.STARTED);
            } else {
                String msg = String.format(BG_FINISHED_BEFORE_STARTED_INCIDENT, n.toString());
                printErrorAndReportIncident(msg);
            }
        } else if (n.getBuildPhase() == BuildPhase.FINISHED) {
            // Handle FINISHED.

            /*
             * BE might have missed start event(e.g. downtime). if so then
             * update starttime also.
             */
            if (buildGroup.getStartTime() == null) {
                buildGroup.setStartTime(new Date());
            }

            // do not change if already set.
            if (buildGroup.getEndTime() == null) {
                buildGroup.setEndTime(new Date());
            }

            buildGroup.setStatus(n.getBuildStatus());
            buildGroup.setPhase(BuildPhase.FINISHED);
        }

        // Handle cases where end time is before start time (negative durations in UI)
        if (buildGroup.getEndTime() != null && buildGroup.getStartTime() != null
                && buildGroup.getEndTime().before(buildGroup.getStartTime())) {
            buildGroup.setStartTime(buildGroup.getEndTime());
        }

        // Update these fields with both STARTED and FINISHED.
        buildGroup.setUrl(n.getBuildUrl());
        buildGroup.setJobName(n.getJobName());
        buildGroup.setJobDisplayName(n.getJobDisplayName());
    }

    private Build getBuild(BuildGroup buildGroup, String jobName)
            throws NotFoundException {

        for (Build build : buildGroup.getBuilds()) {
            if (build.getJobName().equals(jobName)) {
                return build;
            }
        }

        throw new NotFoundException("Build not found for job " + jobName
                + " from buildgroup " + buildGroup);
    }

    private void printErrorAndReportIncident(String msg) {
        log.error("*** " + msg + " ***");
        event = new IncidentEventContent(IncidentType.DELIVERY_CHAIN, msg);
        incidentEvents.fire(event);
    }
}
