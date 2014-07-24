/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class for SlaveMachine.
 *
 * @author aklappal
 */
@Entity
@Table(name = "SLAVE_MACHINE")
public class SlaveMachine extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Access(AccessType.PROPERTY)
    private Long id;
    private Boolean disabled = false;
    private String url;
    private int port;
    @OneToMany(mappedBy = "slaveMachine", cascade = CascadeType.ALL)
    private List<SlaveInstance> slaveInstances = new ArrayList<SlaveInstance>();
    private String workspace;
    private String startScript;
    private String endScript;
    private Long maxSlaveInstanceAmount;
    @OneToMany(mappedBy = "slaveMachine", cascade = CascadeType.ALL)
    private List<SlaveStatPerMachine> slaveStatsPerMachine = new ArrayList<SlaveStatPerMachine>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
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

    public List<SlaveInstance> getSlaveInstances() {
        return slaveInstances;
    }

    public void setSlaveInstances(List<SlaveInstance> slaveInstances) {
        this.slaveInstances = slaveInstances;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getStartScript() {
        return startScript;
    }

    public void setStartScript(String startScript) {
        this.startScript = startScript;
    }

    public String getEndScript() {
        return endScript;
    }

    public void setEndScript(String endScript) {
        this.endScript = endScript;
    }

    public Long getMaxSlaveInstanceAmount() {
        return maxSlaveInstanceAmount;
    }

    public void setMaxSlaveInstanceAmount(Long maxSlaveInstanceAmount) {
        this.maxSlaveInstanceAmount = maxSlaveInstanceAmount;
    }

    public List<SlaveStatPerMachine> getSlaveStatsPerMachine() {
        return slaveStatsPerMachine;
    }

    public void setSlaveStatsPerMachine(List<SlaveStatPerMachine> slaveStatsPerMachine) {
        this.slaveStatsPerMachine = slaveStatsPerMachine;
    }

}
