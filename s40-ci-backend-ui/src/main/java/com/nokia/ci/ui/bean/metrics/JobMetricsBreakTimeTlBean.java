package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.JobMetricsEJB;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsVerification;
import com.nokia.ci.ejb.metrics.MetricsVerificationGroup;
import com.nokia.ci.ejb.metrics.MetricsVerificationResult;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.extensions.model.timeline.DefaultTimeLine;
import org.primefaces.extensions.model.timeline.Timeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for job build break time statistics metrics in time line chart.
 *
 * @author larryang
 */
@Named
public class JobMetricsBreakTimeTlBean extends MetricsTlChartBeanBase {

    private Logger log = LoggerFactory.getLogger(JobMetricsBreakTimeTlBean.class);
    @Inject
    private JobMetricsEJB jobMetricsEJB;
    
    @Override
    public void init() {
        super.init();
        
        setMetricsLevel(MetricsLevel.VERIFICATION);
        setJsfComponent("jobBreaktimeTlChart.xhtml");
        setHeader("Break time timeline");
        
        setRenderDiv("breakTimeTlPanel");
        if(isRendered() && getChart() != null) {
            updateDataModel();
        }
    }

    @Override
    public void updateDataModel() {
        super.updateDataModel();

        Timeline timelineFailed = new DefaultTimeLine("Break", "Break");
        getTimelines().add(timelineFailed);

        try {
            List<MetricsVerificationGroup> metricsVerificationGroups = 
                    jobMetricsEJB.getVerificationBreaks(getVerificationId(), getStartDate(), getEndDate());

            for (MetricsVerificationGroup metricsVerificationGroup : metricsVerificationGroups) {
                // sanity check.
                if (metricsVerificationGroup.getItems().isEmpty()) {
                    continue;
                }

                MetricsVerification firstOfGroup = metricsVerificationGroup.getItems().get(0);

                if (firstOfGroup.getResult() == MetricsVerificationResult.FAILED) {
                    CustomTimelineEvent timelineEvent = new CustomTimelineEvent("",
                            metricsVerificationGroup.getStartTime(), metricsVerificationGroup.getEndTime());
                    timelineEvent.setStyleClass("redtimelinebox");
                    timelineEvent.setItemId(firstOfGroup.getId());
                    timelineFailed.addEvent(timelineEvent);
                }
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
