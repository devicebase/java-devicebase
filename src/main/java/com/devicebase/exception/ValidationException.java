package com.devicebase.exception;

/**
 * Exception thrown when request validation fails.
 *
 * @author Richie
 * @version 1.0.0
 */
public class ValidationException extends DeviceBaseException {

    /**
     * Creates a new ValidationException.
     *
     * @param message the error message
     */
    public ValidationException(String message) {
        super(message, 422);
    }

    /**
     * Creates a new ValidationException with cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, 422, cause);
    }
}