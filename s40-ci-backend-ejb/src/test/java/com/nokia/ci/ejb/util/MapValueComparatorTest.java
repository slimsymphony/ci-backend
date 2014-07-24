package com.nokia.ci.ejb.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.nokia.ci.ejb.model.SlaveInstance;

public class MapValueComparatorTest {

    @Test
    public void testCompareList() {
        Map<String, List<String>> stringMap = new HashMap<String,List<String>>();
        stringMap.put("1", Arrays.asList("a","b","c","d"));
        stringMap.put("2", Arrays.asList("c","d"));
        stringMap.put("3", Arrays.asList("a","b","c","d","e","f"));
        stringMap.put("4", Arrays.asList("a"));
        stringMap.put("5", Arrays.asList("c","d"));
        stringMap.put("6", Arrays.asList("a","b","c","d","e"));
        MapValueComparator<String, List<String>> mapComparator = new MapValueComparator<String, List<String>>();
        mapComparator.setMap(stringMap);
        TreeMap<String, List<String>> sortedStringTreeMap = new TreeMap<String, List<String>>(mapComparator);
        sortedStringTreeMap.putAll(stringMap);
        Assert.assertEquals(sortedStringTreeMap.keySet().toString(), "[4, 2, 5, 1, 6, 3]");
    }

    @Test
    public void testCompareComparable() {
        Map<String, String> stringMap = new HashMap<String, String>();
        stringMap.put("1", "abcd");
        stringMap.put("2", "cdtat");
        stringMap.put("3", "abeagb");
        stringMap.put("4", "abe");
        stringMap.put("5", "ca");
        stringMap.put("6", "abrs");
        MapValueComparator<String, String> mapComparator = new MapValueComparator<String, String>();
        mapComparator.setMap(stringMap);
        TreeMap<String, String> sortedStringTreeMap = new TreeMap<String, String>(mapComparator);
        sortedStringTreeMap.putAll(stringMap);
        Assert.assertEquals(sortedStringTreeMap.keySet().toString(), "[1, 4, 3, 6, 5, 2]");
    }

    @Test
    public void testCompareNonComparable() {
        Map<String, Object> stringMap = new HashMap<String, Object>();
        stringMap.put("1", new Object());
        stringMap.put("2", new Object());
        stringMap.put("3", new Object());
        stringMap.put("4", new Object());
        stringMap.put("5", new Object());
        stringMap.put("6", new Object());
        MapValueComparator<String, Object> mapComparator = new MapValueComparator<String, Object>();
        mapComparator.setMap(stringMap);
        TreeMap<String, Object> sortedStringTreeMap = new TreeMap<String, Object>(mapComparator);
        sortedStringTreeMap.putAll(stringMap);
        Assert.assertEquals(sortedStringTreeMap.keySet().toString(), "[3, 2, 1, 6, 5, 4]");
    }

    @Test
    public void testCompareNull() {
        Map<String, Object> stringMap = new HashMap<String, Object>();
        stringMap.put("1", null);
        stringMap.put("2", null);
        stringMap.put("3", null);
        stringMap.put("4", null);
        stringMap.put("5", null);
        stringMap.put("6", null);
        MapValueComparator<String, Object> mapComparator = new MapValueComparator<String, Object>();
        mapComparator.setMap(stringMap);
        TreeMap<String, Object> sortedStringTreeMap = new TreeMap<String, Object>(mapComparator);
        sortedStringTreeMap.putAll(stringMap);
        Assert.assertEquals(sortedStringTreeMap.keySet().toString(), "[3, 2, 1, 6, 5, 4]");
    }    
}
