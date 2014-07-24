/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.metrics.MetricsBuildFailureCategory;
import com.nokia.ci.ejb.metrics.MetricsBuildFailureCategoryGroup;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;

/**
 *
 * @author larryang
 */
@Named
public class BuildMetricsFailureReasonCountBean extends BuildMetricsFailureReasonBeanBase {
    
    @Override
    protected void initProperties(){
        setMetricsLevel(MetricsLevel.BUILD);
        setJsfComponent("buildFailureReasonCountChart.xhtml");
        setHeader("Failure reason count");
        setRenderDiv("failureReasonCountPanel");
    }
    
    @Override
    protected void setFailureBuildData(ChartSeries failureReasonTrend, MetricsXAxisLabel label, MetricsBuildFailureCategory failureBuild, String failureReason){
        failureReasonTrend.set(label, failureBuild.getFailureCountByReason(failureReason));
    }
    
    @Override
    protected void setFailureBuildGroupData(ChartSeries failureReasonTrend, MetricsXAxisLabel label, MetricsBuildFailureCategoryGroup failureBuildGroup, String failureReason){
        failureReasonTrend.set(label, failureBuildGroup.getFailureCountByReason(failureReason));
    }
}
