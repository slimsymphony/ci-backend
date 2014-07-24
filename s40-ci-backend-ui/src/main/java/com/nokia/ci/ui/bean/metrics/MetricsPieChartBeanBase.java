/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean.metrics;

import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author jajuutin
 */
public abstract class MetricsPieChartBeanBase extends MetricsBeanBase {

    private PieChartModel dataModel;

    @Override
    public void init() {
        super.init();
        initPieModel();
    }

    private void initPieModel() {
        dataModel = new PieChartModel();
        setEmptyDataset();
    }

    public void setEmptyDataset() {
        dataModel.set("Click refresh icon for data", 0);
    }

    @Override
    public void updateDataModel() {
        super.updateDataModel();
        dataModel.clear();
    }

    public PieChartModel getDataModel() {
        return dataModel;
    }

    public void setDataModel(PieChartModel dataModel) {
        this.dataModel = dataModel;
    }
}
