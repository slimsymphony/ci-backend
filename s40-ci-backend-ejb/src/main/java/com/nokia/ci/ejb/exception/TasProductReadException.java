package com.nokia.ci.ejb.exception;

/**
 * Tas product read failed.
 * @author jajuutin
 */
public class TasProductReadException extends BackendAppException {

    /**
     * Creates a new instance of {@code TasProductReadException} without detail message.
     */
    public TasProductReadException() {
    }

    /**
     * Constructs an instance of {@code TasProductReadException} with the specified detail message.
     * @param msg the detail message.
     */
    public TasProductReadException(String msg) {
        super(msg);
    }   

    public TasProductReadException(Throwable cause) {
        super(cause);
    }
}
