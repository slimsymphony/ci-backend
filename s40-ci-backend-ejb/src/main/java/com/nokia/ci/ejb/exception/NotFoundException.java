package com.nokia.ci.ejb.exception;

/**
 * Requested entity not found.
 * @author vrouvine
 */
public class NotFoundException extends BackendAppException {

    /**
     * Creates a new instance of {@code NotFoundException} without detail message.
     */
    public NotFoundException() {
    }

    /**
     * Constructs an instance of {@code NotFoundException} with the specified detail message.
     * @param msg the detail message.
     */
    public NotFoundException(String msg) {
        super(msg);
    }   

    public NotFoundException(Throwable cause) {
        super(cause);
    }
    
    public NotFoundException(Long id, Class type) {
        super(type.getCanonicalName() + " not found with id: " + id.toString());
    }
    
    public NotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
