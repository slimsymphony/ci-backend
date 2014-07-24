/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author jajuutin
 */
@Entity
@Table(name = "CISERVER")
public class CIServer extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Lob
    private String url;
    private int port;
    private String username;
    private String password;
    private String testResultStorage;
    private String proxyServerUrl;
    private Boolean disabled = false;
    private int buildsRunning = 0;
    private String uuid;
    @ManyToMany(mappedBy = "ciServers")
    private List<Branch> branches = new ArrayList<Branch>();
    @ManyToOne
    private SlavePool slavePool;

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(Long id) {
        this.id = id;
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

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    /**
     * @return the slavePool
     */
    public SlavePool getSlavePool() {
        return slavePool;
    }

    /**
     * @param slavePool the slavePool to set
     */
    public void setSlavePool(SlavePool slavePool) {
        this.slavePool = slavePool;
    }

    public String getTestResultStorage() {
        return testResultStorage;
    }

    public void setTestResultStorage(String testResultStorage) {
        this.testResultStorage = testResultStorage;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public int getBuildsRunning() {
        return buildsRunning;
    }

    public void setBuildsRunning(int buildsRunning) {
        this.buildsRunning = buildsRunning;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getProxyServerUrl() {
        return proxyServerUrl;
    }

    public void setProxyServerUrl(String proxyServerUrl) {
        this.proxyServerUrl = proxyServerUrl;
    }
}
