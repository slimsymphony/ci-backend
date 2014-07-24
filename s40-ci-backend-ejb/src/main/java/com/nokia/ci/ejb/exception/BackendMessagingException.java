package com.nokia.ci.ejb.exception;

/**
 * Exception class for messaging exceptions.
 * 
 * @author vrouvine
 */
public class BackendMessagingException extends BackendAppException {
    
    public BackendMessagingException(String msg) {
        super(msg);
    }

    public BackendMessagingException(Throwable cause) {
        super(cause);
    }
}
