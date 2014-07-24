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
public class MetricsBuildFailureCategory extends MetricsBuild{
    
    private HashMap<String, Long> categorizedFailureCount = new HashMap<String, Long>();
    private Long totalCount = 0L;
    private ArrayList<String> reasons;
    
    public MetricsBuildFailureCategory(MetricsBuild metricsBuild){
        
        super(metricsBuild.getId(), metricsBuild.getStatus(), metricsBuild.getStartTime(), metricsBuild.getEndTime(), 
                metricsBuild.getVerificationUuid(), metricsBuild.getProductUuid());
    }

    public void addFailure(String reason){
        if (categorizedFailureCount.containsKey(reason)){
            categorizedFailureCount.put(reason, categorizedFailureCount.get(reason) + 1L);
        }else{
            categorizedFailureCount.put(reason, 1L);
        }
        totalCount++;
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
    
    public Long getTotalCount(){
        return totalCount;
    }
}
