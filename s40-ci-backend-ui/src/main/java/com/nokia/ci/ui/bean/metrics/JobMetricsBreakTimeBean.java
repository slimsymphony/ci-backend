package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.JobMetricsEJB;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsVerificationGroup;
import com.nokia.ci.ejb.metrics.MetricsVerificationResult;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for job build break time statistics metrics.
 *
 * @author larryang
 */
@Named
public class JobMetricsBreakTimeBean extends MetricsLineChartBeanBase {

    private Logger log = LoggerFactory.getLogger(JobMetricsBreakTimeBean.class);

    @Inject
    private JobMetricsEJB jobMetricsEJB;
    
    @Override
    public void init() {
        super.init();
        
        setMetricsLevel(MetricsLevel.VERIFICATION);
        setJsfComponent("jobBreaktimeChart.xhtml");
        setHeader("Break time");
        
        setRenderDiv("breakTimeLineChartPanel");
        if(isRendered() && getChart() != null) {
            updateDataModel();
        }
    }

    @Override
    public void updateDataModel() {
        super.updateDataModel();

        ChartSeries individualBreakTime = new ChartSeries();
        individualBreakTime.setLabel("Break(min)");

        ChartSeries accumulatedBreakTime = new ChartSeries();
        accumulatedBreakTime.setLabel("Accumulated breaks(min)");

        try {
            List<MetricsVerificationGroup> metricsVerificationGroups = 
                    jobMetricsEJB.getVerificationBreaks(getVerificationId(), getStartDate(), getEndDate());

            long accumulatedTimeMin = 0L;

            calculateXAxisLabelDivider(metricsVerificationGroups.size());

            int breakIndex = 0;
            for (MetricsVerificationGroup metricsVerificationGroup : metricsVerificationGroups) {

                // sanity check.
                if (metricsVerificationGroup.getItems().isEmpty()) {
                    continue;
                }

                if (metricsVerificationGroup.getItems().get(0).getResult() == MetricsVerificationResult.FAILED) {
                    long groupDurationMs = metricsVerificationGroup.getEndTime().getTime() - metricsVerificationGroup.getStartTime().getTime();
                    long groupDurationMin = Math.round(msToMin(groupDurationMs));

                    MetricsXAxisLabel label = getXAxisLabel(metricsVerificationGroup.getStartTime(), breakIndex, MetricsTimespan.DAILY);
                    individualBreakTime.set(label, groupDurationMin);

                    accumulatedTimeMin += groupDurationMin;
                    accumulatedBreakTime.set(label, accumulatedTimeMin);

                    if (metricsVerificationGroup.getItems().size() > 0) {
                        getIdList().add(metricsVerificationGroup.getItems().get(0).getId());
                    } else {
                        getIdList().add(null);
                    }

                    breakIndex++;
                }
            }
        } catch (NotFoundException ex) {
            log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }

        //Set it empty so that the chart can be shown normally.
        if (individualBreakTime.getData().isEmpty()) {
            individualBreakTime.set(0, 0);
            displayEmptyDatasetMessage();
        }

        getDataModel().addSeries(individualBreakTime);

        if (accumulatedBreakTime.getData().isEmpty()) {
            accumulatedBreakTime.set(0, 0);
        }

        getDataModel().addSeries(accumulatedBreakTime);
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
