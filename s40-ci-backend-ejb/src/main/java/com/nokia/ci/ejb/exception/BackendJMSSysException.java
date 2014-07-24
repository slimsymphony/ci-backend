package com.nokia.ci.ejb.exception;

/**
 * JMS exception class that triggers transaction rollback. Message is returned
 * back to queue if this exception is thrown.
 *
 * @author miikka
 */
public class BackendJMSSysException extends BackendSysException {

    /**
     * Creates a new instance of
     * <code>BackendJMSSysException</code> without detail message.
     */
    public BackendJMSSysException() {
    }

    /**
     * Constructs an instance of
     * <code>BackendJMSSysException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public BackendJMSSysException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of
     * <code>BackendJMSSysException</code> with cause.
     *
     * @param cause
     */
    public BackendJMSSysException(Throwable cause) {
        super(cause);
    }

    /**
     * Construct an instance of
     * <code>BackendJMSSysException</code> with message and cause.
     *
     * @param msg Detailed message.
     * @param cause Nested exception.
     */
    public BackendJMSSysException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
