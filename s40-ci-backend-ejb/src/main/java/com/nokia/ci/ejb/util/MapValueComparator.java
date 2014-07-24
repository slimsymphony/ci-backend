/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nokia.ci.ejb.util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nokia.ci.ejb.model.SlaveInstance;

/**
 * MapValueComparator
 * 
 * @author aklappal
 */
public class MapValueComparator<K, V> implements Comparator<K> {

    private static Logger log = LoggerFactory.getLogger(MapValueComparator.class);
    private Map<K, V> map;

    public MapValueComparator() {

    }

    @Override
    public int compare(K o1, K o2) {
        if (map.get(o1) == null) {
            log.warn(o1 + " value was null!");
            return 1;
        }
        if (map.get(o2) == null) {
            log.warn(o2 + " value was null!");
            return -1;
        }
        if (map.get(o1) instanceof List) {
            int result = ( ((List) map.get(o1)).size() - ((List)(map.get(o2))).size() );
            log.debug("compared: " + o1 + " and " + o2 + " with result:" + result);
            if(result == 0) {
                return 1;
            }
            return result;
        } else if (map.get(o1)instanceof Comparable) {
            int result = ((Comparable) map.get(o1)).compareTo((map.get(o2)));
            log.debug("compared: " + o1 + " and " + o2 + " with result:" + result);
            if(result == 0) {
                return 1;
            }
            return result;
        } else {
            log.warn(o1 + " value was not comparable!");
            return 1;
        }
    }

    public void setMap(Map<K, V> map) {
        this.map = map;
    }
    
    
}