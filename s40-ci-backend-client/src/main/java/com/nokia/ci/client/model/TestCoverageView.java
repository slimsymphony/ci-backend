/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Metrics view model for test coverage.
 * 
 * @author larryang
 */
@XmlRootElement
public class TestCoverageView {
    
    @XmlElement
    private String componentName;
    @XmlElement
    private String featureName;
    @XmlElement
    private String featureGroupName;
    @XmlElement
    private Float stmtCov;
    @XmlElement
    private Float condCov;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getFeatureGroupName() {
        return featureGroupName;
    }

    public void setFeatureGroupName(String featureGroupName) {
        this.featureGroupName = featureGroupName;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
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