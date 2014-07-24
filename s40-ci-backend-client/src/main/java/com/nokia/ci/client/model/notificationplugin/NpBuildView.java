/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.model.notificationplugin;

import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jajuutin
 */
@XmlRootElement
public class NpBuildView {

    @XmlElement
    private String full_url;
    @XmlElement
    private int number;
    @XmlElement
    private NpBuildPhase phase;
    @XmlElement
    private NpBuildStatus status;
    @XmlElement
    private String url;
    @XmlElement
    private Map<String, String> parameters;

    /**
     * @return the full_url
     */
    public String getFull_url() {
        return full_url;
    }

    /**
     * @param full_url the full_url to set
     */
    public void setFull_url(String full_url) {
        this.full_url = full_url;
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param buildNumber the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the phase
     */
    public NpBuildPhase getPhase() {
        return phase;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(NpBuildPhase phase) {
        this.phase = phase;
    }

    /**
     * @return the status
     */
    public NpBuildStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(NpBuildStatus status) {
        this.status = status;
    }
}
