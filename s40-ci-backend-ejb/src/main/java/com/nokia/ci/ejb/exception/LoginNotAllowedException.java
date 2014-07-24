/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.exception;

/**
 *
 * @author jajuutin
 */
public class LoginNotAllowedException extends BackendAppException {

    /**
     * Creates a new instance of {@code NotFoundException} without detail message.
     */
    public LoginNotAllowedException() {
    }

    /**
     * Constructs an instance of {@code BackendAppException} with the specified detail message.
     * @param msg the detail message.
     */
    public LoginNotAllowedException(String msg) {
        super(msg);
    }   

    public LoginNotAllowedException(Throwable cause) {
        super(cause);
    }
}
