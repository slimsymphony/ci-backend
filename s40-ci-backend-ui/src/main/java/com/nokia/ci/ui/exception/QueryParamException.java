/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.exception;

/**
 *
 * @author jajuutin
 */
public class QueryParamException extends Exception {

    /**
     * Creates a new instance of
     * <code>QueryParamException</code> without detail message.
     */
    public QueryParamException() {
    }

    /**
     * Constructs an instance of
     * <code>QueryParamException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public QueryParamException(String msg) {
        super(msg);
    }
}
