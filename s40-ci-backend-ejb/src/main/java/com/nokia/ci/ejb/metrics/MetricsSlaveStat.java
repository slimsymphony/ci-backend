package com.nokia.ci.ejb.metrics;

import java.util.Date;

/**
 * 
 * @author larryang
 */
public class MetricsSlaveStat {

    private Long id;
    private Date provisionTime;
    private Integer reservedInstanceCount = 0;
    private Integer totalInstanceCount = 0;

    public MetricsSlaveStat (Long id, Date provisionTime, Integer reservedInstanceCount, Integer totalInstanceCount) {

        this.id = id;
        this.provisionTime = provisionTime;
        this.reservedInstanceCount = reservedInstanceCount;
        this.totalInstanceCount = totalInstanceCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getProvisionTime() {
        return provisionTime;
    }

    public void setProvisionTime(Date provisionTime) {
        this.provisionTime = provisionTime;
    }

    public Integer getReservedInstanceCount() {
        return reservedInstanceCount;
    }

    public void setReservedInstanceCount(Integer reservedInstanceCount) {
        this.reservedInstanceCount = reservedInstanceCount;
    }

    public Integer getTotalInstanceCount() {
        return totalInstanceCount;
    }

    public void setTotalInstanceCount(Integer totalInstanceCount) {
        this.totalInstanceCount = totalInstanceCount;
    }

}
