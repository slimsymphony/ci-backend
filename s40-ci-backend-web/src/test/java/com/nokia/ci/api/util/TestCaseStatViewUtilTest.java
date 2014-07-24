/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.google.common.collect.Lists;
import com.nokia.ci.client.model.TestCaseStatView;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Component;
import com.nokia.ci.ejb.model.TestCaseStat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author hhellgre
 */
public class TestCaseStatViewUtilTest {

    @Test
    public void transformTestCasesTest() {
        List<TestCaseStatView> mcvs = new ArrayList<TestCaseStatView>();

        for (int i = 0; i < 5; i++) {
            TestCaseStatView mv = new TestCaseStatView();
            mv.setComponentName("test");
            mv.setPassedCount(i * 20);
            mv.setFailedCount(i * 2);
            mv.setNaCount(i * 1);
            mv.setTotalCount(i * 21);
            mcvs.add(mv);
        }

        Map<Component, TestCaseStat> result = TestCaseStatViewUtil.transformTestCaseStats(mcvs);

        Assert.assertEquals(mcvs.size(), result.size());

        List<TestCaseStat> cons = new ArrayList<TestCaseStat>(result.values());

        // Map values are in reverse order
        cons = Lists.reverse(cons);
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(mcvs.get(i).getPassedCount() == cons.get(i).getPassedCount());
            Assert.assertTrue(mcvs.get(i).getFailedCount() == cons.get(i).getFailedCount());
            Assert.assertTrue(mcvs.get(i).getNaCount() == cons.get(i).getNaCount());
            Assert.assertTrue(mcvs.get(i).getTotalCount() == cons.get(i).getTotalCount());
        }
    }

    @Test
    public void sanityCheckTestOk() {
        List<TestCaseStatView> mcvs = new ArrayList<TestCaseStatView>();

        for (int i = 0; i < 5; i++) {
            TestCaseStatView mv = new TestCaseStatView();
            mv.setComponentName("test");
            mv.setPassedCount(i * 20);
            mv.setFailedCount(i * 2);
            mv.setNaCount(i * 1);
            mv.setTotalCount(i * 21);
            mcvs.add(mv);
        }

        boolean result = TestCaseStatViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(true, result);
    }

    @Test
    public void sanityCheckComponentFail() {
        List<TestCaseStatView> mcvs = new ArrayList<TestCaseStatView>();

        for (int i = 0; i < 5; i++) {
            TestCaseStatView mv = new TestCaseStatView();
            mv.setPassedCount(i * 20);
            mv.setFailedCount(i * 2);
            mv.setNaCount(i * 1);
            mv.setTotalCount(i * 21);
            mcvs.add(mv);
        }
        boolean result = TestCaseStatViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }

    @Test
    public void sanityCheckTotalCountFail() {
        List<TestCaseStatView> mcvs = new ArrayList<TestCaseStatView>();

        for (int i = 0; i < 5; i++) {
            TestCaseStatView mv = new TestCaseStatView();
            mv.setComponentName("test");
            mv.setPassedCount(i * 20);
            mv.setFailedCount(i * 2);
            mv.setNaCount(i * 1);
            mv.setTotalCount(-i);
            mcvs.add(mv);
        }

        boolean result = TestCaseStatViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }

    @Test
    public void sanityCheckPassedCountFail() {
        List<TestCaseStatView> mcvs = new ArrayList<TestCaseStatView>();

        for (int i = 0; i < 5; i++) {
            TestCaseStatView mv = new TestCaseStatView();
            mv.setComponentName("test");
            mv.setPassedCount(-i);
            mv.setFailedCount(i * 2);
            mv.setNaCount(i * 1);
            mv.setTotalCount(100);
            mcvs.add(mv);
        }

        boolean result = TestCaseStatViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }
    
    @Test
    public void sanityCheckFailedCountFail() {
        List<TestCaseStatView> mcvs = new ArrayList<TestCaseStatView>();

        for (int i = 0; i < 5; i++) {
            TestCaseStatView mv = new TestCaseStatView();
            mv.setComponentName("test");
            mv.setPassedCount(100);
            mv.setFailedCount(-i);
            mv.setNaCount(0);
            mv.setTotalCount(100);
            mcvs.add(mv);
        }

        boolean result = TestCaseStatViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }
    
    @Test
    public void sanityCheckNaCountFail() {
        List<TestCaseStatView> mcvs = new ArrayList<TestCaseStatView>();

        for (int i = 0; i < 5; i++) {
            TestCaseStatView mv = new TestCaseStatView();
            mv.setComponentName("test");
            mv.setPassedCount(90);
            mv.setFailedCount(i);
            mv.setNaCount(-i);
            mv.setTotalCount(100);
            mcvs.add(mv);
        }

        boolean result = TestCaseStatViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }

    @Test
    public void fillPersistentObjectsToTestCaseStatsTest() {

        Build build = new Build();
        List<TestCaseStatView> mcvs = new ArrayList<TestCaseStatView>();

        for (int i = 0; i < 5; i++) {
            TestCaseStatView mv = new TestCaseStatView();
            mv.setComponentName("test");
            mv.setPassedCount(i * 20);
            mv.setTotalCount(-i);
            mcvs.add(mv);
        }

        Map<Component, TestCaseStat> map = TestCaseStatViewUtil.transformTestCaseStats(mcvs);

        List<Component> components = new ArrayList<Component>();
        Component comp = new Component();
        comp.setName("test");
        components.add(comp);

        TestCaseStatViewUtil.fillPersistentObjectsToTestCaseStats(build, components, map);

        List<TestCaseStat> cons = new ArrayList<TestCaseStat>(map.values());
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(cons.get(i).getComponent(), comp);
        }
    }
}
