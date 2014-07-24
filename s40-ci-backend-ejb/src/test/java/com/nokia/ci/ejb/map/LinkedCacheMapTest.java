/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.map;

import java.util.Map.Entry;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author hhellgre
 */
public class LinkedCacheMapTest {
    
    @Test
    public void testCapacity() {
        final int size = 10;
        LinkedCacheMap<Integer, Integer> map = new LinkedCacheMap<Integer, Integer>(size);
        
        for(int i = 0; i < 20; i++) {
            map.put(i, i);
        }
        
        Assert.assertEquals(map.size(), size);
        
        int i = 0;
        for(Entry<Integer, Integer> entry : map.entrySet()) {
            Assert.assertEquals(entry.getValue().intValue(), size + i);
            i++;
        }
    }
}
