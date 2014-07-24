/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Metrics view model for test case stat.
 *
 * @author larryang
 */
@XmlRootElement
public class TestCaseStatView {

    @XmlElement
    private String componentName;
    @XmlElement
    private Integer totalCount;
    @XmlElement
    private Integer passedCount;
    @XmlElement
    private Integer failedCount;
    @XmlElement
    private Integer naCount;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Integer getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(Integer passedCount) {
        this.passedCount = passedCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public Integer getNaCount() {
        return naCount;
    }

    public void setNaCount(Integer naCount) {
        this.naCount = naCount;
    }
}