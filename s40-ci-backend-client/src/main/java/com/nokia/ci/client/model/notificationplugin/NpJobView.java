/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.model.notificationplugin;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jajuutin
 */
@XmlRootElement
public class NpJobView {
    @XmlElement
    private String name;
    
    @XmlElement
    private String url;
    
    @XmlElement
    private NpBuildView build;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * @return the build
     */
    public NpBuildView getBuild() {
        return build;
    }

    /**
     * @param build the build to set
     */
    public void setBuild(NpBuildView build) {
        this.build = build;
    }
}
