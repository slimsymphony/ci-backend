/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class SlaveView
 *
 * @author aklappal
 * @since Jan 21, 2013
 */
@XmlRootElement
public class SlaveView {

    @XmlElement
    private String name;
    @XmlElement
    private String username;
    @XmlElement
    private String password;
    @XmlElement
    private String host;
    @XmlElement
    private int port;
    @XmlElement
    private int executors;
    @XmlElement
    private String currentMaster;
    @XmlElement
    private String workspace;
    @XmlElement
    private String startScript;
    @XmlElement
    private String endScript;

    @Override
    public String toString() {
        return "SlaveView{"
                + "name='" + name + '\''
                + ", host='" + host + '\''
                + ", username='" + username + '\''
                + ", password='" + password + '\''
                + ", port='" + port + '\''
                + ", executors='" + executors + '\''
                + ", workspace='" + workspace + '\''
                + ", startScript='" + startScript + '\''
                + ", endScript='" + endScript + '\''
                + '}';
    }

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
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the executors
     */
    public int getExecutors() {
        return executors;
    }

    /**
     * @param executors the executors to set
     */
    public void setExecutors(int executors) {
        this.executors = executors;
    }

    /**
     * @return the currentMaster
     */
    public String getCurrentMaster() {
        return currentMaster;
    }

    /**
     * @param currentMaster the currentMaster to set
     */
    public void setCurrentMaster(String currentMaster) {
        this.currentMaster = currentMaster;
    }

    /**
     * @return the workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * @param workspace the workspace to set
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * @return the startScript
     */
    public String getStartScript() {
        return startScript;
    }

    /**
     * @param startScript the startScript to set
     */
    public void setStartScript(String startScript) {
        this.startScript = startScript;
    }

    /**
     * @return the endScript
     */
    public String getEndScript() {
        return endScript;
    }

    /**
     * @param endScript the endScript to set
     */
    public void setEndScript(String endScript) {
        this.endScript = endScript;
    }
}
