package com.devicebase.client;

import com.devicebase.exception.AuthenticationException;
import com.devicebase.exception.DeviceBaseException;
import com.devicebase.http.DeviceBaseHttpClient;
import com.devicebase.model.AppInfo;
import com.devicebase.model.Bounds;
import com.devicebase.model.DeviceInfo;
import com.devicebase.model.DeviceResponse;
import com.devicebase.model.DeviceStatsData;
import com.devicebase.model.GetActiveSessionResponse;
import com.devicebase.model.HeartbeatResponse;
import com.devicebase.model.HierarchyInfo;
import com.devicebase.model.OperationResult;
import com.devicebase.model.Point;
import com.devicebase.model.StartDeviceUsageResponse;
import com.devicebase.model.StopDeviceUsageResponse;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Main client for interacting with the DeviceBase API.
 *
 * <p>This client provides a unified interface for all device automation operations,
 * including device control and platform API access.</p>
 *
 * <p>Configuration can be provided via constructor parameters or environment variables:</p>
 * <ul>
 *   <li>DEVICEBASE_BASE_URL: API base URL (default: https://api.devicebase.cn)</li>
 *   <li>DEVICEBASE_API_KEY: JWT API key for authentication</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * import com.devicebase.DeviceBaseClient;
 *
 * DeviceBaseClient client = new DeviceBaseClient("your-api-key", "device-serial-number");
 *
 * // Get device info
 * DeviceInfo info = client.getDeviceInfo();
 *
 * // Control the device
 * client.tap(100, 200);
 * client.launchApp("com.example.app");
 *
 * // Get screenshot
 * byte[] screenshot = client.getScreenshot();
 * }</pre>
 *
 * @author Richie
 * @version 1.0.0
 */
public class DeviceBaseClient implements Closeable, AutoCloseable {

    /** Default base URL for the DeviceBase API. */
    public static final String DEFAULT_BASE_URL = DeviceBaseHttpClient.DEFAULT_BASE_URL;

    /** Environment variable name for API key. */
    public static final String ENV_API_KEY = "DEVICEBASE_API_KEY";

    /** Environment variable name for base URL. */
    public static final String ENV_BASE_URL = "DEVICEBASE_BASE_URL";

    private final String serial;
    private final String baseUrl;
    private final String apiKey;
    private final Duration timeout;
    private final DeviceBaseHttpClient httpClient;

    /**
     * Creates a new DeviceBaseClient.
     *
     * <p>API key is read from the DEVICEBASE_API_KEY environment variable.</p>
     *
     * @param apiKey the JWT API key for authentication (or null to read from env)
     * @param serial the device unique identifier
     * @throws AuthenticationException if no API key is provided
     */
    public DeviceBaseClient(String apiKey, String serial) {
        this(System.getenv(ENV_API_KEY), serial, DEFAULT_BASE_URL, Duration.ofSeconds(30));
    }

    /**
     * Creates a new DeviceBaseClient with explicit base URL.
     *
     * @param apiKey the JWT API key for authentication
     * @param serial the device unique identifier
     * @param baseUrl the base URL of the DeviceBase API
     * @throws AuthenticationException if no API key is provided
     */
    public DeviceBaseClient(String apiKey, String serial, String baseUrl) {
        this(apiKey, serial, baseUrl, Duration.ofSeconds(30));
    }

    /**
     * Creates a new DeviceBaseClient with custom timeout.
     *
     * @param apiKey the JWT API key for authentication
     * @param serial the device unique identifier
     * @param baseUrl the base URL of the DeviceBase API
     * @param timeout the request timeout
     * @throws AuthenticationException if no API key is provided
     */
    public DeviceBaseClient(String apiKey, String serial, String baseUrl, Duration timeout) {
        this.apiKey = resolveApiKey(apiKey);
        this.serial = Objects.requireNonNull(serial, "Serial is required");
        this.baseUrl = baseUrl != null ? baseUrl : System.getenv(ENV_BASE_URL);
        this.baseUrl = this.baseUrl != null ? this.baseUrl : DEFAULT_BASE_URL;
        this.timeout = timeout;
        this.httpClient = new DeviceBaseHttpClient(this.baseUrl, this.apiKey, this.timeout);
    }

    /**
     * Creates a new DeviceBaseClient using a device response to get server URL.
     *
     * @param apiKey the JWT API key for authentication
     * @param deviceResponse the device response containing server URL
     * @throws AuthenticationException if no API key is provided
     */
    public DeviceBaseClient(String apiKey, DeviceResponse deviceResponse) {
        this.apiKey = resolveApiKey(apiKey);
        this.serial = deviceResponse.getSerial();
        this.baseUrl = deviceResponse.getServerUrl() != null
                ? deviceResponse.getServerUrl()
                : DEFAULT_BASE_URL;
        this.timeout = Duration.ofSeconds(30);
        this.httpClient = new DeviceBaseHttpClient(this.baseUrl, this.apiKey, this.timeout);
    }

    /**
     * Resolves the API key from parameter or environment variable.
     */
    private String resolveApiKey(String apiKey) {
        String resolved = apiKey != null ? apiKey : System.getenv(ENV_API_KEY);
        if (resolved == null || resolved.isEmpty()) {
            throw new AuthenticationException(
                "API key is required. Provide it via 'apiKey' parameter "
                + "or DEVICEBASE_API_KEY environment variable."
            );
        }
        return resolved;
    }

    /**
     * Returns the device serial.
     *
     * @return the device unique identifier
     */
    public String getSerial() {
        return serial;
    }

    /**
     * Returns the base URL.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Closes the client and releases all resources.
     */
    @Override
    public void close() {
        httpClient.close();
    }

    // ========== Device Info ==========

    /**
     * Gets detailed information about the device.
     *
     * @return DeviceInfo containing device status, hardware info, and connection state
     * @throws DeviceBaseException if the device is not found or not connected
     */
    public DeviceInfo getDeviceInfo() throws DeviceBaseException {
        return httpClient.getDeviceInfo(serial);
    }

    // ========== Touch Operations ==========

    /**
     * Performs a single tap at the specified coordinates.
     *
     * @param x horizontal coordinate (pixels from left)
     * @param y vertical coordinate (pixels from top)
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult tap(int x, int y) throws DeviceBaseException {
        return httpClient.tap(serial, x, y);
    }

    /**
     * Performs a single tap at the specified point.
     *
     * @param point the coordinates where the tap should occur
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult tap(Point point) throws DeviceBaseException {
        return httpClient.tap(serial, point);
    }

    /**
     * Performs a double tap at the specified coordinates.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult doubleTap(int x, int y) throws DeviceBaseException {
        return httpClient.doubleTap(serial, x, y);
    }

    /**
     * Performs a double tap at the specified point.
     *
     * @param point the coordinates where the double tap should occur
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult doubleTap(Point point) throws DeviceBaseException {
        return httpClient.doubleTap(serial, point);
    }

    /**
     * Performs a long press at the specified coordinates.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult longPress(int x, int y) throws DeviceBaseException {
        return httpClient.longPress(serial, x, y);
    }

    /**
     * Performs a long press at the specified point.
     *
     * @param point the coordinates where the long press should occur
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult longPress(Point point) throws DeviceBaseException {
        return httpClient.longPress(serial, point);
    }

    /**
     * Performs a swipe gesture from start to end coordinates.
     *
     * @param x1 starting X coordinate
     * @param y1 starting Y coordinate
     * @param x2 ending X coordinate
     * @param y2 ending Y coordinate
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult swipe(int x1, int y1, int x2, int y2) throws DeviceBaseException {
        return httpClient.swipe(serial, x1, y1, x2, y2);
    }

    /**
     * Performs a swipe gesture from start to end coordinates.
     *
     * @param bounds the start and end coordinates
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult swipe(Bounds bounds) throws DeviceBaseException {
        return httpClient.swipe(serial, bounds);
    }

    // ========== Navigation ==========

    /**
     * Simulates the device back button press.
     *
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult back() throws DeviceBaseException {
        return httpClient.back(serial);
    }

    /**
     * Simulates the device home button press.
     *
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult home() throws DeviceBaseException {
        return httpClient.home(serial);
    }

    // ========== App Operations ==========

    /**
     * Launches an application on the device.
     *
     * @param appName the package name or identifier of the app to launch
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult launchApp(String appName) throws DeviceBaseException {
        return httpClient.launchApp(serial, appName);
    }

    /**
     * Gets information about the currently running foreground app.
     *
     * @return AppInfo containing the current app name and details
     * @throws DeviceBaseException if the request fails
     */
    public AppInfo getCurrentApp() throws DeviceBaseException {
        return httpClient.getCurrentApp(serial);
    }

    // ========== Text Input ==========

    /**
     * Inputs text into the currently focused field.
     *
     * @param text the text to input
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult inputText(String text) throws DeviceBaseException {
        return httpClient.inputText(serial, text);
    }

    /**
     * Clears text in the currently focused field.
     *
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult clearText() throws DeviceBaseException {
        return httpClient.clearText(serial);
    }

    // ========== UI Hierarchy ==========

    /**
     * Gets the current UI hierarchy structure.
     *
     * @return HierarchyInfo containing the UI element tree
     * @throws DeviceBaseException if the request fails
     */
    public HierarchyInfo dumpHierarchy() throws DeviceBaseException {
        return httpClient.dumpHierarchy(serial);
    }

    // ========== Screenshots ==========

    /**
     * Gets a screenshot of the device screen as JPEG bytes.
     *
     * @return raw JPEG image bytes
     * @throws DeviceBaseException if the device is not found
     */
    public byte[] getScreenshot() throws DeviceBaseException {
        return httpClient.getScreenshot(serial);
    }

    /**
     * Downloads screenshot as a file attachment.
     *
     * @return raw JPEG image bytes
     * @throws DeviceBaseException if the request fails
     */
    public byte[] downloadScreenshot() throws DeviceBaseException {
        return httpClient.downloadScreenshot(serial);
    }

    // ========== Platform API ==========

    /**
     * Lists all devices.
     *
     * @return list of devices
     * @throws DeviceBaseException if the request fails
     */
    public List<DeviceResponse> listDevices() throws DeviceBaseException {
        return listDevices(null, null, null, 1, 20);
    }

    /**
     * Lists devices with filters.
     *
     * @param keyword optional keyword to search
     * @param type optional device type filter
     * @param state optional device state filter (online/offline)
     * @param page page number (1-based)
     * @param pageSize number of items per page
     * @return list of devices
     * @throws DeviceBaseException if the request fails
     */
    public List<DeviceResponse> listDevices(String keyword, String type, String state,
            int page, int pageSize) throws DeviceBaseException {
        return httpClient.listDevices(keyword, type, state, page, pageSize);
    }

    /**
     * Gets a device by ID.
     *
     * @param deviceId the device ID
     * @return the device response
     * @throws DeviceBaseException if the request fails
     */
    public DeviceResponse getDevice(int deviceId) throws DeviceBaseException {
        return httpClient.getDevice(deviceId);
    }

    /**
     * Gets device statistics.
     *
     * @param includeShared whether to include shared devices
     * @return device statistics
     * @throws DeviceBaseException if the request fails
     */
    public DeviceStatsData getDeviceStats(boolean includeShared) throws DeviceBaseException {
        return httpClient.getDeviceStats(includeShared);
    }

    /**
     * Starts using a device.
     *
     * @param deviceId the device ID to start using
     * @return the start usage response
     * @throws DeviceBaseException if the request fails
     */
    public StartDeviceUsageResponse startDeviceUsage(int deviceId) throws DeviceBaseException {
        return httpClient.startDeviceUsage(deviceId);
    }

    /**
     * Sends a heartbeat for the current session.
     *
     * @param sessionId the session ID
     * @return the heartbeat response
     * @throws DeviceBaseException if the request fails
     */
    public HeartbeatResponse sendHeartbeat(String sessionId) throws DeviceBaseException {
        return httpClient.sendHeartbeat(sessionId);
    }

    /**
     * Stops using a device.
     *
     * @param sessionId the session ID
     * @return the stop usage response
     * @throws DeviceBaseException if the request fails
     */
    public StopDeviceUsageResponse stopDeviceUsage(String sessionId) throws DeviceBaseException {
        return httpClient.stopDeviceUsage(sessionId);
    }

    /**
     * Gets the active session information.
     *
     * @return the active session response
     * @throws DeviceBaseException if the request fails
     */
    public GetActiveSessionResponse getActiveSession() throws DeviceBaseException {
        return httpClient.getActiveSession();
    }

    /**
     * Gets the underlying HTTP client for advanced operations.
     *
     * @return the HTTP client
     */
    public DeviceBaseHttpClient getHttpClient() {
        return httpClient;
    }
}