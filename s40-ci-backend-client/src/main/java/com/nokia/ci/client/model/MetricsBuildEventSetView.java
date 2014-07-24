package com.nokia.ci.client.model;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * View model for metrics event set.
 * @author jajuutin
 */
@XmlRootElement
public class MetricsBuildEventSetView {
    
    @XmlElement
    private Long buildId;
    @XmlElement
    private String executor;
    @XmlElement
    private List<BuildEventView> events;

    /**
     * @return the buildId
     */
    public Long getBuildId() {
        return buildId;
    }

    /**
     * @param buildId the buildId to set
     */
    public void setBuildId(Long buildId) {
        this.buildId = buildId;
    }

    /**
     * @return the executor
     */
    public String getExecutor() {
        return executor;
    }

    /**
     * @param executor the executor to set
     */
    public void setExecutor(String executor) {
        this.executor = executor;
    }

    /**
     * @return the events
     */
    public List<BuildEventView> getEvents() {
        return events;
    }

    /**
     * @param events the events to set
     */
    public void setEvents(List<BuildEventView> events) {
        this.events = events;
    }
}
