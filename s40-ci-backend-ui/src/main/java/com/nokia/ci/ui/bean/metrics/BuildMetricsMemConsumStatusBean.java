package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.MetricsBuildMemConsumption;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import java.util.List;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for memory consumption status metrics.
 *
 * @author larryang
 */
@Named
public class BuildMetricsMemConsumStatusBean extends BuildMetricsComponentBasedStatusBeanBase {

    private Logger log = LoggerFactory.getLogger(BuildMetricsMemConsumStatusBean.class);
    
    @Override 
    protected void initProperties(){
        setMetricsLevel(MetricsLevel.BUILD);
        setJsfComponent("buildMemConsumStatusChart.xhtml");
        setRenderDiv("memConsumStatusPanel");
        setHeader("Memory consumption status");
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
                    getBuildMetricsEJB().getMemConsumptionsOfLatestBuild(getBuildId(), 
                    getStartDate(), getEndDate(), getTimezone());

            for (int i = 0; i < metricsBuilds.size(); i++) {

                if (getSelectedComponents().contains(metricsBuilds.get(i).getComponentName())){
                    compRamMemConsumptoin.set(metricsBuilds.get(i).getComponentName(), metricsBuilds.get(i).getRam());
                    compRomMemConsumptoin.set(metricsBuilds.get(i).getComponentName(), metricsBuilds.get(i).getRom());
                    getIdList().add(metricsBuilds.get(i).getId());
                }
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
            
        }catch (NotFoundException ex) {
            log.debug("NotFoundException encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }catch (Exception allEx){
            log.debug("Exception encountered when updating data module: {}, {}.", allEx.getMessage(), allEx.getStackTrace());
        }
    }

}