package com.nokia.ci.ejb.exception;

/**
 * JMS exception class that does not trigger transaction rollback.
 * Message is discarded if this exception is thrown.
 * 
 * @author vrouvine
 */
public class BackendJMSAppException extends BackendAppException {

    /**
     * Creates a new instance of
     * <code>BackendJMSAppException</code> without detail message.
     */
    public BackendJMSAppException() {
    }

    /**
     * Constructs an instance of
     * <code>BackendJMSAppException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public BackendJMSAppException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of
     * <code>BackendJMSAppException</code> with cause.
     *
     * @param cause
     */
    public BackendJMSAppException(Throwable cause) {
        super(cause);
    }
}
