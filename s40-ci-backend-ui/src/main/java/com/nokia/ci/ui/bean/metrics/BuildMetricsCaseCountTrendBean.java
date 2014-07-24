package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.MetricsBuild;
import com.nokia.ci.ejb.metrics.MetricsBuildTestCaseStat;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.List;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for test case count trend metrics.
 *
 * @author larryang
 */
@Named
public class BuildMetricsCaseCountTrendBean extends BuildMetricsComponentBasedTrendBeanBase {

    private Logger log = LoggerFactory.getLogger(BuildMetricsCaseCountTrendBean.class);

    @Override
    protected void initProperties() {
        setMetricsLevel(MetricsLevel.BUILD);
        setJsfComponent("buildCaseCountTrendChart.xhtml");
        setRenderDiv("caseCountTrendPanel");
        setHeader("Case count trend");
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
                    getBuildMetricsEJB().getTestCaseStatOfBuilds(getBuildId(),
                    getStartDate(), getEndDate(), getTimezone(), getSelectedComponent());

            calculateXAxisLabelDivider(metricsBuilds.size());

            for (int i = 0; i < metricsBuilds.size(); i++) {
                MetricsBuild metricsBuild = metricsBuilds.get(i);

                MetricsXAxisLabel label = getXAxisLabel(metricsBuild.getStartTime(), i, MetricsTimespan.INDIVIDUAL);

                //null check required to display old records correctly.
                Integer passedCount = metricsBuilds.get(i).getPassedCount() != null ? metricsBuilds.get(i).getPassedCount() : 0;
                compPassedCaseCount.set(label, passedCount);

                Integer failedCount = metricsBuilds.get(i).getFailedCount() != null ? metricsBuilds.get(i).getFailedCount() : 0;
                compFailedCaseCount.set(label, failedCount);

                Integer naCount = metricsBuilds.get(i).getNaCount() != null ? metricsBuilds.get(i).getNaCount() : 0;
                compNaCaseCount.set(label, naCount);

                //match total for older records, or in case some tests do not have result.
                int otherCount = metricsBuilds.get(i).getTotalCount().intValue() - passedCount.intValue() - failedCount.intValue() - naCount.intValue();
                if (otherCount > 0) {
                    compOtherCaseCount.set(label, otherCount);
                } else {
                    compOtherCaseCount.set(label, 0);
                }

                getIdList().add(metricsBuilds.get(i).getId());
            }
        } catch (NotFoundException ex) {
            log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
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
    }
}