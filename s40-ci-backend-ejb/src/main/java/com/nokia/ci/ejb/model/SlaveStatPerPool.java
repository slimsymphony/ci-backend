package com.nokia.ci.ejb.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class for SlaveStat.
 *
 * @author larryang
 */
@Entity
@Table(name = "SLAVE_STAT_PER_POOL")
public class SlaveStatPerPool extends BaseEntity {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date provisionTime;
    private Integer reservedInstanceCount = 0;
    private Integer totalInstanceCount = 0;
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private SlavePool slavePool;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getReservedInstanceCount() {
        return reservedInstanceCount;
    }

    public void setReservedInstanceCount(Integer reservedInstanceCount) {
        this.reservedInstanceCount = reservedInstanceCount;
    }

    public Date getProvisionTime() {
        return provisionTime;
    }

    public void setProvisionTime(Date provisionTime) {
        this.provisionTime = provisionTime;
    }

    public SlavePool getSlavePool() {
        return slavePool;
    }

    public void setSlavePool(SlavePool slavePool) {
        this.slavePool = slavePool;
    }

    public Integer getTotalInstanceCount() {
        return totalInstanceCount;
    }

    public void setTotalInstanceCount(Integer totalInstanceCount) {
        this.totalInstanceCount = totalInstanceCount;
    }

}