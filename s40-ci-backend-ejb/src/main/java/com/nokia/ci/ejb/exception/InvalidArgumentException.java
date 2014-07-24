/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.exception;

/**
 *
 * @author jajuutin
 */
public class InvalidArgumentException extends BackendAppException {

    /**
     * Creates a new instance of <code>InvalidArgumentException</code> without detail message.
     */
    public InvalidArgumentException() {
    }

    /**
     * Constructs an instance of <code>InvalidArgumentException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidArgumentException(String msg) {
        super(msg);
    }
}
