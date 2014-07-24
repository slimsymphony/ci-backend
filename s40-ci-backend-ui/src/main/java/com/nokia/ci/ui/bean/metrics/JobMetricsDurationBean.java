package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.JobMetricsEJB;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsVerificationGroup;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ejb.metrics.MetricsVerification;
import com.nokia.ci.ejb.metrics.MetricsVerificationResult;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for job metrics.
 *
 * @author jajuutin
 */
@Named
public class JobMetricsDurationBean extends MetricsLineChartBeanBase {

    private Logger log = LoggerFactory.getLogger(JobMetricsDurationBean.class);
    @Inject
    private JobMetricsEJB jobMetricsEJB;
    private List<String> resultStatus;
    private String selectedResultStatus;

    @Override
    public void init() {
        super.init();
        
        setMetricsLevel(MetricsLevel.VERIFICATION);
        setJsfComponent("jobDurationChart.xhtml");
        setHeader("Duration");

        getScaleOptions().add(MetricsTimespan.INDIVIDUAL.toString());
        getScaleOptions().add(MetricsTimespan.DAILY.toString());
        getScaleOptions().add(MetricsTimespan.WEEKLY.toString());
        getScaleOptions().add(MetricsTimespan.MONTHLY.toString());

        setSelectedScale(MetricsTimespan.INDIVIDUAL.toString());

        resultStatus = new ArrayList<String>();
        getResultStatus().add(MetricsVerificationResult.PASSED.toString());
        getResultStatus().add(MetricsVerificationResult.FAILED.toString());
        getResultStatus().add(MetricsVerificationResult.ALL.toString());
        setSelectedResultStatus(MetricsVerificationResult.PASSED.toString());

        setRenderDiv("durationPanel");
        if (isRendered() && getChart() != null) {
            MetricsTimespan t = getTimespan();
            if (t != null) {
                setSelectedScale(t.toString());
            }

            String result = getQueryParam("result");
            if (result != null) {
                selectedResultStatus = result;
            }

            updateDataModel();
        }
    }

    @Override
    public void updateDataModel() {

        super.updateDataModel();

        MetricsTimespan selectedTimespan = getMetricsTimespan(getSelectedScale());

        ChartSeries buildRuntime = new ChartSeries();
        buildRuntime.setLabel("Duration(min) " + getSelectedScale());

        try {
            if (selectedTimespan == MetricsTimespan.INDIVIDUAL) {
                List<MetricsVerification> metricsVerifications = jobMetricsEJB.getVerifications(getVerificationId(),
                        getStartDate(), getEndDate(), getTimezone(), MetricsVerificationResult.getEnum(selectedResultStatus)).getItems();

                calculateXAxisLabelDivider(metricsVerifications.size());

                for (int i = 0; i < metricsVerifications.size(); i++) {
                    MetricsVerification metricsVerification = metricsVerifications.get(i);
                    MetricsXAxisLabel label = getXAxisLabel(metricsVerification.getStartTime(), i, MetricsTimespan.INDIVIDUAL);
                    buildRuntime.set(label, Math.round(msToMin(metricsVerification.getRuntime())));
                    getIdList().add(metricsVerification.getId());
                }
            } else {
                List<MetricsVerificationGroup> metricsVerificationGroups = jobMetricsEJB.getVerifications(getVerificationId(),
                        getStartDate(), getEndDate(), selectedTimespan, getTimezone(), true, MetricsVerificationResult.getEnum(selectedResultStatus));

                calculateXAxisLabelDivider(metricsVerificationGroups.size());

                for (int i = 0; i < metricsVerificationGroups.size(); i++) {
                    MetricsVerificationGroup metricsVerificationGroup = metricsVerificationGroups.get(i);

                    MetricsXAxisLabel label = getXAxisLabel(metricsVerificationGroup.getStartTime(), i, selectedTimespan);
                    buildRuntime.set(label, Math.round(msToMin(metricsVerificationGroup.getRuntimeAverage())));

                    if (metricsVerificationGroup.getItems().size() > 0) {
                        getIdList().add(metricsVerificationGroup.getItems().get(0).getId());
                    } else {
                        getIdList().add(null);
                    }
                }
            }
        } catch (NotFoundException ex) {
            log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }

        //Set it empty so that the chart can be shown normally.
        if (buildRuntime.getData().isEmpty()) {
            buildRuntime.set(0, 0);
            displayEmptyDatasetMessage();
        }

        getDataModel().addSeries(buildRuntime);
    }

    public JobMetricsEJB getJobMetricsEJB() {
        return jobMetricsEJB;
    }

    public void setJobMetricsEJB(JobMetricsEJB jobMetricsEJB) {
        this.jobMetricsEJB = jobMetricsEJB;
    }

    @Override
    void open(Long id) {
        openBuild(id);
    }

    @Override
    protected String createPermalinkURL() {
        if (selectedResultStatus != null && !selectedResultStatus.equals("")) {
            return (super.createPermalinkURL() + selectedResultStatus + "/");
        } else {
            return super.createPermalinkURL();
        }
    }

    public List<String> getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(List<String> resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getSelectedResultStatus() {
        return selectedResultStatus;
    }

    public void setSelectedResultStatus(String selectedResultStatus) {
        this.selectedResultStatus = selectedResultStatus;
    }

    public List<String> completeResultStatus(String query) {
        return resultStatus;
    }
}
