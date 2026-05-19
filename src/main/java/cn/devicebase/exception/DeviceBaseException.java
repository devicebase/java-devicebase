package cn.devicebase.exception;

/**
 * Base exception for DeviceBase SDK errors.
 *
 * @author Richie
 * @version 1.0.0
 */
public class DeviceBaseException extends RuntimeException {

    private final String message;
    private final Integer statusCode;

    /**
     * Creates a new DeviceBaseException.
     *
     * @param message the error message
     */
    public DeviceBaseException(String message) {
        super(message);
        this.message = message;
        this.statusCode = null;
    }

    /**
     * Creates a new DeviceBaseException with status code.
     *
     * @param message the error message
     * @param statusCode the HTTP status code, if applicable
     */
    public DeviceBaseException(String message, Integer statusCode) {
        super(message);
        this.message = message;
        this.statusCode = statusCode;
    }

    /**
     * Creates a new DeviceBaseException with cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public DeviceBaseException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.statusCode = null;
    }

    /**
     * Creates a new DeviceBaseException with status code and cause.
     *
     * @param message the error message
     * @param statusCode the HTTP status code
     * @param cause the cause of this exception
     */
    public DeviceBaseException(String message, Integer statusCode, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.statusCode = statusCode;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Returns the HTTP status code, if applicable.
     *
     * @return the status code, or null if not applicable
     */
    public Integer getStatusCode() {
        return statusCode;
    }
}