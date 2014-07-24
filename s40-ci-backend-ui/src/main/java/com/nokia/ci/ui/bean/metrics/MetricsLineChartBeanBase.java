/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 *
 * @author jajuutin
 */
public abstract class MetricsLineChartBeanBase extends MetricsBeanBase {

    private static int MAX_AMOUNT_OF_X_LABELS = 8;
    private static String EMPTY_LABEL_TEXT = " "; // trick line chart to display nothing.
    private CartesianChartModel dataModel;
    private List<String> scaleOptions;
    private String selectedScale;
    private int labelDivider = 0;

    @Override
    public void init() {
        super.init();

        //Set common initial values.
        scaleOptions = new ArrayList<String>();

        // init data model.
        initChartModel();
    }

    @Override
    public void updateDataModel() {
        super.updateDataModel();
        dataModel.clear();
    }
    
    public void itemSelect(ItemSelectEvent event) {
        Long id = getIdList().get(event.getItemIndex());
        open(id);
    }
    
    public List<String> completeScaleOptions(String query) {
        return scaleOptions;
    }

    protected void calculateXAxisLabelDivider(int itemCount) {
        float itemCountFloat = itemCount;
        Double tmp = Math.ceil(itemCountFloat / MAX_AMOUNT_OF_X_LABELS);
        labelDivider = tmp.intValue();
        if (MAX_AMOUNT_OF_X_LABELS > itemCount || labelDivider > itemCount) {
            labelDivider = 1;
        }
    }

    protected MetricsXAxisLabel getXAxisLabel(Date date, int index, MetricsTimespan timespan) {
        // TODO: take user locale into account when creating these strings.                

        if ((index + 1) % labelDivider != 0) {
            return new MetricsXAxisLabel(date, EMPTY_LABEL_TEXT);
        }

        StringBuilder labelBuilder = new StringBuilder();

        Calendar cal = Calendar.getInstance(getTimezone());
        cal.setTime(date);

        if (timespan == MetricsTimespan.INDIVIDUAL) {
            String yearStr = Integer.toString(cal.get(Calendar.YEAR));
            labelBuilder.append(yearStr.substring(yearStr.length()-2));
            labelBuilder.append("-").append(cal.get(Calendar.MONTH) + 1);
            labelBuilder.append("-").append(cal.get(Calendar.DAY_OF_MONTH));
            labelBuilder.append(" ").append(cal.get(Calendar.HOUR_OF_DAY));
            labelBuilder.append(":").append(cal.get(Calendar.MINUTE));
        } else if (timespan == MetricsTimespan.DAILY) {
            labelBuilder.append(cal.get(Calendar.YEAR));
            labelBuilder.append("-").append(cal.get(Calendar.MONTH) + 1);
            labelBuilder.append("-").append(cal.get(Calendar.DAY_OF_MONTH));
        } else if (timespan == MetricsTimespan.WEEKLY) {
            labelBuilder.append(cal.get(Calendar.YEAR));
            labelBuilder.append("-").append("W").append(cal.get(Calendar.WEEK_OF_YEAR));
        } else if (timespan == MetricsTimespan.MONTHLY) {
            labelBuilder.append(cal.get(Calendar.YEAR));
            labelBuilder.append("-").append(cal.get(Calendar.MONTH) + 1);
        }

        return new MetricsXAxisLabel(date, labelBuilder.toString());
    }

    protected void initChartModel() {
        dataModel = new CartesianChartModel();
        ChartSeries emptySeries = new ChartSeries();
        emptySeries.setLabel("Click refresh icon for data");
        emptySeries.set(0, 0);
        dataModel.addSeries(emptySeries);
    }
    
    @Override
    protected String createPermalinkURL() {
        if (selectedScale == null){
            return (super.createPermalinkURL() + "null" + "/");
        }else{
            return (super.createPermalinkURL() + selectedScale + "/");
        }
    }
    
    @Override
    protected boolean checkEmptyInputSelection(List selection, String dataLabel){
        
        if (super.checkEmptyInputSelection(selection, dataLabel)){
            ChartSeries emptySeries = new ChartSeries();
            emptySeries.set(0, 0);
            emptySeries.setLabel("No " + dataLabel + " selected");
            getDataModel().addSeries(emptySeries);
            return true;
        }else{
            return false;
        }
    }

    /**
     * @return the dataModel
     */
    public CartesianChartModel getDataModel() {
        return dataModel;
    }

    /**
     * @param dataModel the dataModel to set
     */
    public void setDataModel(CartesianChartModel dataModel) {
        this.dataModel = dataModel;
    }

    /**
     * @return the scaleOptions
     */
    public List<String> getScaleOptions() {
        return scaleOptions;
    }

    /**
     * @param scaleOptions the scaleOptions to set
     */
    public void setScaleOptions(List<String> scaleOptions) {
        this.scaleOptions = scaleOptions;
    }

    /**
     * @return the selectedScale
     */
    public String getSelectedScale() {
        return selectedScale;
    }

    /**
     * @param selectedScale the selectedScale to set
     */
    public void setSelectedScale(String selectedScale) {
        this.selectedScale = selectedScale;
    }

    /**
     * @return the labelDivider
     */
    public int getLabelDivider() {
        return labelDivider;
    }

    /**
     * @param labelDivider the labelDivider to set
     */
    public void setLabelDivider(int labelDivider) {
        this.labelDivider = labelDivider;
    }
}
