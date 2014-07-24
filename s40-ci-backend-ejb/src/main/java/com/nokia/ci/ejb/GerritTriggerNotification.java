/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BuildPhase;
import java.util.Map;

/**
 *
 * @author jajuutin
 */
public class GerritTriggerNotification extends Notification {

    private String trigger;
    private String monitor;
    private Map<String, String> parameters;
    private BuildPhase buildPhase;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("trigger:").append(trigger);
        sb.append(",monitor:").append(monitor);
        sb.append(",buildPhase:").append(buildPhase);
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                sb.append(",");
                sb.append(entry.getKey());
                sb.append(":");
                sb.append(entry.getValue());
            }
        }

        return sb.toString();
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getMonitor() {
        return monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }

    public BuildPhase getBuildPhase() {
        return buildPhase;
    }

    public void setBuildPhase(BuildPhase buildPhase) {
        this.buildPhase = buildPhase;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
