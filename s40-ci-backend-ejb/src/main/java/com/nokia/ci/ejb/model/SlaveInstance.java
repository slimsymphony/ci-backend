/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * Entity class for SlaveInstance.
 *
 * @author aklappal
 */
@Entity
@Table(name = "SLAVE_INSTANCE")
public class SlaveInstance extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SlaveMachine slaveMachine;
    private String currentMaster;
    private String url;
    @Temporal(TemporalType.TIMESTAMP)
    private Date reserved;
    @ManyToMany
    @JoinTable(name = "SLAVE_POOL_SLAVE_INSTANCE")
    private List<SlavePool> slavePools = new ArrayList<SlavePool>();
    @ManyToMany
    @JoinTable(name = "SLAVE_INSTANCE_SLAVE_LABEL")
    private List<SlaveLabel> slaveLabels = new ArrayList<SlaveLabel>();
    @Version
    @Column(nullable = false)
    private long version;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public SlaveMachine getSlaveMachine() {
        return slaveMachine;
    }

    public void setSlaveMachine(SlaveMachine slaveMachine) {
        this.slaveMachine = slaveMachine;
    }

    public String getCurrentMaster() {
        return currentMaster;
    }

    public void setCurrentMaster(String currentMaster) {
        this.currentMaster = currentMaster;
    }

    public List<SlaveLabel> getSlaveLabels() {
        return slaveLabels;
    }

    public void setSlaveLabels(List<SlaveLabel> slaveLabels) {
        this.slaveLabels = slaveLabels;
    }

    public List<SlavePool> getSlavePools() {
        return slavePools;
    }

    public void setSlavePools(List<SlavePool> slavePools) {
        this.slavePools = slavePools;
    }

    public Date getReserved() {
        return reserved;
    }

    public void setReserved(Date reserved) {
        this.reserved = reserved;
    }

    protected long getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
