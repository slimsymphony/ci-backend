package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.BuildMetricsEJB;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsVerificationGroup;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ejb.metrics.MetricsVerificationResult;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for test triggerrate metrics.
 *
 * @author larryang
 */
@Named
public class BuildMetricsTestTriggerrateBean extends MetricsLineChartBeanBase {

    protected Logger log = LoggerFactory.getLogger(BuildMetricsTestTriggerrateBean.class);

    @Inject
    private BuildMetricsEJB buildMetricsEJB;    
    
    @Override
    public void init() {
        super.init();
        
        setMetricsLevel(MetricsLevel.BUILD);
        setJsfComponent("buildTestTriggerrateChart.xhtml");
        setHeader("Test triggerrate");

        getScaleOptions().add(MetricsTimespan.DAILY.toString());
        getScaleOptions().add(MetricsTimespan.WEEKLY.toString());
        getScaleOptions().add(MetricsTimespan.MONTHLY.toString());
        setSelectedScale(MetricsTimespan.DAILY.toString());
        
        setRenderDiv("testTriggerrateLineChartPanel");
        if(isRendered() && getChart() != null) {
            MetricsTimespan t = getTimespan();
            if(t != null) {
                setSelectedScale(t.toString());
            }
            updateDataModel();
        }
    }
    
     protected List<MetricsVerificationGroup> fetchMetricsVerificationGroups(MetricsTimespan selectedTimespan){
         
         List<MetricsVerificationGroup> metricsVerificationGroups = null;
         
         try{
            metricsVerificationGroups = 
                    buildMetricsEJB.getTestTriggeringBuilds(getBuildId(), 
                    getStartDate(), getEndDate(), selectedTimespan, getTimezone(), MetricsVerificationResult.ALL);
            
        }catch (NotFoundException ex) {
            log.debug("Exception encountered when fetching metrics verification groups: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }
        
        return metricsVerificationGroups;
    }
    
    @Override
    public void updateDataModel() {
        super.updateDataModel();

        MetricsTimespan selectedTimespan = getMetricsTimespan(getSelectedScale());

        ChartSeries testTriggeringBuildCount = new ChartSeries();
        testTriggeringBuildCount.setLabel("Test-triggering builds" + getSelectedScale());
        
        ChartSeries nonTestBuildCount = new ChartSeries();
        nonTestBuildCount.setLabel("Non-test builds" + getSelectedScale());


        try {
            List<MetricsVerificationGroup> metricsVerificationGroups = 
                    fetchMetricsVerificationGroups(selectedTimespan);

            calculateXAxisLabelDivider(metricsVerificationGroups.size());

            for (int i = 0; i < metricsVerificationGroups.size(); i++) {
                MetricsVerificationGroup metricsVerificationGroup = metricsVerificationGroups.get(i);

                MetricsXAxisLabel label = getXAxisLabel(metricsVerificationGroup.getStartTime(), i, selectedTimespan);

                testTriggeringBuildCount.set(label, metricsVerificationGroup.getTestTriggeringBuildCount());
                nonTestBuildCount.set(label, metricsVerificationGroup.getItemCount() - metricsVerificationGroup.getTestTriggeringBuildCount());

                if (metricsVerificationGroup.getItems().size() > 0) {
                    getIdList().add(metricsVerificationGroup.getItems().get(0).getId());
                } else {
                    getIdList().add(null);
                }
            }
        } catch (Exception ex) {
            log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }

        //Set it empty so that the chart can be shown normally.
        if (testTriggeringBuildCount.getData().isEmpty()) {
            testTriggeringBuildCount.set(0, 0);
            displayEmptyDatasetMessage();
        }

        getDataModel().addSeries(testTriggeringBuildCount);
        
        if (nonTestBuildCount.getData().isEmpty()) {
            nonTestBuildCount.set(0, 0);
            displayEmptyDatasetMessage();
        }

        getDataModel().addSeries(nonTestBuildCount);

    }
    
    @Override
    protected void open(Long id) {
        openBuild(id);
    }    
}