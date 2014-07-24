/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.google.common.collect.Lists;
import com.nokia.ci.client.model.MemConsumptionView;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Component;
import com.nokia.ci.ejb.model.MemConsumption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author hhellgre
 */
public class MemConsumptionViewUtilTest {

    @Test
    public void transformMemConsumptionsTest() {
        List<MemConsumptionView> mcvs = new ArrayList<MemConsumptionView>();

        for (int i = 0; i < 5; i++) {
            MemConsumptionView mv = new MemConsumptionView();
            mv.setComponentName("test");
            mv.setRam(i);
            mv.setRom(i);
            mcvs.add(mv);
        }

        Map<Component, MemConsumption> result = MemConsumptionViewUtil.transformMemConsumptions(mcvs);

        Assert.assertEquals(mcvs.size(), result.size());

        List<MemConsumption> cons = new ArrayList<MemConsumption>(result.values());

        // Map values are in reverse order
        cons = Lists.reverse(cons);
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(mcvs.get(i).getRam() == cons.get(i).getRam());
            Assert.assertTrue(mcvs.get(i).getRom() == cons.get(i).getRom());
        }
    }

    @Test
    public void sanityCheckTestOk() {
        List<MemConsumptionView> mcvs = new ArrayList<MemConsumptionView>();

        for (int i = 0; i < 5; i++) {
            MemConsumptionView mv = new MemConsumptionView();
            mv.setComponentName("test");
            mv.setRam(i);
            mv.setRom(i);
            mcvs.add(mv);
        }

        boolean result = MemConsumptionViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(true, result);
    }

    @Test
    public void sanityCheckComponentFail() {
        List<MemConsumptionView> mcvs = new ArrayList<MemConsumptionView>();

        for (int i = 0; i < 5; i++) {
            MemConsumptionView mv = new MemConsumptionView();
            mv.setRam(i);
            mv.setRom(i);
            mcvs.add(mv);
        }

        boolean result = MemConsumptionViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }

    @Test
    public void sanityCheckRamFail() {
        List<MemConsumptionView> mcvs = new ArrayList<MemConsumptionView>();

        for (int i = 0; i < 5; i++) {
            MemConsumptionView mv = new MemConsumptionView();
            mv.setComponentName("test");
            mv.setRam(-1);
            mv.setRom(-i);
            mcvs.add(mv);
        }

        boolean result = MemConsumptionViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }

    @Test
    public void sanityCheckRomFail() {
        List<MemConsumptionView> mcvs = new ArrayList<MemConsumptionView>();

        for (int i = 0; i < 5; i++) {
            MemConsumptionView mv = new MemConsumptionView();
            mv.setComponentName("test");
            mv.setRam(i);
            mv.setRom(-i);
            mcvs.add(mv);
        }

        boolean result = MemConsumptionViewUtil.sanityCheck(mcvs);

        Assert.assertEquals(false, result);
    }

    @Test
    public void fillPersistentObjectsToMemConsumptionsTest() {

        Build build = new Build();
        List<MemConsumptionView> mcvs = new ArrayList<MemConsumptionView>();

        for (int i = 0; i < 5; i++) {
            MemConsumptionView mv = new MemConsumptionView();
            mv.setComponentName("test");
            mv.setRam(i);
            mv.setRom(i);
            mcvs.add(mv);
        }

        Map<Component, MemConsumption> map = MemConsumptionViewUtil.transformMemConsumptions(mcvs);

        List<Component> components = new ArrayList<Component>();
        Component comp = new Component();
        comp.setName("test");
        components.add(comp);

        MemConsumptionViewUtil.fillPersistentObjectsToMemConsumptions(build, components, map);

        List<MemConsumption> cons = new ArrayList<MemConsumption>(map.values());
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(cons.get(i).getComponent(), comp);
        }
    }
}
