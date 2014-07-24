package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class for SlavePool.
 *
 * @author jaakkpaa
 */
@Entity
@Table(name = "SLAVE_POOL")
public class SlavePool extends BaseEntity {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    private Long id;
    @ManyToMany(mappedBy = "slavePools")
    private List<SlaveInstance> slaveInstances = new ArrayList<SlaveInstance>();
    @OneToMany(mappedBy = "slavePool")
    private List<CIServer> ciServers = new ArrayList<CIServer>();
    private String name;
    private Long reservedSlaveInstancesLimit = -1L;
    @OneToMany(mappedBy = "slavePool", cascade = CascadeType.ALL)
    private List<SlaveStatPerLabel> slaveStatsPerLabel = new ArrayList<SlaveStatPerLabel>();
    @OneToMany(mappedBy = "slavePool", cascade = CascadeType.ALL)
    private List<SlaveStatPerMachine> slaveStatsPerMachine = new ArrayList<SlaveStatPerMachine>();
    @OneToMany(mappedBy = "slavePool", cascade = CascadeType.ALL)
    private List<SlaveStatPerPool> slaveStatsPerPool = new ArrayList<SlaveStatPerPool>();

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return (id);
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getName() {
        return (this.name);
    }

    public List<SlaveInstance> getSlaveInstances() {
        return slaveInstances;
    }

    public void setSlaveInstances(List<SlaveInstance> slaveInstances) {
        this.slaveInstances = slaveInstances;
    }

    public List<CIServer> getCiServers() {
        return ciServers;
    }

    public void setCiServers(List<CIServer> ciServers) {
        this.ciServers = ciServers;
    }

    public Long getReservedSlaveInstancesLimit() {
        return reservedSlaveInstancesLimit;
    }

    public void setReservedSlaveInstancesLimit(Long reservedSlaveInstancesLimit) {
        this.reservedSlaveInstancesLimit = reservedSlaveInstancesLimit;
    }

    public List<SlaveStatPerLabel> getSlaveStatsPerLabel() {
        return slaveStatsPerLabel;
    }

    public void setSlaveStatsPerLabel(List<SlaveStatPerLabel> slaveStatsPerLabel) {
        this.slaveStatsPerLabel = slaveStatsPerLabel;
    }

    public List<SlaveStatPerMachine> getSlaveStatsPerMachine() {
        return slaveStatsPerMachine;
    }

    public void setSlaveStatsPerMachine(List<SlaveStatPerMachine> slaveStatsPerMachine) {
        this.slaveStatsPerMachine = slaveStatsPerMachine;
    }

    public List<SlaveStatPerPool> getSlaveStatsPerPool() {
        return slaveStatsPerPool;
    }

    public void setSlaveStatsPerPool(List<SlaveStatPerPool> slaveStatsPerPool) {
        this.slaveStatsPerPool = slaveStatsPerPool;
    }

}
