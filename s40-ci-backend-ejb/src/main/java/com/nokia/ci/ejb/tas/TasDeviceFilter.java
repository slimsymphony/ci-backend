/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.tas;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jajuutin
 */
public class TasDeviceFilter {

    /**
     * Filter tas devices from source list with rm codes and free search string.
     * Case insensitive search.
     *
     * @param devices
     * @param rmCode
     * @param contains
     * @return
     */
    public static List<TasDevice> filter(List<TasDevice> devices, String rmCode, List<String> searchKeys) {
        
        if (devices == null || devices.isEmpty() || rmCode.isEmpty()) {
            return new ArrayList<TasDevice>();
        }

        List<TasDevice> filtered = new ArrayList<TasDevice>();

        // convert search keys to lowercase.
        List<String> searchKeysLowercase = new ArrayList();
        for (String searchKey : searchKeys) {
            searchKeysLowercase.add(searchKey.toLowerCase());
        }

        // filter devices by rm code and searchkeys.
        for (TasDevice device : devices) {
            if (device == null) {
                continue;
            }

            if (device.getRmCode() == null) {
                continue;
            }
            
            if (!rmCode.equalsIgnoreCase(device.getRmCode())) {
                continue;
            }

            // convert content to lower case.
            String content = device.getContent().toLowerCase();
            if (StringUtils.isEmpty(content)) {
                continue;
            }

            if (!find(content, searchKeysLowercase)) {
                continue;
            }

            // passed checks. add to list.
            filtered.add(device);
        }

        return filtered;
    }

    /**
     * search source string for keys. true returned if all keys are found from
     * source.
     *
     * @param source
     * @param keys
     * @return
     */
    private static boolean find(String source, List<String> keys) {
        for (String key : keys) {
            if (!source.contains(key)) {
                return false;
            }
        }

        return true;
    }
}
