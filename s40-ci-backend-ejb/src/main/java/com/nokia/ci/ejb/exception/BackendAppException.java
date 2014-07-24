/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.exception;

/**
 *
 * @author jajuutin
 *
 * BackendAppExceptions are application exceptions. Application exception is
 * thrown when error from which system CAN recover is detected.
 *
 * Rule: when your system CAN NOT recover from error, then throw implementation
 * of BackendSySException. Otherwise use implementations of BackendAppException.
 *
 * Throwing BackendAppException does not automatically roll back current
 * transaction and it must be done manually if necessary.
 *
 * Application exceptions are checked exceptions.
 */
public abstract class BackendAppException extends Exception {

    /**
     * Creates a new instance of
     * <code>BackendAppException</code> without detail message.
     */
    public BackendAppException() {
    }

    /**
     * Constructs an instance of
     * <code>BackendAppException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public BackendAppException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of
     * <code>BackendAppException</code> with cause.
     *
     * @param cause
     */
    public BackendAppException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of
     * <code>BackendAppException</code> with message and cause.
     *
     * @param message
     * @param cause
     */
    public BackendAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
