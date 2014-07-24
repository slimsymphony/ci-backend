package com.nokia.ci.client.exception;

import com.nokia.ci.client.model.AbstractView;

/**
 * Exception class for {@link AbstractView} transformation exceptions.
 * @author vrouvine
 */
public class ViewTransformException extends RuntimeException {

    /**
     * Creates a new instance of {@code ViewTransformException} without detail message.
     */
    public ViewTransformException() {
    }

    /**
     * Constructs an instance of {@code ViewTransformException} with the specified detail message.
     * @param msg the detail message.
     */
    public ViewTransformException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of {@code ViewTransformException} with the specified detail message and {@code Throwable}.
     * @param msg the detail message.
     */
    public ViewTransformException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
    
}
