/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.google.common.collect.Lists;
import com.nokia.ci.client.model.TestCoverageView;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Component;
import com.nokia.ci.ejb.model.TestCaseStat;
import com.nokia.ci.ejb.model.TestCoverage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author hhellgre
 */
public class TestCoverateViewUtilTest {

    @Test
    public void transformTestCoveragesTest() {
        List<TestCoverageView> mcvs = new ArrayList<TestCoverageView>();

        for (int i = 0; i < 5; i++) {
            TestCoverageView mv = new TestCoverageView();
            mv.setCondCov(1.0f);
            mv.setStmtCov(2.0f);
            mcvs.add(mv);
        }

        Map<Component, TestCoverage> result = TestCoverageViewUtil.transformTestCoverages(mcvs);

        Assert.assertEquals(mcvs.size(), result.size());

        List<TestCoverage> cons = new ArrayList<TestCoverage>(result.values());

        // Map values are in reverse order
        cons = Lists.reverse(cons);
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(mcvs.get(i).getCondCov() == cons.get(i).getCondCov());
            Assert.assertTrue(mcvs.get(i).getStmtCov() == cons.get(i).getStmtCov());
        }
    }

    @Test
    public void sanityCheckTestOk() {
        List<TestCoverageView> mcvs = new ArrayList<TestCoverageView>();

        for (int i = 0; i < 5; i++) {
            TestCoverageView mv = new TestCoverageView();
            mv.setComponentName("test");
            mv.setStmtCov(i * 1.0f);
            mv.setCondCov(i * 2.3f);
            mcvs.add(mv);
        }

        boolean result = TestCoverageViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(true, result);
    }

    @Test
    public void sanityCheckComponentFail() {
        List<TestCoverageView> mcvs = new ArrayList<TestCoverageView>();

        for (int i = 0; i < 5; i++) {
            TestCoverageView mv = new TestCoverageView();
            mv.setStmtCov(i * 1.0f);
            mv.setCondCov(i * 2.3f);
            mcvs.add(mv);
        }

        boolean result = TestCoverageViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }

    @Test
    public void sanityCheckStmtFail() {
        List<TestCoverageView> mcvs = new ArrayList<TestCoverageView>();

        for (int i = 0; i < 5; i++) {
            TestCoverageView mv = new TestCoverageView();
            mv.setComponentName("test");
            mv.setStmtCov(-1f);
            mv.setCondCov(i * 2.3f);
            mcvs.add(mv);
        }

        boolean result = TestCoverageViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }

    @Test
    public void sanityCheckCondFail() {
        List<TestCoverageView> mcvs = new ArrayList<TestCoverageView>();

        for (int i = 0; i < 5; i++) {
            TestCoverageView mv = new TestCoverageView();
            mv.setComponentName("test");
            mv.setStmtCov(1f);
            mv.setCondCov(-1f);
            mcvs.add(mv);
        }

        boolean result = TestCoverageViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }

    @Test
    public void fillPersistentObjectsToTestCoveragesTest() {

        Build build = new Build();
        List<TestCoverageView> mcvs = new ArrayList<TestCoverageView>();

        for (int i = 0; i < 5; i++) {
            TestCoverageView mv = new TestCoverageView();
            mv.setComponentName("test");
            mv.setCondCov(1.0f);
            mv.setStmtCov(2.0f);
            mcvs.add(mv);
        }

        Map<Component, TestCoverage> map = TestCoverageViewUtil.transformTestCoverages(mcvs);

        List<Component> components = new ArrayList<Component>();
        Component comp = new Component();
        comp.setName("test");
        components.add(comp);

        TestCoverageViewUtil.fillPersistentObjectsToTestCoverages(build, components, map);

        List<TestCoverage> cons = new ArrayList<TestCoverage>(map.values());
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(cons.get(i).getComponent(), comp);
        }
    }
}
