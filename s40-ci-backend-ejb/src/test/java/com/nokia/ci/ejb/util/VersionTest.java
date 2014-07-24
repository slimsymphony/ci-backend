/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author hhellgre
 */
public class VersionTest {

    @Test
    public void newerThanTest() {
        Version v1 = new Version("1.2.3");
        Version v2 = new Version("1.1.1");

        boolean ret = v1.newerThan(v2);

        Assert.assertEquals(true, ret);
    }

    @Test
    public void olderThanTest() {
        Version v1 = new Version("1.2.3");
        Version v2 = new Version("1.7.1");

        boolean ret = v1.olderThan(v2);

        Assert.assertEquals(true, ret);
    }

    @Test
    public void sameThanTest() {
        Version v1 = new Version("1.2.3");
        Version v2 = new Version("1.2.3");

        boolean ret = v1.equals(v2);

        Assert.assertEquals(true, ret);

        ret = v1.equals(v1);

        Assert.assertEquals(true, ret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notCorrectVersionNumber() throws Exception {
        Version v = new Version("Not a version number");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullVersionNumber() throws Exception {
        Version v = new Version(null);
    }
}
