package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.SlaveLabelEJB;
import com.nokia.ci.ejb.SlaveMachineEJB;
import com.nokia.ci.ejb.SlavePoolEJB;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsSlaveStat;
import com.nokia.ci.ejb.metrics.MetricsSlaveStatGroup;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ejb.metrics.SlaveMetricsEJB;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.SlaveMachine;
import com.nokia.ci.ejb.model.SlavePool;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for load balancer (slave statistical trend) metrics.
 *
 * @author larryang
 */
@Named
public class SysMetricsSlaveTrendBean extends MetricsLineChartBeanBase {

    private Logger log = LoggerFactory.getLogger(SysMetricsSlaveTrendBean.class);
    @Inject
    private SlaveMetricsEJB slaveMetricsEJB;
    @Inject
    private SlavePoolEJB slavePoolEJB;
    @Inject
    private SlaveLabelEJB slaveLabelEJB;
    @Inject
    private SlaveMachineEJB slaveMachineEJB;
    
    private List<String> slavePools;
    private List<String> slaveLabels;
    private List<String> slaveMachines;
    private String selectedSlavePool;
    private String selectedSlaveLabel;
    private String selectedSlaveMachine;
    
    private static final String KW_ALL = "ALL";
    private static final String KW_DEFAULT = "NoLabel";

    @Override
    public void init() {
        super.init();
        
        setMetricsLevel(MetricsLevel.SYSTEM);
        setJsfComponent("sysSlaveTrendChart.xhtml");
        setHeader("Slave Trend");
        
        selectedSlaveLabel = KW_ALL;
        selectedSlaveMachine = KW_ALL;

        getScaleOptions().add(MetricsTimespan.INDIVIDUAL.toString());
        getScaleOptions().add(MetricsTimespan.DAILY.toString());
        getScaleOptions().add(MetricsTimespan.WEEKLY.toString());
        getScaleOptions().add(MetricsTimespan.MONTHLY.toString());

        setSelectedScale(MetricsTimespan.DAILY.toString());

        initSlaveSelectionOptions();

        setRenderDiv("slaveTrendPanel");
        if (isRendered() && getChart() != null) {
            MetricsTimespan t = getTimespan();
            if (t != null) {
                setSelectedScale(t.toString());
            }

            String poolResult = getQueryParam("pool");
            if (poolResult != null) {
                selectedSlavePool = poolResult;
            }
            
            String labelResult = getQueryParam("label");
            if (labelResult != null) {
                selectedSlaveLabel = labelResult;
            }
            
            String machineResult = getQueryParam("machine");
            if (machineResult != null) {
                selectedSlaveMachine = machineResult;
            }

            updateDataModel();
        }
    }
    
    public void initSlaveSelectionOptions(){
        
        slavePools = new ArrayList<String>();
        for (SlavePool slavePool : slavePoolEJB.readAll()){
            slavePools.add(slavePool.getName());
        }
        
        slaveLabels = new ArrayList<String>();
        slaveLabels.add(KW_ALL);
        slaveLabels.add(KW_DEFAULT);
        for (SlaveLabel slaveLabel : slaveLabelEJB.readAll()){
            slaveLabels.add(slaveLabel.getName());
        }
        
        slaveMachines = new ArrayList<String>();
        slaveMachines.add(KW_ALL);
        for (SlaveMachine slaveMachine : slaveMachineEJB.readAll()){
            slaveMachines.add(slaveMachine.getUrl());
        }

    }
    
    public List<MetricsSlaveStat> getIndividualMetricsSlaveStats(Date start, Date end, TimeZone timeZone){
        
        MetricsSlaveStatGroup metricsSlaveStatGroup = new MetricsSlaveStatGroup();
        
        try{
            if (StringUtils.isNotEmpty(selectedSlaveLabel) && selectedSlaveLabel.equals(KW_ALL)
                    && StringUtils.isNotEmpty(selectedSlaveMachine) && selectedSlaveMachine.equals(KW_ALL)){
                metricsSlaveStatGroup = slaveMetricsEJB.getIndividualSlaveStatPerPool(
                        slavePoolEJB.getSlavePoolByName(selectedSlavePool).getId(), start, end, timeZone);
            }else if (StringUtils.isNotEmpty(selectedSlaveMachine) && selectedSlaveMachine.equals(KW_ALL)){
                Long slaveLabelId = selectedSlaveLabel.equals(KW_DEFAULT) ? null : slaveLabelEJB.getSlaveLabelByName(selectedSlaveLabel).getId();
                metricsSlaveStatGroup = slaveMetricsEJB.getIndividualSlaveStatPerLabel(
                        slavePoolEJB.getSlavePoolByName(selectedSlavePool).getId(), 
                        slaveLabelId, start, end, timeZone);
            }else {
                metricsSlaveStatGroup = slaveMetricsEJB.getIndividualSlaveStatPerMachine(
                        slavePoolEJB.getSlavePoolByName(selectedSlavePool).getId(), 
                        slaveMachineEJB.getSlaveMachineByURL(selectedSlaveMachine).getId(), start, end, timeZone);
            }
        }catch(Exception e){
            log.error("Exception when getting individual metrics slave stats. {}", e.getMessage() + e.getStackTrace());
        }
        
        return metricsSlaveStatGroup.getItems();
    }
    
    public List<MetricsSlaveStatGroup> getGroupedMetricsSlaveStats(Date start, Date end, MetricsTimespan timespan, TimeZone timeZone){
        
        List<MetricsSlaveStatGroup> metricsSlaveStatGroups = new ArrayList<MetricsSlaveStatGroup>();
        
        try{
        
            if (StringUtils.isNotEmpty(selectedSlaveLabel) && selectedSlaveLabel.equals(KW_ALL)
                    && StringUtils.isNotEmpty(selectedSlaveMachine) && selectedSlaveMachine.equals(KW_ALL)){
                metricsSlaveStatGroups = slaveMetricsEJB.getGroupedSlaveStatPerPool(
                        slavePoolEJB.getSlavePoolByName(selectedSlavePool).getId(), start, end, timespan, timeZone);
            }else if (StringUtils.isNotEmpty(selectedSlaveMachine) && selectedSlaveMachine.equals(KW_ALL)){
                Long slaveLabelId = selectedSlaveLabel.equals(KW_DEFAULT) ? null : slaveLabelEJB.getSlaveLabelByName(selectedSlaveLabel).getId();
                metricsSlaveStatGroups = slaveMetricsEJB.getGroupedSlaveStatPerLabel(
                        slavePoolEJB.getSlavePoolByName(selectedSlavePool).getId(), 
                        slaveLabelId, start, end, timespan, timeZone);
            }else {
                metricsSlaveStatGroups = slaveMetricsEJB.getGroupedSlaveStatPerMachine(
                        slavePoolEJB.getSlavePoolByName(selectedSlavePool).getId(), 
                        slaveMachineEJB.getSlaveMachineByURL(selectedSlaveMachine).getId(), start, end, timespan, timeZone);
            }

        }catch(Exception e){
            log.error("Exception when getting grouped metrics slave stats. {}", e.getMessage() + e.getStackTrace());
        }
        
        return metricsSlaveStatGroups;
    }

    @Override
    public void updateDataModel() {

        super.updateDataModel();

        MetricsTimespan selectedTimespan = getMetricsTimespan(getSelectedScale());

        ChartSeries reservedCount = new ChartSeries();
        reservedCount.setLabel("Reserved " + getSelectedScale());
        
        ChartSeries totalCount = new ChartSeries();
        totalCount.setLabel("Total " + getSelectedScale());

        try {
            if (selectedTimespan == MetricsTimespan.INDIVIDUAL) {
                
                List<MetricsSlaveStat> metricsSlaveStats = getIndividualMetricsSlaveStats(getStartDate(), getEndDate(), getTimezone());

                calculateXAxisLabelDivider(metricsSlaveStats.size());

                for (int i = 0; i < metricsSlaveStats.size(); i++) {
                    MetricsSlaveStat metricsSlaveStat = metricsSlaveStats.get(i);
                    MetricsXAxisLabel label = getXAxisLabel(metricsSlaveStat.getProvisionTime(), i, MetricsTimespan.INDIVIDUAL);
                    reservedCount.set(label, metricsSlaveStat.getReservedInstanceCount());
                    totalCount.set(label, metricsSlaveStat.getTotalInstanceCount());
                }
            } else {
                List<MetricsSlaveStatGroup> metricsSlaveStatGroups = getGroupedMetricsSlaveStats(
                        getStartDate(), getEndDate(), selectedTimespan, getTimezone());

                calculateXAxisLabelDivider(metricsSlaveStatGroups.size());

                for (int i = 0; i < metricsSlaveStatGroups.size(); i++) {
                    MetricsSlaveStatGroup metricsSlaveStatGroup = metricsSlaveStatGroups.get(i);

                    MetricsXAxisLabel label = getXAxisLabel(metricsSlaveStatGroup.getStartTime(), i, selectedTimespan);
                    reservedCount.set(label, metricsSlaveStatGroup.getReservedAverage());
                    totalCount.set(label, metricsSlaveStatGroup.getTotalAverage());
                }
            }
        } catch (Exception ex) {
            log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
        }

        //Set it empty so that the chart can be shown normally.
        if (reservedCount.getData().isEmpty()) {
            reservedCount.set(0, 0);
            displayEmptyDatasetMessage(reservedCount.getLabel());
        }

        getDataModel().addSeries(reservedCount);
        
        if (totalCount.getData().isEmpty()) {
            totalCount.set(0, 0);
            displayEmptyDatasetMessage(totalCount.getLabel());
        }

        getDataModel().addSeries(totalCount);
    }
    
    @Override
    public void itemSelect(ItemSelectEvent event) {
        MetricsXAxisLabel pointXAxisLabel = (MetricsXAxisLabel)getDataModel().getSeries().get(event.getSeriesIndex()).getData().keySet().toArray()[event.getItemIndex()];
        Number pointXAxisValue = getDataModel().getSeries().get(event.getSeriesIndex()).getData().get(pointXAxisLabel);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String pointLabelStr = formatter.format(pointXAxisLabel.getTime());

        addMessage("sysSlaveTrendChartForm", FacesMessage.SEVERITY_INFO, "Point detail", "x:" + pointLabelStr + " y:" + pointXAxisValue);
    }

    @Override
    protected String createPermalinkURL() {
        return (super.createPermalinkURL() + "0/0/0/0/" + selectedSlavePool + "/" + selectedSlaveLabel + "/" + selectedSlaveMachine + "/");
    }
    
    public List<String> completeSlavePoolOptions(String query){
        return slavePools;
    }
    
    public List<String> completeSlaveLabelOptions(String query){
        //reset selected slave machine.
        return slaveLabels;
    }

    public List<String> completeSlaveMachineOptions(String query){
        //reset selected slave label.
        return slaveMachines;
    }

    public String getSelectedSlaveLabel() {
        return selectedSlaveLabel;
    }

    public void setSelectedSlaveLabel(String selectedSlaveLabel) {
        this.selectedSlaveLabel = selectedSlaveLabel;
    }

    public String getSelectedSlaveMachine() {
        return selectedSlaveMachine;
    }

    public void setSelectedSlaveMachine(String selectedSlaveMachine) {
        this.selectedSlaveMachine = selectedSlaveMachine;
    }

    public String getSelectedSlavePool() {
        return selectedSlavePool;
    }

    public void setSelectedSlavePool(String selectedSlavePool) {
        this.selectedSlavePool = selectedSlavePool;
    }

    public List<String> getSlaveLabels() {
        return slaveLabels;
    }

    public void setSlaveLabels(List<String> slaveLabels) {
        this.slaveLabels = slaveLabels;
    }

    public List<String> getSlaveMachines() {
        return slaveMachines;
    }

    public void setSlaveMachines(List<String> slaveMachines) {
        this.slaveMachines = slaveMachines;
    }

    public List<String> getSlavePools() {
        return slavePools;
    }

    public void setSlavePools(List<String> slavePools) {
        this.slavePools = slavePools;
    }
}