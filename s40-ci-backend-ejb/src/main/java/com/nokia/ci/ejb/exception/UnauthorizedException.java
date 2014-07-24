/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.exception;

import com.nokia.ci.ejb.model.SysUser;

/**
 *
 * @author jajuutin
 */
public class UnauthorizedException extends BackendAppException {

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

    /**
     * Constructs an instance of
     * <code>UnauthorizedException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnauthorizedException(SysUser sysUser) {
        super(new StringBuilder().append("authorization failed for sysuser: ").
                append(sysUser).toString());
    }
}
