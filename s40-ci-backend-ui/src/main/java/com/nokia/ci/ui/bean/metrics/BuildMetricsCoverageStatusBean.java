package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.MetricsBuildTestCoverage;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import java.util.List;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for test coverage status metrics.
 *
 * @author larryang
 */
@Named
public class BuildMetricsCoverageStatusBean extends BuildMetricsComponentBasedStatusBeanBase {

    private Logger log = LoggerFactory.getLogger(BuildMetricsCoverageStatusBean.class);

    @Override 
    protected void initProperties(){
        setMetricsLevel(MetricsLevel.BUILD);
        setJsfComponent("buildCoverageStatusChart.xhtml");
        setRenderDiv("coverageStatusPanel");
        setHeader("Coverage status");
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
                    getBuildMetricsEJB().getTestCoveragesOfLatestBuild(getBuildId(), 
                    getStartDate(), getEndDate(), getTimezone());

            for (int i = 0; i < metricsBuilds.size(); i++) {

                if (getSelectedComponents().contains(metricsBuilds.get(i).getComponentName())){
                    compCondCov.set(metricsBuilds.get(i).getComponentName(), metricsBuilds.get(i).getCondCov());
                    compStmtCov.set(metricsBuilds.get(i).getComponentName(), metricsBuilds.get(i).getStmtCov());
                    getIdList().add(metricsBuilds.get(i).getId());
                }
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
            
        }catch (NotFoundException ex) {
            log.debug("NotFoundException encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }catch (Exception allEx){
            log.debug("Exception encountered when updating data module: {}, {}.", allEx.getMessage(), allEx.getStackTrace());
        }
    }
    
}