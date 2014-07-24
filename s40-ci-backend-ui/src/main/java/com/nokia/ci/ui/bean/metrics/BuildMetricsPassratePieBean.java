/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.BuildMetricsEJB;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ejb.metrics.MetricsVerificationGroup;
import com.nokia.ci.ejb.metrics.MetricsVerificationResult;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author larryang
 */
@Named
public class BuildMetricsPassratePieBean extends JobMetricsPassratePieBean{
    

    @Inject
    private BuildMetricsEJB buildMetricsEJB;
    
    @Override
    public void initProperties() {
        
        //Override parent initial setting.
        setMetricsLevel(MetricsLevel.BUILD);
        setJsfComponent("buildPassratePieChart.xhtml");
        setRenderDiv("buildPassratePieChartPanel");
        setHeader("Passrate percentage");

    }

    @Override
    protected MetricsVerificationGroup fetchMetricsVerificationGroup(){
        
        MetricsVerificationGroup mvg = null;
        
        try{
            mvg = buildMetricsEJB.getBuilds(
                    getBuildId(), getStartDate(), getEndDate(), getTimezone(), MetricsVerificationResult.ALL);
        } catch (NotFoundException ex) {
            log.debug("Exception encountered when fetching metrics verification group: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }
        
        return mvg;
    }
}
