package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.MetricsBuild;
import com.nokia.ci.ejb.metrics.MetricsBuildTestCoverage;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.List;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for test coverage trend metrics.
 *
 * @author larryang
 */
@Named
public class BuildMetricsCoverageTrendBean extends BuildMetricsComponentBasedTrendBeanBase {

    private Logger log = LoggerFactory.getLogger(BuildMetricsCoverageTrendBean.class);
    
    @Override
    protected void initProperties(){
        setMetricsLevel(MetricsLevel.BUILD);
        setJsfComponent("buildCoverageTrendChart.xhtml");
        setRenderDiv("coverageTrendPanel");
        setHeader("Coverage trend");
    }
    
    @Override
    public void updateDataModel() {
        super.updateDataModel();

        ChartSeries compCondCov = new ChartSeries();
        compCondCov.setLabel("CondCov");
        
        ChartSeries compStmtCov = new ChartSeries();
        compStmtCov.setLabel("StmtCov");

        try {
            List<MetricsBuildTestCoverage> metricsBuilds = 
                    getBuildMetricsEJB().getTestCoverageOfBuilds(getBuildId(), 
                    getStartDate(), getEndDate(), getTimezone(), getSelectedComponent());

            calculateXAxisLabelDivider(metricsBuilds.size());

            for (int i = 0; i < metricsBuilds.size(); i++) {
                MetricsBuild metricsBuild = metricsBuilds.get(i);

                MetricsXAxisLabel label = getXAxisLabel(metricsBuild.getStartTime(), i, MetricsTimespan.INDIVIDUAL);

                compCondCov.set(label, metricsBuilds.get(i).getCondCov());
                compStmtCov.set(label, metricsBuilds.get(i).getStmtCov());

                getIdList().add(metricsBuilds.get(i).getId());
            }
        } catch (NotFoundException ex) {
            log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }

        //Set it empty so that the chart can be shown normally.
        if (compCondCov.getData().isEmpty()) {
            compCondCov.set(0, 0);
            displayEmptyDatasetMessage(compCondCov.getLabel());
        }

        getDataModel().addSeries(compCondCov);
        
        if (compStmtCov.getData().isEmpty()) {
            compStmtCov.set(0, 0);
            displayEmptyDatasetMessage(compStmtCov.getLabel());
        }

        getDataModel().addSeries(compStmtCov);
    }
}