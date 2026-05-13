package com.devicebase.websocket;

import com.devicebase.exception.AuthenticationException;
import com.devicebase.exception.DeviceBaseException;
import com.devicebase.exception.DeviceNotFoundException;

import java.io.Closeable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * WebSocket client for minicap protocol screen streaming.
 *
 * <p>This client connects to the minicap WebSocket endpoint and receives
 * real-time JPEG frames from the device screen.</p>
 *
 * <p>Protocol format: Banner(24 bytes) + FrameData(4 bytes size + JPEG data)</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * try (MinicapClient client = new MinicapClient(
 *         "https://api.devicebase.cn", "device123", "api-key")) {
 *
 *     client.subscribe(frame -> {
 *         // Process JPEG frame
 *         System.out.println("Received frame: " + frame.length + " bytes");
 *     });
 *
 *     // Keep the connection alive
 *     Thread.sleep(60000);
 * }
 * }</pre>
 *
 * @author Richie
 * @version 1.0.0
 */
public class MinicapClient implements Closeable, AutoCloseable {

    /** Banner size in bytes. */
    public static final int BANNER_SIZE = 24;

    /** Frame header size in bytes. */
    public static final int FRAME_HEADER_SIZE = 4;

    private final String serial;
    private final String apiKey;
    private final String wsUrl;
    private WebSocket webSocket;
    private final ExecutorService executor;
    private volatile boolean connected;
    private volatile boolean headerParsed;

    // Frame parsing state
    private ByteBuffer frameSizeBuffer;
    private int expectedFrameSize;
    private ByteBuffer frameDataBuffer;

    // Subscriber for frames
    private Flow.Subscriber<? super byte[]> subscriber;

    /**
     * Creates a new MinicapClient.
     *
     * @param baseUrl the base URL of the DeviceBase API
     * @param serial the device unique identifier
     * @param apiKey the JWT API key
     * @throws AuthenticationException if no API key is available
     */
    public MinicapClient(String baseUrl, String serial, String apiKey) {
        this.serial = Objects.requireNonNull(serial, "Serial is required");
        this.apiKey = resolveApiKey(apiKey);
        this.wsUrl = toWebSocketUrl(baseUrl) + "/v1/minicap/" + serial;
        this.executor = Executors.newSingleThreadExecutor();
        this.connected = false;
        this.headerParsed = false;
        this.frameSizeBuffer = ByteBuffer.allocate(FRAME_HEADER_SIZE);
        this.expectedFrameSize = -1;
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
     * Connects to the WebSocket server.
     *
     * @throws DeviceNotFoundException if the device is not found
     * @throws DeviceBaseException if connection fails
     */
    public void connect() {
        if (connected) {
            return;
        }

        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .executor(executor)
                    .build();

            webSocket = httpClient.connectWebSocket(
                    URI.create(wsUrl),
                    new WebSocket.Listener() {
                        @Override
                        public void onOpen(WebSocket webSocket) {
                            connected = true;
                            webSocket.request(1);
                        }

                        @Override
                        public void onBinary(WebSocket webSocket, boolean last,
                                ByteBuffer data) {
                            handleBinaryData(data);
                            webSocket.request(1);
                        }

                        @Override
                        public void onClose(WebSocket webSocket, int statusCode,
                                String reason) {
                            connected = false;
                        }

                        @Override
                        public void onError(WebSocket webSocket, Throwable error) {
                            connected = false;
                            if (error.getMessage() != null
                                    && error.getMessage().contains("408")) {
                                throw new IllegalStateException(
                                        "Device not found or not connected",
                                        error);
                            }
                        }
                    }
            );
        } catch (Exception e) {
            throw new DeviceBaseException("WebSocket connection failed: " + e.getMessage(), e);
        }
    }

    /**
     * Handles incoming binary data.
     */
    private synchronized void handleBinaryData(ByteBuffer data) {
        if (subscriber == null) {
            return;
        }

        while (data.hasRemaining()) {
            if (!headerParsed) {
                // Skip banner
                if (data.remaining() >= BANNER_SIZE) {
                    // Skip banner bytes
                    byte[] banner = new byte[BANNER_SIZE];
                    data.get(banner);
                    headerParsed = true;
                    frameSizeBuffer.clear();
                } else {
                    // Store partial banner
                    byte[] temp = new byte[data.remaining()];
                    data.get(temp);
                }
            } else {
                // Parse frame
                if (expectedFrameSize < 0) {
                    // Reading frame size
                    while (data.hasRemaining() && frameSizeBuffer.hasRemaining()) {
                        frameSizeBuffer.put(data.get());
                    }

                    if (!frameSizeBuffer.hasRemaining()) {
                        frameSizeBuffer.flip();
                        expectedFrameSize = frameSizeBuffer.getInt();
                        frameDataBuffer = ByteBuffer.allocate(expectedFrameSize);
                        frameSizeBuffer.clear();
                    }
                } else {
                    // Reading frame data
                    byte[] chunk = new byte[Math.min(data.remaining(),
                            frameDataBuffer.remaining())];
                    data.get(chunk);
                    frameDataBuffer.put(chunk);

                    if (!frameDataBuffer.hasRemaining()) {
                        // Frame complete
                        frameDataBuffer.flip();
                        byte[] frame = new byte[expectedFrameSize];
                        frameDataBuffer.get(frame);
                        subscriber.onNext(frame);

                        // Reset for next frame
                        expectedFrameSize = -1;
                        frameDataBuffer = null;
                    }
                }
            }
        }
    }

    /**
     * Subscribes to receive JPEG frames.
     *
     * @param subscriber the subscriber to receive frames
     */
    public void subscribe(Flow.Subscriber<? super byte[]> subscriber) {
        this.subscriber = subscriber;
        connect();

        CompletableFuture.runAsync(() -> {
            try {
                // Send initial request
                if (webSocket != null) {
                    webSocket.sendText("", true);
                }

                // Keep connection alive
                while (connected) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, executor);
    }

    /**
     * Captures a single frame.
     *
     * @return JPEG image bytes
     * @throws DeviceBaseException if no frame is received
     */
    public byte[] captureFrame() throws DeviceBaseException {
        connect();

        List<byte[]> frames = new ArrayList<>();
        Flow.Subscriber<? super byte[]> singleFrameSubscriber = new Flow.Subscriber<>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(byte[] item) {
                frames.add(item);
                subscription.cancel();
            }

            @Override
            public void onError(Throwable error) {
                // Ignore
            }

            @Override
            public void onComplete() {
                // Ignore
            }
        };

        subscribe(singleFrameSubscriber);

        // Wait for frame
        long startTime = System.currentTimeMillis();
        while (frames.isEmpty() && System.currentTimeMillis() - startTime < 5000) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (frames.isEmpty()) {
            throw new DeviceBaseException("No frame received from stream");
        }

        return frames.get(0);
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