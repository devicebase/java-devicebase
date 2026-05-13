package com.devicebase.exception;

/**
 * Exception thrown when a device is not found or not connected.
 *
 * @author Richie
 * @version 1.0.0
 */
public class DeviceNotFoundException extends DeviceBaseException {

    /**
     * Creates a new DeviceNotFoundException.
     *
     * @param message the error message
     */
    public DeviceNotFoundException(String message) {
        super(message, 404);
    }

    /**
     * Creates a new DeviceNotFoundException with cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public DeviceNotFoundException(String message, Throwable cause) {
        super(message, 404, cause);
    }
}