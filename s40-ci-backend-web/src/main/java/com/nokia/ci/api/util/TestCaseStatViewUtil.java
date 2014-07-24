/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.nokia.ci.client.model.TestCaseStatView;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Component;
import com.nokia.ci.ejb.model.TestCaseStat;
import com.nokia.ci.ejb.util.RelationUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author larryang
 */
public class TestCaseStatViewUtil {

    public static Map<Component, TestCaseStat> transformTestCaseStats(List<TestCaseStatView> tcsvs) {

        Map<Component, TestCaseStat> compTestCaseStats = new HashMap();

        for (TestCaseStatView tcsv : tcsvs) {

            Component component = new Component();
            component.setName(tcsv.getComponentName());

            TestCaseStat testCaseStat = new TestCaseStat();

            testCaseStat.setComponent(component);
            testCaseStat.setPassedCount(tcsv.getPassedCount());
            testCaseStat.setFailedCount(tcsv.getFailedCount());
            testCaseStat.setNaCount(tcsv.getNaCount());
            testCaseStat.setTotalCount(tcsv.getTotalCount());

            component.getTestCaseStats().add(testCaseStat);
            compTestCaseStats.put(component, testCaseStat);
        }

        return compTestCaseStats;
    }

    public static boolean sanityCheck(List<TestCaseStatView> tcsvs) {

        for (TestCaseStatView tcsv : tcsvs) {

            if (tcsv.getComponentName() == null) {
                return false;
            }

            if (tcsv.getPassedCount() == null || tcsv.getPassedCount() < 0) {
                return false;
            }

            if (tcsv.getFailedCount() == null || tcsv.getFailedCount() < 0) {
                return false;
            }

            if (tcsv.getNaCount() == null || tcsv.getNaCount() < 0) {
                return false;
            }

            if (tcsv.getTotalCount() == null || tcsv.getTotalCount() < 0) {
                return false;
            }
        }

        return true;
    }

    public static void fillPersistentObjectsToTestCaseStats(Build build, List<Component> components, Map<Component, TestCaseStat> componentTestCaseStats) {

        for (Map.Entry<Component, TestCaseStat> entry : componentTestCaseStats.entrySet()) {

            for (Component component : components) {
                if (entry.getValue().getComponent().getName().equals(component.getName())) {
                    RelationUtil.partialRelate(component, entry.getValue());
                }
            }
        }
    }
}