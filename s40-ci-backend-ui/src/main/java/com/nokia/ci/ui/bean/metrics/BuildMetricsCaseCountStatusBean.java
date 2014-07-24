package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.MetricsBuildTestCaseStat;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import java.util.List;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for test case count status metrics.
 *
 * @author larryang
 */
@Named
public class BuildMetricsCaseCountStatusBean extends BuildMetricsComponentBasedStatusBeanBase {

    private Logger log = LoggerFactory.getLogger(BuildMetricsCaseCountStatusBean.class);

    @Override
    protected void initProperties() {
        setMetricsLevel(MetricsLevel.BUILD);
        setJsfComponent("buildCaseCountStatusChart.xhtml");
        setRenderDiv("caseCountStatusPanel");
        setHeader("Case count status");
    }

    @Override
    public void updateDataModel() {
        super.updateDataModel();

        ChartSeries compPassedCaseCount = new ChartSeries();
        compPassedCaseCount.setLabel("Passed");

        ChartSeries compFailedCaseCount = new ChartSeries();
        compFailedCaseCount.setLabel("Failed");

        ChartSeries compNaCaseCount = new ChartSeries();
        compNaCaseCount.setLabel("N/A");

        ChartSeries compOtherCaseCount = new ChartSeries();
        compOtherCaseCount.setLabel("Other");

        try {
            List<MetricsBuildTestCaseStat> metricsBuilds =
                    getBuildMetricsEJB().getTestCaseStatsOfLatestBuild(getBuildId(),
                    getStartDate(), getEndDate(), getTimezone());

            for (int i = 0; i < metricsBuilds.size(); i++) {

                if (getSelectedComponents().contains(metricsBuilds.get(i).getComponentName())) {

                    //null check required to display old records correctly.
                    Integer passedCount = metricsBuilds.get(i).getPassedCount() != null ? metricsBuilds.get(i).getPassedCount() : 0;
                    compPassedCaseCount.set(metricsBuilds.get(i).getComponentName(), passedCount);

                    Integer failedCount = metricsBuilds.get(i).getFailedCount() != null ? metricsBuilds.get(i).getFailedCount() : 0;
                    compFailedCaseCount.set(metricsBuilds.get(i).getComponentName(), failedCount);

                    Integer naCount = metricsBuilds.get(i).getNaCount() != null ? metricsBuilds.get(i).getNaCount() : 0;
                    compNaCaseCount.set(metricsBuilds.get(i).getComponentName(), naCount);

                    //match total for older records, or in case some tests do not have result.
                    int otherCount = metricsBuilds.get(i).getTotalCount().intValue() - passedCount.intValue() - failedCount.intValue() - naCount.intValue();
                    if (otherCount > 0) {
                        compOtherCaseCount.set(metricsBuilds.get(i).getComponentName(), otherCount);
                    } else {
                        compOtherCaseCount.set(metricsBuilds.get(i).getComponentName(), 0);
                    }

                    getIdList().add(metricsBuilds.get(i).getId());
                }
            }

            //Set it empty so that the chart can be shown normally.
            if (compPassedCaseCount.getData().isEmpty()) {
                compPassedCaseCount.set(0, 0);
                displayEmptyDatasetMessage(compPassedCaseCount.getLabel());
            }
            getDataModel().addSeries(compPassedCaseCount);

            if (compFailedCaseCount.getData().isEmpty()) {
                compFailedCaseCount.set(0, 0);
                displayEmptyDatasetMessage(compFailedCaseCount.getLabel());
            }
            getDataModel().addSeries(compFailedCaseCount);

            if (compNaCaseCount.getData().isEmpty()) {
                compNaCaseCount.set(0, 0);
                displayEmptyDatasetMessage(compNaCaseCount.getLabel());
            }
            getDataModel().addSeries(compNaCaseCount);

            if (compOtherCaseCount.getData().isEmpty()) {
                compOtherCaseCount.set(0, 0);
                displayEmptyDatasetMessage(compOtherCaseCount.getLabel());
            }
            getDataModel().addSeries(compOtherCaseCount);

        } catch (NotFoundException ex) {
            log.debug("NotFoundException encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        } catch (Exception allEx) {
            log.debug("Exception encountered when updating data module: {}, {}.", allEx.getMessage(), allEx.getStackTrace());
        }
    }
}