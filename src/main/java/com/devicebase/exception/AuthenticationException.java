package com.devicebase.exception;

/**
 * Exception thrown when authentication fails.
 *
 * @author Richie
 * @version 1.0.0
 */
public class AuthenticationException extends DeviceBaseException {

    /**
     * Creates a new AuthenticationException.
     *
     * @param message the error message
     */
    public AuthenticationException(String message) {
        super(message, 401);
    }

    /**
     * Creates a new AuthenticationException with cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, 401, cause);
    }
}