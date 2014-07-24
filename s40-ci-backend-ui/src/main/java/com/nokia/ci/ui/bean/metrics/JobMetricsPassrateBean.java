package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.JobMetricsEJB;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsVerificationGroup;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for job health metrics.
 *
 * @author larryang
 */
@Named
public class JobMetricsPassrateBean extends MetricsLineChartBeanBase {

    protected Logger log = LoggerFactory.getLogger(JobMetricsPassrateBean.class);

    @Inject
    private JobMetricsEJB jobMetricsEJB;    
    
    @Override
    public void init() {
        super.init();

        initProperties();
        
        getScaleOptions().add(MetricsTimespan.DAILY.toString());
        getScaleOptions().add(MetricsTimespan.WEEKLY.toString());
        getScaleOptions().add(MetricsTimespan.MONTHLY.toString());
        setSelectedScale(MetricsTimespan.DAILY.toString());
        
        if(isRendered() && getChart() != null) {
            MetricsTimespan t = getTimespan();
            if(t != null) {
                setSelectedScale(t.toString());
            }
            updateDataModel();
        }
    }
    
    protected void initProperties(){
        
        setMetricsLevel(MetricsLevel.VERIFICATION);
        setJsfComponent("jobPassrateChart.xhtml");
        setRenderDiv("passrateLineChartPanel");
        setHeader("Passrate");
    }
    
    protected List<MetricsVerificationGroup> fetchMetricsVerificationGroups(MetricsTimespan selectedTimespan){
         
         List<MetricsVerificationGroup> metricsVerificationGroups = null;
         
         try{
            metricsVerificationGroups = 
                    jobMetricsEJB.getVerifications(getVerificationId(), 
                    getStartDate(), getEndDate(), selectedTimespan, getTimezone());
            
        }catch (NotFoundException ex) {
            log.debug("Exception encountered when fetching metrics verification groups: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }
        
        return metricsVerificationGroups;
    }
    
    @Override
    public void updateDataModel() {
        super.updateDataModel();

        MetricsTimespan selectedTimespan = getMetricsTimespan(getSelectedScale());

        ChartSeries successBuildCount = new ChartSeries();
        successBuildCount.setLabel("Success " + getSelectedScale());
        
        ChartSeries unstableBuildCount = new ChartSeries();
        unstableBuildCount.setLabel("Unstable " + getSelectedScale());

        ChartSeries failureBuildCount = new ChartSeries();
        failureBuildCount.setLabel("Failed " + getSelectedScale());

        try {
            List<MetricsVerificationGroup> metricsVerificationGroups = 
                    fetchMetricsVerificationGroups(selectedTimespan);

            calculateXAxisLabelDivider(metricsVerificationGroups.size());

            for (int i = 0; i < metricsVerificationGroups.size(); i++) {
                MetricsVerificationGroup metricsVerificationGroup = metricsVerificationGroups.get(i);
                
                Map<BuildStatus, Long> statusMap = metricsVerificationGroup.getStatusMap();

                MetricsXAxisLabel label = getXAxisLabel(metricsVerificationGroup.getStartTime(), i, selectedTimespan);

                successBuildCount.set(label, 
                        statusMap.get(BuildStatus.SUCCESS) == null ? 0 : statusMap.get(BuildStatus.SUCCESS));
                unstableBuildCount.set(label, 
                        statusMap.get(BuildStatus.UNSTABLE) == null ? 0 : statusMap.get(BuildStatus.UNSTABLE));
                failureBuildCount.set(label, metricsVerificationGroup.getFailedCount());

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
        if (unstableBuildCount.getData().isEmpty()) {
            unstableBuildCount.set(0, 0);
            displayEmptyDatasetMessage(unstableBuildCount.getLabel());
        }

        getDataModel().addSeries(unstableBuildCount);

        if (failureBuildCount.getData().isEmpty()) {
            failureBuildCount.set(0, 0);
            displayEmptyDatasetMessage(failureBuildCount.getLabel());
        }

        getDataModel().addSeries(failureBuildCount);
        
        if (successBuildCount.getData().isEmpty()) {
            successBuildCount.set(0, 0);
            displayEmptyDatasetMessage(successBuildCount.getLabel());
        }

        getDataModel().addSeries(successBuildCount);
    }

    public JobMetricsEJB getJobMetricsEJB() {
        return jobMetricsEJB;
    }

    public void setJobMetricsEJB(JobMetricsEJB jobMetricsEJB) {
        this.jobMetricsEJB = jobMetricsEJB;
    }
    
    @Override
    protected void open(Long id) {
        openBuild(id);
    }

}
