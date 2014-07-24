/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.exception;

/**
 *
 * @author jajuutin
 */
public class JobStartException extends BackendAppException {

    /**
     * Creates a new instance of <code>JobStartException</code> without detail message.
     */
    public JobStartException() {
    }

    /**
     * Constructs an instance of <code>JobStartException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public JobStartException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>JobStartException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public JobStartException(Throwable cause) {
        super(cause);
    }
}
