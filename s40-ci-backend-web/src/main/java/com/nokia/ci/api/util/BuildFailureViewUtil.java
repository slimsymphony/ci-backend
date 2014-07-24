/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.nokia.ci.client.model.BuildFailureView;
import com.nokia.ci.ejb.model.BuildFailure;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author stirrone
 */
public class BuildFailureViewUtil {
    
    public static boolean sanityCheck(List<BuildFailureView> buildFailureViews) {
        
        //test case name has to be set.
        //type and message only for granite & cppunit.
        //relativepath only for granite.
        for (BuildFailureView bfv : buildFailureViews) {
            
            if (StringUtils.isBlank(bfv.getTestcaseName())) {
                return false;
            }
        }
        
        return true;
    }
    
    public static List<BuildFailure> transformBuildFailures(List<BuildFailureView> buildFailureViews) {
        
        List<BuildFailure> buildFailures = new ArrayList<BuildFailure>();
        
        for (BuildFailureView bfv : buildFailureViews) {
            
            BuildFailure failure = new BuildFailure();
            failure.setTestcaseName(bfv.getTestcaseName());
            failure.setType(bfv.getType());
            failure.setMessage(bfv.getMessage());
            failure.setRelativePath(bfv.getRelativePath());
            buildFailures.add(failure);
            
        }
        return buildFailures;
    }
    
}
