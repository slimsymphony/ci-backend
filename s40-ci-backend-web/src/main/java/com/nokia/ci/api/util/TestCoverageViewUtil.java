/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.nokia.ci.client.model.TestCaseStatView;
import com.nokia.ci.client.model.TestCoverageView;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Component;
import com.nokia.ci.ejb.model.TestCaseStat;
import com.nokia.ci.ejb.model.TestCoverage;
import com.nokia.ci.ejb.util.RelationUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author larryang
 */
public class TestCoverageViewUtil {

    public static Map<Component, TestCoverage> transformTestCoverages(List<TestCoverageView> tcvs) {
        
        Map<Component, TestCoverage> compTestCoverages = new HashMap();
        
        for (TestCoverageView tcv : tcvs){
           
            Component component = new Component();
            component.setName(tcv.getComponentName());
            
            TestCoverage testCoverage = new TestCoverage();

            testCoverage.setComponent(component);
            testCoverage.setCondCov(tcv.getCondCov());
            testCoverage.setStmtCov(tcv.getStmtCov());
            
            component.getTestCoverages().add(testCoverage);
            compTestCoverages.put(component, testCoverage);
        }
        
        return compTestCoverages;
    }
    
    public static boolean sanityCheck(List<TestCoverageView> tcvs) {
        
        for (TestCoverageView tcv : tcvs) {

            if (tcv.getComponentName() == null) {
                return false;
            }

            if (tcv.getCondCov() < 0) {
                return false;
            }
            
            if(tcv.getStmtCov() < 0) {
                return false;
            }
        }

        return true;
    }
    
    public static void fillPersistentObjectsToTestCoverages(Build build, List<Component> components, Map<Component, TestCoverage> componentTestCoverages){
        
        for (Map.Entry<Component, TestCoverage> entry : componentTestCoverages.entrySet()){

            for (Component component : components){
                if (entry.getValue().getComponent().getName().equals(component.getName())){
                    RelationUtil.partialRelate(component, entry.getValue());
                }
            }
        }
    }
    
}