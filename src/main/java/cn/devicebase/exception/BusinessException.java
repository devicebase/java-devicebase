package cn.devicebase.exception;

/**
 * Thrown when the API answers with a success status but reports a failure
 * inside the response envelope.
 *
 * <p>The control API returns HTTP 200 with a non-2xx {@code code} for action
 * failures — a browser selector that matches nothing, a computer command that
 * cannot run:</p>
 *
 * <pre>{@code
 * {"code":502,"message":"-32602: Invalid parameters"}
 * }</pre>
 *
 * <p>Guarding on the HTTP status alone would report those as success, so the
 * envelope is inspected too. {@link #getCode()} carries the envelope's code and
 * {@link #getBody()} the raw response body, which holds the server's own
 * message. There is no HTTP status for this layer — the transport succeeded.</p>
 *
 * @author Richie
 */
public class BusinessException extends DeviceBaseException {

    private static final long serialVersionUID = 1L;

    private final int code;
    private final String body;

    /**
     * Creates a new BusinessException.
     *
     * @param code the envelope's non-2xx code
     * @param body the raw response body
     */
    public BusinessException(int code, String body) {
        super("API error (code " + code + "): " + body);
        this.code = code;
        this.body = body;
    }

    /**
     * Returns the envelope's code, e.g. 502.
     *
     * @return the envelope code
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the raw response body, which carries the server's own message.
     *
     * @return the response body
     */
    public String getBody() {
        return body;
    }
}
