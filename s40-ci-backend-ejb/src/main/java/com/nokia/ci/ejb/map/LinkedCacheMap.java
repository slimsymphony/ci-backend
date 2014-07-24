/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.map;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author hhellgre
 */
public class LinkedCacheMap<K,V> extends LinkedHashMap<K,V> {
    
    private int maxCapacity;

    public LinkedCacheMap(int maxCapacity) {
        super(maxCapacity);
        this.maxCapacity = maxCapacity;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxCapacity;
    }
}
