package com.nokia.ci.ejb.exception;

/**
 * Exception class for invalid application phases.
 * 
 * @author vrouvine
 */
public class InvalidPhaseException extends BackendAppException {

    public InvalidPhaseException() {
    }

    public InvalidPhaseException(String msg) {
        super(msg);
    }

    public InvalidPhaseException(Throwable cause) {
        super(cause);
    }

}
