/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

/**
 *
 * @author jajuutin
 */
public enum Order {    
    ASC("asc", "asc"),
    DESC("desc", "desc");
    
    private final String sql;
    private final String stringValue;
    
    private Order(String sql, String stringValue) {
        this.sql = sql;
        this.stringValue = stringValue;
    }
    
    public String getSql() {
        return sql;
    }
        
    @Override
    public String toString() {
        return stringValue;
    }
    
    /**
     * Convert from "asc" and "desc" strings to order
     * type safe order data type.
     * This method is non-case sensitive.
     * 
     * @param orderString
     * @return 
     */
    public static Order fromString(String orderString) {
        if(orderString == null) {
            return null;
        }
        
        Order order = null;
        
        if (orderString.compareToIgnoreCase(ASC.toString()) == 0) {
            order = Order.ASC;
        }

        if (orderString.compareToIgnoreCase(DESC.toString()) == 0) {
            order = Order.DESC;
        }

        return order;
    }
}
