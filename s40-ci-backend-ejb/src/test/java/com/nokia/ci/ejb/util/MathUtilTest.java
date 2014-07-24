/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author hhellgre
 */
public class MathUtilTest {

    @Test
    public void percentageIntTest() {
        int result = MathUtil.getPercentageInt(50, 100);
        Assert.assertEquals(50, result);
    }

    @Test
    public void percentageFloatTest() {
        float result = MathUtil.getPercentage(50, 100);
        Assert.assertTrue(50f == result);
    }
}
