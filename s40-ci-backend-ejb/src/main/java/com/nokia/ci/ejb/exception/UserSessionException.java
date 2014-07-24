/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.exception;

import com.nokia.ci.ejb.model.SysUser;

/**
 *
 * @author hhellgre
 */
public class UserSessionException extends BackendAppException {

    /**
     * Creates a new instance of
     * <code>UserSessionException</code> without detail message.
     */
    public UserSessionException() {
    }

    /**
     * Constructs an instance of
     * <code>UserSessionException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UserSessionException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of
     * <code>UserSessionException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UserSessionException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of
     * <code>UserSessionException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UserSessionException(SysUser sysUser) {
        super(new StringBuilder().append("sysuser session exception for user: ").
                append(sysUser).toString());
    }
}
