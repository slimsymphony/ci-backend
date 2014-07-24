package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.MetricsBuild;
import com.nokia.ci.ejb.metrics.MetricsBuildMemConsumption;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.List;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for memory consumption trend metrics.
 *
 * @author larryang
 */
@Named
public class BuildMetricsMemConsumTrendBean extends BuildMetricsComponentBasedTrendBeanBase {

    private Logger log = LoggerFactory.getLogger(BuildMetricsMemConsumTrendBean.class);
    
    @Override
    protected void initProperties(){
        setMetricsLevel(MetricsLevel.BUILD);
        setJsfComponent("buildMemConsumTrendChart.xhtml");
        setRenderDiv("memConsumTrendPanel");
        setHeader("Memory consumption trend");
    }
    
    @Override
    public void updateDataModel() {
        super.updateDataModel();

        ChartSeries compRamMemConsumptoin = new ChartSeries();
        compRamMemConsumptoin.setLabel("RAM");
        
        ChartSeries compRomMemConsumptoin = new ChartSeries();
        compRomMemConsumptoin.setLabel("ROM");

        try {
            List<MetricsBuildMemConsumption> metricsBuilds = 
                    getBuildMetricsEJB().getMemConsumptionOfBuilds(getBuildId(), 
                    getStartDate(), getEndDate(), getTimezone(), getSelectedComponent());

            calculateXAxisLabelDivider(metricsBuilds.size());

            for (int i = 0; i < metricsBuilds.size(); i++) {
                MetricsBuild metricsBuild = metricsBuilds.get(i);

                MetricsXAxisLabel label = getXAxisLabel(metricsBuild.getStartTime(), i, MetricsTimespan.INDIVIDUAL);

                compRamMemConsumptoin.set(label, metricsBuilds.get(i).getRam());
                compRomMemConsumptoin.set(label, metricsBuilds.get(i).getRom());

                getIdList().add(metricsBuilds.get(i).getId());
            }
        } catch (NotFoundException ex) {
            log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }

        //Set it empty so that the chart can be shown normally.
        if (compRamMemConsumptoin.getData().isEmpty()) {
            compRamMemConsumptoin.set(0, 0);
            displayEmptyDatasetMessage(compRamMemConsumptoin.getLabel());
        }

        getDataModel().addSeries(compRamMemConsumptoin);
        
        if (compRomMemConsumptoin.getData().isEmpty()) {
            compRomMemConsumptoin.set(0, 0);
            displayEmptyDatasetMessage(compRomMemConsumptoin.getLabel());
        }

        getDataModel().addSeries(compRomMemConsumptoin);
    }    
}

