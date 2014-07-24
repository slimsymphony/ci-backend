/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
public class OrderTest {
    static final String ASC_SQL_VALUE = "asc";
    static final String ASC_STRING_VALUE ="asc";
    static final String DESC_SQL_VALUE = "desc";
    static final String DESC_STRING_VALUE ="desc";
    
    @Test
    public void ascBasic() {
        Order order = Order.ASC;
        assertOrder(order, ASC_SQL_VALUE, ASC_STRING_VALUE);        
    }
    
    @Test
    public void descBasic() {
        Order order = Order.DESC;
        assertOrder(order, DESC_SQL_VALUE, DESC_STRING_VALUE);
    }

    @Test
    public void ascFromString() {
        Order order = Order.fromString(ASC_STRING_VALUE);
        Assert.assertNotNull(order);
        assertOrder(order, ASC_SQL_VALUE, ASC_STRING_VALUE);        
    }    

    @Test
    public void descFromString() {
        Order order = Order.fromString(DESC_STRING_VALUE);
        Assert.assertNotNull(order);
        assertOrder(order, DESC_SQL_VALUE, DESC_STRING_VALUE);
    }

    @Test
    public void nullStringTest() {
        Order order = Order.fromString(null);
        Assert.assertNull(order);
    }    
    
    private static void assertOrder(Order order, 
            String sql, String stringValue) {
        Assert.assertTrue(order.getSql().compareTo(sql) == 0);
        Assert.assertTrue(order.toString().compareTo(stringValue) == 0);        
    }
}
