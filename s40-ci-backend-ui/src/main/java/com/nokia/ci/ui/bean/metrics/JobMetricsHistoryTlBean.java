package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.JobMetricsEJB;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsVerification;
import com.nokia.ci.ejb.model.BuildStatus;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.extensions.model.timeline.DefaultTimeLine;
import org.primefaces.extensions.model.timeline.Timeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for job build history time line metrics.
 *
 * @author larryang
 */
@Named
public class JobMetricsHistoryTlBean extends MetricsTlChartBeanBase {

    private Logger log = LoggerFactory.getLogger(JobMetricsHistoryTlBean.class);
    @Inject
    private JobMetricsEJB jobMetricsEJB;
    
    @Override
    public void init() {
        super.init();
        
        setMetricsLevel(MetricsLevel.VERIFICATION);
        setJsfComponent("jobHistoryTlChart.xhtml");
        setHeader("History timeline");
        
        setRenderDiv("historyTlPanel");
        if(isRendered() && getChart() != null) {
            updateDataModel();
        }
    }

    @Override
    public void updateDataModel() {
        super.updateDataModel();

        Timeline timelineHistory = new DefaultTimeLine("History", "History");
        getTimelines().add(timelineHistory);

        try {
            List<MetricsVerification> metricsVerifications = jobMetricsEJB.getVerifications(getVerificationId(),
                        getStartDate(), getEndDate(), getTimezone()).getItems();
            
            for (int i = 0; i < metricsVerifications.size(); i++) {
                    MetricsVerification metricsVerification = metricsVerifications.get(i);
                    getIdList().add(metricsVerification.getId());
                    
                    CustomTimelineEvent timelineEvent = new CustomTimelineEvent(metricsVerification.getId().toString(),
                            metricsVerification.getStartTime(), metricsVerification.getEndTime());
                    timelineEvent.setItemId(metricsVerification.getId());
                    
                    if (metricsVerification.getStatus() == BuildStatus.SUCCESS){
                        timelineEvent.setStyleClass("greentimelinebox");
                    }else if (metricsVerification.getStatus() == BuildStatus.UNSTABLE){
                        timelineEvent.setStyleClass("yellowtimelinebox");
                    }else if (metricsVerification.getStatus() == BuildStatus.FAILURE){
                        timelineEvent.setStyleClass("redtimelinebox");
                    }else{
                        timelineEvent.setStyleClass("graytimelinebox");
                    }
                    timelineHistory.addEvent(timelineEvent);                    
            }
        } catch (NotFoundException ex) {
            log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }
    }

    @Override
    protected void open(Long id) {
        openBuild(id);
    }

    public JobMetricsEJB getJobMetricsEJB() {
        return jobMetricsEJB;
    }

    public void setJobMetricsEJB(JobMetricsEJB jobMetricsEJB) {
        this.jobMetricsEJB = jobMetricsEJB;
    }
}