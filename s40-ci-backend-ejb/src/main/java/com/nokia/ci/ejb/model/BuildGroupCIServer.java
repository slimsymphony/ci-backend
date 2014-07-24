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
@Table(name = "BUILD_GROUP_CISERVER")
public class BuildGroupCIServer extends BaseEntity {

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
    private String ciServerUuid;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private BuildGroup buildGroup;

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

    public BuildGroup getBuildGroup() {
        return buildGroup;
    }

    public void setBuildGroup(BuildGroup buildGroup) {
        this.buildGroup = buildGroup;
    }

    public String getTestResultStorage() {
        return testResultStorage;
    }

    public void setTestResultStorage(String testResultStorage) {
        this.testResultStorage = testResultStorage;
    }

    public String getCiServerUuid() {
        return ciServerUuid;
    }

    public void setCiServerUuid(String ciServerUuid) {
        this.ciServerUuid = ciServerUuid;
    }

    public String getProxyServerUrl() {
        return proxyServerUrl;
    }

    public void setProxyServerUrl(String proxyServerUrl) {
        this.proxyServerUrl = proxyServerUrl;
    }
}
