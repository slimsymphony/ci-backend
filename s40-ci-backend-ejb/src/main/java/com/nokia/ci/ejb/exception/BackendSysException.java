/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.exception;

/**
 *
 * @author jajuutin
 * 
 * Inherit from this class when implementing a RUNTIME exception to backend.
 * Runtime based exceptions are to be used when a really serious situation
 * is detected and system CAN NOT recover from it.
 * 
 * Runtime based exceptions are also called system exceptions.
 * 
 * Throwing system exception from EJB layer will cause the exception to be
 * wrapper in RemoteException(wrapping is done by application server). This
 * wrapper is then thrown to caller.
 * 
 * When system exception is detected related session beans are also discarded
 * and current transaction is automatically rolled back.
 * 
 * System exceptions are unchecked exceptions and therefore do not require
 * "try", "catch" or "throws".
 * 
 * Rule: when your system can not recover from error, then throw 
 * implementation of BackendSysException. Otherwise use 
 * implementations of BackendAppException.
 */
public abstract class BackendSysException extends RuntimeException {

    /**
     * Creates a new instance of <code>BackendSysException</code> without detail message.
     */
    public BackendSysException() {
    }

    /**
     * Constructs an instance of <code>BackendSysException</code> with the specified detail message.
     * @param msg Detailed message.
     */
    public BackendSysException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>BackendSysException</code> with the specified cause.
     * @param cause Nested exception.
     */
    public BackendSysException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructs an instance of <code>BackendSysException</code> with the specified detail message and cause.
     * @param msg Detailed message.
     * @param cause Nested exception.
     */
    public BackendSysException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
