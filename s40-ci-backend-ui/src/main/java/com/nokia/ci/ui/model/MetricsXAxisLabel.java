/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model;

import java.util.Date;

/**
 *
 * @author jajuutin
 *
 * This class is provided to be used as charts label object.
 *
 * Extends java.util.Date and relies on the fact that base class implements
 * valid equals and Comparable functionality.
 */
public class MetricsXAxisLabel extends Date {

    private String stringValue;

    public MetricsXAxisLabel(Date date, String stringValue) {
        super();

        setTime(date.getTime());
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
}