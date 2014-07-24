package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.JobMetricsEJB;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsVerificationGroup;
import com.nokia.ci.ejb.model.BuildStatus;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for job health metrics.
 *
 * @author larryang
 */
@Named
public class JobMetricsPassratePieBean extends MetricsPieChartBeanBase {

    protected Logger log = LoggerFactory.getLogger(JobMetricsPassratePieBean.class);

    @Inject
    private JobMetricsEJB jobMetricsEJB;    
    
    @Override
    public void init() {
        super.init();
        
        initProperties();

        if(isRendered() && getChart() != null) {
            updateDataModel();
        }
    }
    
    protected void initProperties(){
        
        setMetricsLevel(MetricsLevel.VERIFICATION);
        setJsfComponent("jobPassratePieChart.xhtml");
        setRenderDiv("passratePieChartPanel");
        setHeader("Passrate percentage");
    }
    
    protected MetricsVerificationGroup fetchMetricsVerificationGroup(){
        
        MetricsVerificationGroup mvg = null;
        
        try{
            mvg = jobMetricsEJB.getVerifications(
                    getVerificationId(), getStartDate(), getEndDate(), getTimezone());
        } catch (NotFoundException ex) {
            log.debug("Exception encountered when fetching metrics verification group: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }
        
        return mvg;
    }

    @Override
    public void updateDataModel() {
        super.updateDataModel();       

        try {
            MetricsVerificationGroup mvg = fetchMetricsVerificationGroup();            

            if(mvg != null && !mvg.getItems().isEmpty()) {
                Long totalCount = mvg.getPassedCount() + mvg.getFailedCount();
                
                Map<BuildStatus, Long> statusMap = mvg.getStatusMap();
                insertData(createLegendText(BuildStatus.SUCCESS, statusMap.get(BuildStatus.SUCCESS), totalCount),
                        statusMap.get(BuildStatus.SUCCESS));
                insertData(createLegendText(BuildStatus.FAILURE, statusMap.get(BuildStatus.FAILURE), totalCount),
                        statusMap.get(BuildStatus.FAILURE));
                insertData(createLegendText(BuildStatus.ABORTED, statusMap.get(BuildStatus.ABORTED), totalCount),
                        statusMap.get(BuildStatus.ABORTED));
                insertData(createLegendText(BuildStatus.UNSTABLE, statusMap.get(BuildStatus.UNSTABLE), totalCount),
                        statusMap.get(BuildStatus.UNSTABLE));
                insertData(createLegendText(BuildStatus.NOT_BUILT, statusMap.get(BuildStatus.NOT_BUILT), totalCount),
                        statusMap.get(BuildStatus.NOT_BUILT));
            }
        } catch (Exception ex) {
            log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }

        if (getDataModel().getData().isEmpty()) {
            displayEmptyDatasetMessage();
            setEmptyDataset();
        }
    }
    
    private String createLegendText(BuildStatus buildStatus, Long amount, Long totalAmount) {
        StringBuilder sb = new StringBuilder();
       
        if (amount == null){
            amount = new Long(0L);
        }

        sb.append(buildStatus.toString());
        sb.append("(");
        sb.append(amount);
        sb.append("/");
        sb.append(totalAmount);
        sb.append(")");
        return sb.toString();
    }
    
    private void insertData(String buildStatusLabel, Long value) {
        if (value == null) {
            value = new Long(0L);
        }
        getDataModel().set(buildStatusLabel, value);
        
    }
    
    public JobMetricsEJB getJobMetricsEJB() {
        return jobMetricsEJB;
    }

    public void setJobMetricsEJB(JobMetricsEJB jobMetricsEJB) {
        this.jobMetricsEJB = jobMetricsEJB;
    }
}
