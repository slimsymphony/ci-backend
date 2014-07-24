/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.nokia.ci.client.model.notificationplugin.NpBuildPhase;
import com.nokia.ci.client.model.notificationplugin.NpBuildStatus;
import com.nokia.ci.client.model.notificationplugin.NpBuildView;
import com.nokia.ci.client.model.notificationplugin.NpJobView;
import com.nokia.ci.ejb.BuildNotification;
import com.nokia.ci.ejb.CreatorNotification;
import com.nokia.ci.ejb.GerritTriggerNotification;
import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
public class NotificationPluginUtil {

    private static Logger log = LoggerFactory.getLogger(NotificationPluginUtil.class);

    private NotificationPluginUtil() {
    }

    /**
     * Convert build status from notification plugin format to format supported
     * by entity model status.
     *
     * @param buildViewStatus
     * @return correlating status for backend entity model.
     */
    public static BuildStatus convertBuildStatus(NpBuildStatus buildStatus) {
        if (buildStatus == null) {
            return null;
        }

        BuildStatus status = null;

        if (buildStatus == NpBuildStatus.ABORTED) {
            status = BuildStatus.ABORTED;
        } else if (buildStatus == NpBuildStatus.NOT_BUILT) {
            status = BuildStatus.NOT_BUILT;
        } else if (buildStatus == NpBuildStatus.SUCCESS) {
            status = BuildStatus.SUCCESS;
        } else if (buildStatus == NpBuildStatus.UNSTABLE) {
            status = BuildStatus.UNSTABLE;
        } else {
            status = BuildStatus.FAILURE;
        }

        log.debug("Converted build status {} to {}", buildStatus, status);
        return status;
    }

    /**
     * Convert build phase from notification plugin format to format supported
     * by entity model phase.
     *
     * Later this method will make more sense, when entities are implemented to
     * store phase as enum.
     *
     * @param buildViewPhase
     * @return correlating phase for backend entity model.
     */
    public static BuildPhase convertBuildPhase(NpBuildPhase buildPhase) {
        if (buildPhase == null) {
            return null;
        }

        BuildPhase phase = null;

        if (buildPhase == NpBuildPhase.STARTED) {
            phase = BuildPhase.STARTED;
        } else if (buildPhase == NpBuildPhase.COMPLETED) {
            phase = BuildPhase.COMPLETED;
        } else if (buildPhase == NpBuildPhase.FINISHED) {
            phase = BuildPhase.FINISHED;
        }
        log.debug("Converted build phase {} to {}", buildPhase, phase);
        return phase;
    }

    public static CreatorNotification toCreatorNotification(NpJobView jobView) {
        if (jobView == null) {
            return null;
        }
        CreatorNotification n = new CreatorNotification();
        populateBuildNotification(jobView, n);
        return n;
    }

    public static BuildNotification toBuildNotification(NpJobView jobView) {
        if (jobView == null) {
            return null;
        }
        BuildNotification n = new BuildNotification();
        populateBuildNotification(jobView, n);
        return n;
    }

    private static void populateBuildNotification(NpJobView jobView, BuildNotification n) {
        n.setJobName(jobView.getName());
        n.setJobUrl(jobView.getUrl());
        Map<String, String> params = jobView.getBuild().getParameters();
        if (params != null) {
            n.setJobDisplayName(params.get(CIParam.DISPLAY_NAME.toString()));
            n.setBuildRefSpec(params.get(CIParam.GERRIT_REFSPEC.toString()));
            String buildIdString = params.get(CIParam.BUILD_ID.toString());
            if (buildIdString != null) {
                n.setBuildId(Long.parseLong(buildIdString));
            }
            n.setMonitor(params.get(CIParam.MONITOR.toString()));
            String buildGroupIdString = params.get(CIParam.BUILD_GROUP_ID.toString());
            if (buildGroupIdString != null) {
                n.setBuildGroupId(Long.parseLong(buildGroupIdString));
            }
            n.setParameters(new HashMap<String, String>(params));
        }

        NpBuildView buildView = jobView.getBuild();
        if (buildView != null) {
            n.setBuildNumber(buildView.getNumber());
            n.setBuildPhase(convertBuildPhase(buildView.getPhase()));
            n.setBuildStatus(convertBuildStatus(buildView.getStatus()));
            n.setBuildUrl(buildView.getFull_url());
        }
    }

    public static GerritTriggerNotification toGerritTriggerNotification(NpJobView jobView) {
        if (jobView == null) {
            return null;
        }

        NpBuildView buildView = jobView.getBuild();
        if (buildView == null) {
            return null;
        }

        Map<String, String> params = buildView.getParameters();
        if (params == null) {
            return null;
        }

        GerritTriggerNotification n = new GerritTriggerNotification();

        n.setMonitor(params.get(CIParam.MONITOR.toString()));
        n.setTrigger(params.get(CIParam.TRIGGER.toString()));
        n.setBuildPhase(convertBuildPhase(buildView.getPhase()));
        n.setParameters(new HashMap<String, String>(params));

        return n;
    }
}
