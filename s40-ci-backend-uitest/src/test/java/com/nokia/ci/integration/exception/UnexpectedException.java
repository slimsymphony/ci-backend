/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.integration.exception;

/**
 *
 * @author suoyrjo
 */
public class UnexpectedException extends Exception {
        /**
     * Creates a new instance of
     * <code>UnexpectedException</code> without detail message.
     */
    public UnexpectedException() {
    }

    /**
     * Constructs an instance of
     * <code>UnexpectedException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnexpectedException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of
     * <code>UnexpectedException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnexpectedException(Throwable cause) {
        super(cause);
    }
}
