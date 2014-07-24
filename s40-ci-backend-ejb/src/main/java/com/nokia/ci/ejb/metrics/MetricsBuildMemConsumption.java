/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.MemConsumption;
import java.util.Date;

/**
 *
 * @author larryang
 */
public class MetricsBuildMemConsumption extends MetricsBuild {

    private String componentName;
    private Float rom;
    private Float ram;

    public MetricsBuildMemConsumption(Long id, BuildStatus status, Date startTime, Date endTime, String verificationUuid, String productUuid) {

        super(id, status, startTime, endTime, verificationUuid, productUuid);
    }

    public MetricsBuildMemConsumption(MetricsBuild metricsBuild) {

        super(metricsBuild.getId(), metricsBuild.getStatus(), metricsBuild.getStartTime(), metricsBuild.getEndTime(),
                metricsBuild.getVerificationUuid(), metricsBuild.getProductUuid());
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Float getRam() {
        return ram;
    }

    public void setRam(Float ram) {
        this.ram = ram;
    }

    public Float getRom() {
        return rom;
    }

    public void setRom(Float rom) {
        this.rom = rom;
    }

}
