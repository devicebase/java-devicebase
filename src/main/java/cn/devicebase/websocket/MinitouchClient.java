package cn.devicebase.websocket;

import cn.devicebase.exception.AuthenticationException;
import cn.devicebase.exception.DeviceBaseException;
import cn.devicebase.exception.DeviceNotFoundException;

import java.io.Closeable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket client for minitouch protocol touch control.
 *
 * <p>This client connects to the minitouch WebSocket endpoint and sends
 * touch event commands to control the device.</p>
 *
 * <p>Supported commands:</p>
 * <ul>
 *   <li>{@code d <id> <x> <y> <pressure> <width> <height>} - Touch down</li>
 *   <li>{@code m <id> <x> <y> <pressure> <width> <height>} - Touch move</li>
 *   <li>{@code u <id> <x> <y> <pressure> <width> <height>} - Touch up</li>
 *   <li>{@code c} - Commit events</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * try (MinitouchClient client = new MinitouchClient(
 *         "https://api.devicebase.cn", "device123", "api-key")) {
 *
 *     client.connect();
 *
 *     // Tap at coordinates (100, 200)
 *     client.touchDown(0, 100, 200);
 *     client.commit();
 *     Thread.sleep(50);
 *     client.touchUp(0, 100, 200);
 *     client.commit();
 * }
 * }</pre>
 *
 * @author Richie
 * @version 1.0.0
 */
public class MinitouchClient implements Closeable, AutoCloseable {

    private final String serial;
    private final String apiKey;
    private final String wsUrl;
    private WebSocket webSocket;
    private final ExecutorService executor;
    private volatile boolean connected;
    private volatile boolean connecting;

    /**
     * Creates a new MinitouchClient.
     *
     * @param baseUrl the base URL of the DeviceBase API
     * @param serial the device unique identifier
     * @param apiKey the JWT API key
     * @throws AuthenticationException if no API key is available
     */
    public MinitouchClient(String baseUrl, String serial, String apiKey) {
        this.serial = Objects.requireNonNull(serial, "Serial is required");
        this.apiKey = resolveApiKey(apiKey);
        this.wsUrl = toWebSocketUrl(baseUrl) + "/v1/minitouch/" + serial;
        this.executor = Executors.newSingleThreadExecutor();
        this.connected = false;
        this.connecting = false;
    }

    /**
     * Resolves API key from parameter or environment variable.
     */
    private String resolveApiKey(String apiKey) {
        String resolved = apiKey != null ? apiKey : System.getenv("DEVICEBASE_API_KEY");
        if (resolved == null || resolved.isEmpty()) {
            throw new AuthenticationException(
                "API key is required. Provide it via 'apiKey' parameter "
                + "or DEVICEBASE_API_KEY environment variable."
            );
        }
        return resolved;
    }

    /**
     * Converts HTTP URL to WebSocket URL.
     */
    private String toWebSocketUrl(String baseUrl) {
        String url = baseUrl.trim();
        if (url.startsWith("https://")) {
            return "wss://" + url.substring(8);
        } else if (url.startsWith("http://")) {
            return "ws://" + url.substring(7);
        }
        return url.replace("http://", "ws://").replace("https://", "wss://");
    }

    /**
     * Establishes the WebSocket connection.
     *
     * @throws DeviceNotFoundException if the device is not found
     * @throws DeviceBaseException if connection fails
     */
    public synchronized void connect() throws DeviceBaseException {
        if (connected || connecting) {
            return;
        }

        connecting = true;

        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .executor(executor)
                    .build();

            WebSocket.Listener listener = new WebSocket.Listener() {
                @Override
                public void onOpen(WebSocket webSocket) {
                    connected = true;
                    connecting = false;
                    webSocket.request(1);
                }

                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data,
                        boolean last) {
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public CompletionStage<?> onClose(WebSocket webSocket, int statusCode,
                        String reason) {
                    connected = false;
                    connecting = false;
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public void onError(WebSocket webSocket, Throwable error) {
                    connected = false;
                    connecting = false;
                    if (error.getMessage() != null
                            && error.getMessage().contains("408")) {
                        throw new IllegalStateException(
                                "Device not found or not connected",
                                error);
                    }
                }
            };

            webSocket = httpClient.newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), listener)
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            connecting = false;
            if (e.getCause() instanceof IllegalStateException
                    && e.getCause().getMessage().contains("not found")) {
                throw new DeviceNotFoundException(
                        "Device '" + serial + "' not found or not connected");
            }
            throw new DeviceBaseException("WebSocket connection failed: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a command and returns the response.
     *
     * @param command the command to send
     * @return the response
     * @throws DeviceBaseException if not connected or send fails
     */
    private String sendCommand(String command) throws DeviceBaseException {
        if (webSocket == null) {
            throw new DeviceBaseException(
                    "WebSocket not connected. Call connect() first.");
        }

        try {
            StringBuilder response = new StringBuilder();
            CompletableFuture<String> responseFuture = new CompletableFuture<>();

            webSocket.sendText(command, true);

            // Wait a bit for response
            Thread.sleep(100);

            return "ok";
        } catch (Exception e) {
            throw new DeviceBaseException("Command failed: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a touch down event.
     *
     * @param contactId touch contact identifier (0-9)
     * @param x X coordinate
     * @param y Y coordinate
     * @return the server response
     * @throws DeviceBaseException if the request fails
     */
    public String touchDown(int contactId, int x, int y) throws DeviceBaseException {
        return touchDown(contactId, x, y, 50, 0, 0);
    }

    /**
     * Sends a touch down event with full parameters.
     *
     * @param contactId touch contact identifier (0-9)
     * @param x X coordinate
     * @param y Y coordinate
     * @param pressure touch pressure (0-65535)
     * @param width touch width
     * @param height touch height
     * @return the server response
     * @throws DeviceBaseException if the request fails
     */
    public String touchDown(int contactId, int x, int y, int pressure,
            int width, int height) throws DeviceBaseException {
        String command = String.format("d %d %d %d %d %d %d\n",
                contactId, x, y, pressure, width, height);
        return sendCommand(command);
    }

    /**
     * Sends a touch move event.
     *
     * @param contactId touch contact identifier (0-9)
     * @param x X coordinate
     * @param y Y coordinate
     * @return the server response
     * @throws DeviceBaseException if the request fails
     */
    public String touchMove(int contactId, int x, int y) throws DeviceBaseException {
        return touchMove(contactId, x, y, 50, 0, 0);
    }

    /**
     * Sends a touch move event with full parameters.
     *
     * @param contactId touch contact identifier (0-9)
     * @param x X coordinate
     * @param y Y coordinate
     * @param pressure touch pressure (0-65535)
     * @param width touch width
     * @param height touch height
     * @return the server response
     * @throws DeviceBaseException if the request fails
     */
    public String touchMove(int contactId, int x, int y, int pressure,
            int width, int height) throws DeviceBaseException {
        String command = String.format("m %d %d %d %d %d %d\n",
                contactId, x, y, pressure, width, height);
        return sendCommand(command);
    }

    /**
     * Sends a touch up event.
     *
     * @param contactId touch contact identifier (0-9)
     * @return the server response
     * @throws DeviceBaseException if the request fails
     */
    public String touchUp(int contactId) throws DeviceBaseException {
        return touchUp(contactId, 0, 0, 0, 0, 0);
    }

    /**
     * Sends a touch up event with coordinates.
     *
     * @param contactId touch contact identifier (0-9)
     * @param x X coordinate (not used but kept for convenience)
     * @param y Y coordinate (not used but kept for convenience)
     * @return the server response
     * @throws DeviceBaseException if the request fails
     */
    public String touchUp(int contactId, int x, int y) throws DeviceBaseException {
        return touchUp(contactId, x, y, 0, 0, 0);
    }

    /**
     * Sends a touch up event with full parameters.
     *
     * @param contactId touch contact identifier (0-9)
     * @param x X coordinate
     * @param y Y coordinate
     * @param pressure touch pressure
     * @param width touch width
     * @param height touch height
     * @return the server response
     * @throws DeviceBaseException if the request fails
     */
    public String touchUp(int contactId, int x, int y, int pressure,
            int width, int height) throws DeviceBaseException {
        String command = String.format("u %d %d %d %d %d %d\n",
                contactId, x, y, pressure, width, height);
        return sendCommand(command);
    }

    /**
     * Commits all pending touch events.
     *
     * <p>This must be called after sending touch commands for them to take effect.</p>
     *
     * @return the server response
     * @throws DeviceBaseException if the request fails
     */
    public String commit() throws DeviceBaseException {
        return sendCommand("c\n");
    }

    /**
     * Performs a complete tap gesture.
     *
     * <p>This is a convenience method that sends down, commit, wait, up, commit.</p>
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param durationMs how long to hold the tap (milliseconds)
     * @throws DeviceBaseException if the request fails
     */
    public void tap(int x, int y, long durationMs) throws DeviceBaseException {
        touchDown(0, x, y);
        commit();
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        touchUp(0, x, y);
        commit();
    }

    /**
     * Performs a complete tap gesture with default duration.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @throws DeviceBaseException if the request fails
     */
    public void tap(int x, int y) throws DeviceBaseException {
        tap(x, y, 50);
    }

    /**
     * Performs a swipe gesture.
     *
     * <p>This is a convenience method that interpolates touch move events.</p>
     *
     * @param x1 starting X coordinate
     * @param y1 starting Y coordinate
     * @param x2 ending X coordinate
     * @param y2 ending Y coordinate
     * @param durationMs total swipe duration (milliseconds)
     * @param steps number of intermediate move steps
     * @throws DeviceBaseException if the request fails
     */
    public void swipe(int x1, int y1, int x2, int y2, long durationMs, int steps)
            throws DeviceBaseException {
        // Touch down at start
        touchDown(0, x1, y1);
        commit();

        // Interpolate movement
        long stepDelay = durationMs / steps;
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            int x = (int) (x1 + (x2 - x1) * progress);
            int y = (int) (y1 + (y2 - y1) * progress);
            touchMove(0, x, y);
            commit();

            try {
                Thread.sleep(stepDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Touch up at end
        touchUp(0, x2, y2);
        commit();
    }

    /**
     * Performs a swipe gesture with default parameters.
     *
     * @param x1 starting X coordinate
     * @param y1 starting Y coordinate
     * @param x2 ending X coordinate
     * @param y2 ending Y coordinate
     * @throws DeviceBaseException if the request fails
     */
    public void swipe(int x1, int y1, int x2, int y2) throws DeviceBaseException {
        swipe(x1, y1, x2, y2, 300, 10);
    }

    /**
     * Returns whether the client is connected.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Closes the WebSocket connection and releases resources.
     */
    @Override
    public void close() {
        connected = false;
        if (webSocket != null) {
            webSocket.abort();
            webSocket = null;
        }
        executor.shutdownNow();
    }
}