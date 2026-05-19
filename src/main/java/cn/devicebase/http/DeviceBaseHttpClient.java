package cn.devicebase.http;

import cn.devicebase.exception.AuthenticationException;
import cn.devicebase.exception.DeviceBaseException;
import cn.devicebase.exception.DeviceNotFoundException;
import cn.devicebase.exception.ValidationException;
import cn.devicebase.model.AppInfo;
import cn.devicebase.model.Bounds;
import cn.devicebase.model.DeviceInfo;
import cn.devicebase.model.DeviceResponse;
import cn.devicebase.model.DeviceStatsData;
import cn.devicebase.model.GetActiveSessionResponse;
import cn.devicebase.model.HeartbeatResponse;
import cn.devicebase.model.HierarchyInfo;
import cn.devicebase.model.InputTextRequest;
import cn.devicebase.model.LaunchAppRequest;
import cn.devicebase.model.OperationResult;
import cn.devicebase.model.Point;
import cn.devicebase.model.StartDeviceUsageResponse;
import cn.devicebase.model.StopDeviceUsageResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP client for interacting with the DeviceBase API.
 *
 * <p>This client handles authentication, request/response serialization,
 * and error handling for all HTTP-based API operations.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * DeviceBaseHttpClient client = new DeviceBaseHttpClient(
 *     "https://api.devicebase.cn",
 *     "your-jwt-token"
 * );
 * DeviceInfo info = client.getDeviceInfo("device123");
 * }</pre>
 *
 * @author Richie
 * @version 1.0.0
 */
public class DeviceBaseHttpClient implements AutoCloseable {

    /** Default base URL for the DeviceBase API. */
    public static final String DEFAULT_BASE_URL = "https://api.devicebase.cn";

    private final String baseUrl;
    private final String apiKey;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new DeviceBaseHttpClient.
     *
     * @param baseUrl the base URL of the DeviceBase API
     * @param apiKey the JWT API key for authentication
     * @throws AuthenticationException if no API key is provided
     */
    public DeviceBaseHttpClient(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, Duration.ofSeconds(30));
    }

    /**
     * Creates a new DeviceBaseHttpClient with custom timeout.
     *
     * @param baseUrl the base URL of the DeviceBase API
     * @param apiKey the JWT API key for authentication
     * @param timeout the request timeout
     * @throws AuthenticationException if no API key is provided
     */
    public DeviceBaseHttpClient(String baseUrl, String apiKey, Duration timeout) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.apiKey = Objects.requireNonNull(apiKey, "API key is required");
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Normalizes the base URL by removing trailing slashes.
     */
    private static String normalizeBaseUrl(String url) {
        if (url == null) {
            return DEFAULT_BASE_URL;
        }
        String normalized = url.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.isEmpty() ? DEFAULT_BASE_URL : normalized;
    }

    /**
     * Generates authentication headers with JWT token.
     */
    private Map<String, String> authHeaders() {
        return Map.of(
            "Authorization", "Bearer " + apiKey,
            "Content-Type", "application/json"
        );
    }

    /**
     * Makes an HTTP POST request with JSON body.
     */
    private JsonNode post(String path, Map<String, ?> body) throws DeviceBaseException {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(timeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(body != null
                        ? HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body))
                        : HttpRequest.BodyPublishers.noBody());

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            return handleResponse(response);
        } catch (IOException | InterruptedException e) {
            throw new DeviceBaseException("Request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Makes an HTTP GET request.
     */
    private JsonNode get(String path) throws DeviceBaseException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(timeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return handleResponse(response);
        } catch (IOException | InterruptedException e) {
            throw new DeviceBaseException("Request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Gets raw bytes from the API (for screenshots).
     */
    private byte[] getBytes(String path) throws DeviceBaseException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(timeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            if (response.statusCode() >= 400) {
                handleError(response.statusCode(), new String(response.body()));
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new DeviceBaseException("Request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Handles HTTP response and parses JSON.
     */
    private JsonNode handleResponse(HttpResponse<String> response) throws DeviceBaseException {
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            try {
                return objectMapper.readTree(body);
            } catch (IOException e) {
                throw new DeviceBaseException("Failed to parse response: " + e.getMessage(), e);
            }
        }

        handleError(statusCode, body);
        return objectMapper.createObjectNode();
    }

    /**
     * Handles HTTP errors.
     */
    private void handleError(int statusCode, String body) throws DeviceBaseException {
        if (statusCode == 401) {
            throw new AuthenticationException("Authentication failed - invalid API key");
        } else if (statusCode == 404) {
            throw new DeviceNotFoundException("Device not found or not connected");
        } else if (statusCode == 422) {
            throw new ValidationException("Validation error: " + body);
        } else if (statusCode >= 400) {
            throw new DeviceBaseException("API error: " + statusCode + " - " + body, statusCode);
        }
    }

    /**
     * Closes the HTTP client and releases resources.
     */
    @Override
    public void close() {
        // HttpClient doesn't require explicit close in Java 11+
    }

    // ========== Device Control APIs ==========

    /**
     * Gets detailed information about a device.
     *
     * @param serial the device unique identifier
     * @return DeviceInfo containing device status and connection state
     * @throws DeviceNotFoundException if the device is not found
     * @throws ValidationException if the serial is invalid
     */
    public DeviceInfo getDeviceInfo(String serial) throws DeviceBaseException {
        JsonNode response = post("/v1/deviceinfo/" + serial, null);
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return DeviceInfo.fromMap(serial, data);
    }

    /**
     * Performs a single tap at the specified coordinates.
     *
     * @param serial the device unique identifier
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return OperationResult indicating success or failure
     */
    public OperationResult tap(String serial, int x, int y) throws DeviceBaseException {
        return tap(serial, new Point(x, y));
    }

    /**
     * Performs a single tap at the specified point.
     *
     * @param serial the device unique identifier
     * @param point the coordinates where the tap should occur
     * @return OperationResult indicating success or failure
     */
    public OperationResult tap(String serial, Point point) throws DeviceBaseException {
        JsonNode response = post("/v1/tap/" + serial, point.toMap());
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return OperationResult.fromMap(data);
    }

    /**
     * Performs a double tap at the specified coordinates.
     *
     * @param serial the device unique identifier
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return OperationResult indicating success or failure
     */
    public OperationResult doubleTap(String serial, int x, int y) throws DeviceBaseException {
        return doubleTap(serial, new Point(x, y));
    }

    /**
     * Performs a double tap at the specified point.
     *
     * @param serial the device unique identifier
     * @param point the coordinates where the double tap should occur
     * @return OperationResult indicating success or failure
     */
    public OperationResult doubleTap(String serial, Point point) throws DeviceBaseException {
        JsonNode response = post("/v1/double_tap/" + serial, point.toMap());
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return OperationResult.fromMap(data);
    }

    /**
     * Performs a long press at the specified coordinates.
     *
     * @param serial the device unique identifier
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return OperationResult indicating success or failure
     */
    public OperationResult longPress(String serial, int x, int y) throws DeviceBaseException {
        return longPress(serial, new Point(x, y));
    }

    /**
     * Performs a long press at the specified point.
     *
     * @param serial the device unique identifier
     * @param point the coordinates where the long press should occur
     * @return OperationResult indicating success or failure
     */
    public OperationResult longPress(String serial, Point point) throws DeviceBaseException {
        JsonNode response = post("/v1/long_press/" + serial, point.toMap());
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return OperationResult.fromMap(data);
    }

    /**
     * Performs a swipe gesture from start to end coordinates.
     *
     * @param serial the device unique identifier
     * @param x1 starting X coordinate
     * @param y1 starting Y coordinate
     * @param x2 ending X coordinate
     * @param y2 ending Y coordinate
     * @return OperationResult indicating success or failure
     */
    public OperationResult swipe(String serial, int x1, int y1, int x2, int y2)
            throws DeviceBaseException {
        return swipe(serial, new Bounds(x1, y1, x2, y2));
    }

    /**
     * Performs a swipe gesture from start to end coordinates.
     *
     * @param serial the device unique identifier
     * @param bounds the start and end coordinates
     * @return OperationResult indicating success or failure
     */
    public OperationResult swipe(String serial, Bounds bounds) throws DeviceBaseException {
        JsonNode response = post("/v1/swipe/" + serial, bounds.toMap());
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return OperationResult.fromMap(data);
    }

    /**
     * Simulates the device back button press.
     *
     * @param serial the device unique identifier
     * @return OperationResult indicating success or failure
     */
    public OperationResult back(String serial) throws DeviceBaseException {
        JsonNode response = post("/v1/back/" + serial, null);
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return OperationResult.fromMap(data);
    }

    /**
     * Simulates the device home button press.
     *
     * @param serial the device unique identifier
     * @return OperationResult indicating success or failure
     */
    public OperationResult home(String serial) throws DeviceBaseException {
        JsonNode response = post("/v1/home/" + serial, null);
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return OperationResult.fromMap(data);
    }

    /**
     * Launches an application on the device.
     *
     * @param serial the device unique identifier
     * @param appName the package name or identifier of the app to launch
     * @return OperationResult indicating success or failure
     */
    public OperationResult launchApp(String serial, String appName) throws DeviceBaseException {
        LaunchAppRequest request = new LaunchAppRequest(appName);
        JsonNode response = post("/v1/launch_app/" + serial, request.toMap());
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return OperationResult.fromMap(data);
    }

    /**
     * Inputs text into the currently focused field.
     *
     * @param serial the device unique identifier
     * @param text the text to input
     * @return OperationResult indicating success or failure
     */
    public OperationResult inputText(String serial, String text) throws DeviceBaseException {
        InputTextRequest request = new InputTextRequest(text);
        JsonNode response = post("/v1/input/" + serial, request.toMap());
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return OperationResult.fromMap(data);
    }

    /**
     * Clears text in the currently focused field.
     *
     * @param serial the device unique identifier
     * @return OperationResult indicating success or failure
     */
    public OperationResult clearText(String serial) throws DeviceBaseException {
        JsonNode response = post("/v1/clear_text/" + serial, null);
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return OperationResult.fromMap(data);
    }

    /**
     * Gets information about the currently running foreground app.
     *
     * @param serial the device unique identifier
     * @return AppInfo containing the current app name and details
     */
    public AppInfo getCurrentApp(String serial) throws DeviceBaseException {
        JsonNode response = post("/v1/current_app/" + serial, null);
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return AppInfo.fromMap(data);
    }

    /**
     * Gets the current UI hierarchy structure.
     *
     * @param serial the device unique identifier
     * @return HierarchyInfo containing the UI element tree
     */
    public HierarchyInfo dumpHierarchy(String serial) throws DeviceBaseException {
        JsonNode response = post("/v1/dump_hierarchy/" + serial, null);
        Map<String, Object> data = objectMapper.convertValue(response, new TypeReference<>() {});
        return HierarchyInfo.fromMap(data);
    }

    /**
     * Gets a screenshot of the device screen as JPEG bytes.
     *
     * @param serial the device unique identifier
     * @return raw JPEG image bytes
     * @throws DeviceNotFoundException if the device is not found
     */
    public byte[] getScreenshot(String serial) throws DeviceBaseException {
        return getBytes("/v1/screen/" + serial);
    }

    /**
     * Gets a screenshot using POST method.
     *
     * @param serial the device unique identifier
     * @return raw JPEG image bytes
     */
    public byte[] getScreenshotPost(String serial) throws DeviceBaseException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/screen/" + serial))
                    .timeout(timeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<byte[]> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            handleError(response.statusCode(), new String(response.body()));
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new DeviceBaseException("Request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Downloads screenshot as a file attachment.
     *
     * @param serial the device unique identifier
     * @return raw JPEG image bytes
     */
    public byte[] downloadScreenshot(String serial) throws DeviceBaseException {
        return getBytes("/v1/screenshot/" + serial);
    }

    // ========== Platform API ==========

    /**
     * Lists all devices.
     *
     * @param keyword optional keyword to search
     * @param type optional device type filter
     * @param state optional device state filter (online/offline)
     * @param page page number (1-based)
     * @param pageSize number of items per page
     * @return list of devices
     */
    public List<DeviceResponse> listDevices(String keyword, String type, String state,
            int page, int pageSize) throws DeviceBaseException {
        StringBuilder path = new StringBuilder("/api/v1/devices?page=").append(page)
                .append("&page_size=").append(pageSize);
        if (keyword != null && !keyword.isEmpty()) {
            path.append("&keyword=").append(keyword);
        }
        if (type != null && !type.isEmpty()) {
            path.append("&type=").append(type);
        }
        if (state != null && !state.isEmpty()) {
            path.append("&state=").append(state);
        }

        JsonNode response = get(path.toString());
        ApiResponse<List<DeviceResponse>> apiResponse = objectMapper.convertValue(
                response,
                new TypeReference<>() {}
        );
        if (!apiResponse.isSuccess()) {
            throw new DeviceBaseException("API error: " + apiResponse.getError());
        }
        return apiResponse.getData() != null ? apiResponse.getData() : Collections.emptyList();
    }

    /**
     * Gets a device by ID.
     *
     * @param deviceId the device ID
     * @return the device response
     */
    public DeviceResponse getDevice(int deviceId) throws DeviceBaseException {
        JsonNode response = get("/api/v1/devices/" + deviceId);
        ApiResponse<DeviceResponse> apiResponse = objectMapper.convertValue(
                response,
                new TypeReference<>() {}
        );
        if (!apiResponse.isSuccess()) {
            throw new DeviceBaseException("API error: " + apiResponse.getError());
        }
        return apiResponse.getData();
    }

    /**
     * Gets device statistics.
     *
     * @param includeShared whether to include shared devices
     * @return device statistics
     */
    @SuppressWarnings("unchecked")
    public DeviceStatsData getDeviceStats(boolean includeShared) throws DeviceBaseException {
        JsonNode response = get("/api/v1/devices/stats?include_shared=" + includeShared);
        ApiResponse<Map<String, Integer>> apiResponse = objectMapper.convertValue(
                response,
                new TypeReference<>() {}
        );
        if (!apiResponse.isSuccess()) {
            throw new DeviceBaseException("API error: " + apiResponse.getError());
        }
        Map<String, Integer> data = apiResponse.getData();
        return new DeviceStatsData(
                data.getOrDefault("total", 0),
                data.getOrDefault("free", 0),
                data.getOrDefault("busy", 0),
                data.getOrDefault("offline", 0)
        );
    }

    /**
     * Starts using a device.
     *
     * @param deviceId the device ID to start using
     * @return the start usage response
     */
    public StartDeviceUsageResponse startDeviceUsage(int deviceId) throws DeviceBaseException {
        JsonNode response = post("/api/v1/devices/usage/start",
                Map.of("device_id", deviceId));
        ApiResponse<StartDeviceUsageResponse> apiResponse = objectMapper.convertValue(
                response,
                new TypeReference<>() {}
        );
        if (!apiResponse.isSuccess()) {
            throw new DeviceBaseException("API error: " + apiResponse.getError());
        }
        return apiResponse.getData();
    }

    /**
     * Sends a heartbeat for the current session.
     *
     * @param sessionId the session ID
     * @return the heartbeat response
     */
    public HeartbeatResponse sendHeartbeat(String sessionId) throws DeviceBaseException {
        JsonNode response = post("/api/v1/devices/usage/heartbeat",
                Map.of("session_id", sessionId));
        ApiResponse<HeartbeatResponse> apiResponse = objectMapper.convertValue(
                response,
                new TypeReference<>() {}
        );
        if (!apiResponse.isSuccess()) {
            throw new DeviceBaseException("API error: " + apiResponse.getError());
        }
        return apiResponse.getData();
    }

    /**
     * Stops using a device.
     *
     * @param sessionId the session ID
     * @return the stop usage response
     */
    public StopDeviceUsageResponse stopDeviceUsage(String sessionId) throws DeviceBaseException {
        JsonNode response = post("/api/v1/devices/usage/stop",
                Map.of("session_id", sessionId));
        ApiResponse<StopDeviceUsageResponse> apiResponse = objectMapper.convertValue(
                response,
                new TypeReference<>() {}
        );
        if (!apiResponse.isSuccess()) {
            throw new DeviceBaseException("API error: " + apiResponse.getError());
        }
        return apiResponse.getData();
    }

    /**
     * Gets the active session information.
     *
     * @return the active session response
     */
    public GetActiveSessionResponse getActiveSession() throws DeviceBaseException {
        JsonNode response = get("/api/v1/devices/usage/active");
        ApiResponse<GetActiveSessionResponse> apiResponse = objectMapper.convertValue(
                response,
                new TypeReference<>() {}
        );
        if (!apiResponse.isSuccess()) {
            throw new DeviceBaseException("API error: " + apiResponse.getError());
        }
        return apiResponse.getData();
    }
}