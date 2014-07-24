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
public class BuildMetricsPassrateBean extends JobMetricsPassrateBean{
    

    @Inject
    private BuildMetricsEJB buildMetricsEJB;
    
    @Override
    public void initProperties() {
        
        //Override parent initial setting.
        setMetricsLevel(MetricsLevel.BUILD);
        setJsfComponent("buildPassrateChart.xhtml");
        setRenderDiv("buildPassrateLineChartPanel");
        setHeader("Passrate");
    }

    @Override
     protected List<MetricsVerificationGroup> fetchMetricsVerificationGroups(MetricsTimespan selectedTimespan){
         
         List<MetricsVerificationGroup> metricsVerificationGroups = null;
         
         try{
            metricsVerificationGroups = 
                    buildMetricsEJB.getBuilds(getBuildId(), 
                    getStartDate(), getEndDate(), selectedTimespan, getTimezone(), MetricsVerificationResult.ALL);
            
        }catch (NotFoundException ex) {
            log.debug("Exception encountered when fetching metrics verification groups: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }
        
        return metricsVerificationGroups;
    }
}
