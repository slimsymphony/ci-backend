/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.tas;

/**
 *
 * @author jajuutin
 */
public class TasDeviceReadException extends Exception {

    /**
     * Creates a new instance of
     * <code>TasDeviceReadException</code> without detail message.
     */
    public TasDeviceReadException() {
    }

    /**
     * Constructs an instance of
     * <code>TasDeviceReadException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public TasDeviceReadException(String msg) {
        super(msg);
    }
}
