/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.exception;

/**
 *
 * @author miikka
 * 
 * This exception is thrown when authentication fails.
 */
public class AuthException extends BackendAppException {

    /**
     * Creates a new instance of
     * <code>AuthException</code> without detail message.
     */
    public AuthException() {
    }

    /**
     * Constructs an instance of
     * <code>AuthException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public AuthException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of
     * <code>AuthException</code> with the specified detail message.
     *
     * @param cause the cause.
     */
    public AuthException(Throwable cause) {
        super(cause);
    }    
}
