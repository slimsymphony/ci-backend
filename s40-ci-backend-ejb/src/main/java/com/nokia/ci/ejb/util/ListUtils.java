/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jajuutin
 */
public final class ListUtils {
    
    public static <T> List<List<T>> partition(List<T> sourceList, int size) {
        // create copy of source to avoid modifying existing.
        List<T> sourceListTmp = new ArrayList<T>(sourceList);
        List<List<T>> lists = new ArrayList<List<T>>();
        
        while(!sourceListTmp.isEmpty()) {            
            int index = size;
            if(index > sourceListTmp.size()) {
                index = sourceListTmp.size();
            }
            
            if(index <= 0) {
                // Should not be here. For safety.
                break;
            }
            
            // retrieve sub view to master list.
            List<T> subList = sourceListTmp.subList(0, index);
            
            // create independent patch list
            List<T> patch = new ArrayList<T>();
            patch.addAll(subList);
            lists.add(patch);
            
            // remove recently added entries. this operation propagates to
            // original master list.
            subList.clear();
        }
        
        return lists;
    }   
}
