package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class for SlaveLabel.
 *
 * @author jaakkpaa
 */
@Entity
@Table(name = "SLAVE_LABEL")
public class SlaveLabel extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToMany(mappedBy = "slaveLabels")
    private List<SlaveInstance> slaveInstances = new ArrayList<SlaveInstance>();
    @ManyToMany(mappedBy = "slaveLabels")
    private List<Verification> verifications = new ArrayList<Verification>();
    private String name;
    @OneToMany(mappedBy = "slaveLabel", cascade = CascadeType.ALL)
    private List<SlaveStatPerLabel> slaveStatsPerLabel = new ArrayList<SlaveStatPerLabel>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    public List<SlaveInstance> getSlaveInstances() {
        return slaveInstances;
    }

    public void setSlaveInstances(List<SlaveInstance> slaveInstances) {
        this.slaveInstances = slaveInstances;
    }

    public List<Verification> getVerifications() {
        return verifications;
    }

    public void setVerifications(List<Verification> verifications) {
        this.verifications = verifications;
    }

    public List<SlaveStatPerLabel> getSlaveStatsPerLabel() {
        return slaveStatsPerLabel;
    }

    public void setSlaveStatsPerLabel(List<SlaveStatPerLabel> slaveStatsPerLabel) {
        this.slaveStatsPerLabel = slaveStatsPerLabel;
    }

}
