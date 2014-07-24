/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author jajuutin
 */
@Entity
@Table(name = "GERRIT")
public class Gerrit extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String url;
    private int port;
    private String privateKeyPath;
    private String sshUserName;
    private Boolean listenStream;
    private String projectAccessHost;
    private Integer projectAccessPort;
    @OneToMany(mappedBy = "gerrit", cascade = {CascadeType.MERGE,
        CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Project> projects = new ArrayList<Project>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getSshUserName() {
        return sshUserName;
    }

    public void setSshUserName(String sshUserName) {
        this.sshUserName = sshUserName;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public Boolean getListenStream() {
        return listenStream;
    }

    public void setListenStream(Boolean listenStream) {
        this.listenStream = listenStream;
    }

    public String getProjectAccessHost() {
        return projectAccessHost;
    }

    public void setProjectAccessHost(String projectAccessHost) {
        this.projectAccessHost = projectAccessHost;
    }

    public Integer getProjectAccessPort() {
        return projectAccessPort;
    }

    public void setProjectAccessPort(Integer projectAccessPort) {
        this.projectAccessPort = projectAccessPort;
    }
}
