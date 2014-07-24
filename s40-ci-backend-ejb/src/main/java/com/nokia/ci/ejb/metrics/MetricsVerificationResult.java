/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.model.BuildStatus;

/**
 *
 * @author jajuutin
 */
public enum MetricsVerificationResult {
    PASSED, FAILED, ALL;
    
    public static MetricsVerificationResult getResult(BuildStatus buildStatus) {
        if(buildStatus == BuildStatus.SUCCESS || buildStatus == BuildStatus.UNSTABLE) {
            return PASSED;
        } else {
            return FAILED;
        }
    }
    
    @Override
    public String toString(){
        
        if (this == PASSED){
            return "SUCCESS&UNSTABLE";
        }else{
            return super.toString();
        }
    }
    
    public static MetricsVerificationResult getEnum(String strValue){
        
        if (strValue.equals("SUCCESS&UNSTABLE")){
            return PASSED;
        }else{
            return valueOf(strValue);
        }
    }
}
