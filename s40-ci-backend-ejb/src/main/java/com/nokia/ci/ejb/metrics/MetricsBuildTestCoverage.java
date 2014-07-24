/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.model.BuildStatus;
import java.util.Date;

/**
 *
 * @author larryang
 */
public class MetricsBuildTestCoverage extends MetricsBuild{
    
    private String componentName;
    private Float condCov;
    private Float stmtCov;
       
    public MetricsBuildTestCoverage(Long id, BuildStatus status, Date startTime, Date endTime, String verificationUuid, String productUuid){
           
        super(id, status, startTime, endTime, verificationUuid, productUuid);
    }
    
    public MetricsBuildTestCoverage(MetricsBuild metricsBuild){
        
        super(metricsBuild.getId(), metricsBuild.getStatus(), metricsBuild.getStartTime(), metricsBuild.getEndTime(), 
                metricsBuild.getVerificationUuid(), metricsBuild.getProductUuid());
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Float getCondCov() {
        return condCov;
    }

    public void setCondCov(Float condCov) {
        this.condCov = condCov;
    }

    public Float getStmtCov() {
        return stmtCov;
    }

    public void setStmtCov(Float stmtCov) {
        this.stmtCov = stmtCov;
    }

}