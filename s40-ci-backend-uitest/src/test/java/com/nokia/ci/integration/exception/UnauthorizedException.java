/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.integration.exception;

/**
 *
 * @author suoyrjo
 */
public class UnauthorizedException extends Exception {
        /**
     * Creates a new instance of
     * <code>UnauthorizedException</code> without detail message.
     */
    public UnauthorizedException() {
    }

    /**
     * Constructs an instance of
     * <code>UnauthorizedException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnauthorizedException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of
     * <code>UnauthorizedException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnauthorizedException(Throwable cause) {
        super(cause);
    }
}
