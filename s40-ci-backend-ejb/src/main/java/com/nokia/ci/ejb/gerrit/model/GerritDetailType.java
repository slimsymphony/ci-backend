/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.gerrit.model;

/**
 *
 * @author jajuutin
 */
public enum GerritDetailType {

    NEW("NEW"), DRAFT("DRAFT"), SUBMITTED("SUBMITTED"), MERGED("MERGED"), ABANDONED("ABANDONED");
    private String statusString;

    private GerritDetailType(String statusString) {
        this.statusString = statusString;
    }

    public static GerritDetailType fromStatus(String status) {
        GerritDetailType result = null;
        
        for(GerritDetailType type : values()) {
            if(type.statusString.equalsIgnoreCase(status)) {
                result = type;
                break;
            }
        }
        
        return result;
    }    
}
