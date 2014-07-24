/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.util.MathUtil;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author larryang
 */
public class MetricsBuildFailureCategoryGroup extends MetricsGroup<MetricsBuildFailureCategory> {

    private HashMap<String, Long> categorizedFailureCount = new HashMap<String, Long>();
    private Long totalCount = 0L;
    private ArrayList<String> reasons;
    
    public void add(MetricsBuildFailureCategory metricsBuildFailureCategory){
        for (String reason : metricsBuildFailureCategory.getReasons()){
            if (categorizedFailureCount.containsKey(reason)){
                categorizedFailureCount.put(reason, categorizedFailureCount.get(reason) + metricsBuildFailureCategory.getFailureCountByReason(reason));
            }else{
                categorizedFailureCount.put(reason, metricsBuildFailureCategory.getFailureCountByReason(reason));
            }
        }
        totalCount += metricsBuildFailureCategory.getTotalCount();
    }
    
    public ArrayList<String> getReasons(){
        reasons = new ArrayList<String>(categorizedFailureCount.keySet());
        return reasons;
    }
    
    public Long getFailureCountByReason(String reason){
        return (categorizedFailureCount.get(reason) != null ? categorizedFailureCount.get(reason) : 0);
    }
    
    public float getFailureRatioByReason(String reason){
        return (categorizedFailureCount.get(reason) != null ? MathUtil.getPercentage(categorizedFailureCount.get(reason), totalCount) : 0);
    }
    
    public boolean isEmpty(){
        return categorizedFailureCount.isEmpty();
    }
}
