package cn.devicebase.http;

import cn.devicebase.exception.AuthenticationException;
import cn.devicebase.exception.BusinessException;
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
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP client for interacting with the DeviceBase API.
 *
 * <p>This client handles authentication, request/response serialization, and
 * error handling. It exposes three device platforms, each with its own route
 * family:</p>
 *
 * <ul>
 *   <li><b>mobile</b> (Android / HarmonyOS / iOS) &mdash;
 *       {@code /v1/{action}/{serialno}}</li>
 *   <li><b>browser</b> (Chrome / Chromium / Edge over CDP) &mdash;
 *       {@link #browser()}</li>
 *   <li><b>computer</b> (macOS / Windows / Linux desktops) &mdash;
 *       {@link #computer()}</li>
 * </ul>
 *
 * <p>Every method takes the device's {@code serialno} first. A serialno is only
 * meaningful within one platform family, so it is discovered with
 * {@link #listDevices} before it is used.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * DeviceBaseHttpClient client = new DeviceBaseHttpClient(
 *     "https://api.devicebase.cn",
 *     "your-api-key"
 * );
 *
 * DeviceInfo info = client.getDeviceInfo("db-mttul4i41di8");
 * client.browser().navigate("db-mtsi49bf0mqb", "https://example.com");
 * client.computer().click("db-mtthisv311f1", 640, 360, null);
 * }</pre>
 *
 * @author Richie
 * @version 1.0.0
 */
public class DeviceBaseHttpClient implements AutoCloseable {

    /** Default base URL for the DeviceBase API. */
    public static final String DEFAULT_BASE_URL = "https://api.devicebase.cn";

    /** Deadline for an ordinary request. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final String baseUrl;
    private final String apiKey;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final BrowserApi browserApi;
    private final ComputerApi computerApi;

    /**
     * Creates a new DeviceBaseHttpClient.
     *
     * @param baseUrl the base URL of the DeviceBase API
     * @param apiKey the API key for authentication
     * @throws AuthenticationException if no API key is provided
     */
    public DeviceBaseHttpClient(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new DeviceBaseHttpClient with custom timeout.
     *
     * @param baseUrl the base URL of the DeviceBase API
     * @param apiKey the API key for authentication
     * @param timeout the request timeout
     * @throws AuthenticationException if no API key is provided
     */
    public DeviceBaseHttpClient(String baseUrl, String apiKey, Duration timeout) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.apiKey = Objects.requireNonNull(apiKey, "API key is required");
        this.timeout = timeout != null ? timeout : DEFAULT_TIMEOUT;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();
        SimpleModule tolerantInstants = new SimpleModule();
        tolerantInstants.addDeserializer(Instant.class, new TolerantInstantDeserializer());
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(tolerantInstants)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.browserApi = new BrowserApi(this);
        this.computerApi = new ComputerApi(this);
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

    // ========== Path building ==========

    /**
     * Builds {@code /v1/{action}/{serialno}} — the mobile route family.
     *
     * @param serialno the device serialno
     * @param action the action name
     * @return the request path
     */
    public static String mobilePath(String serialno, String action) {
        return "/v1/" + action + "/" + encode(serialno);
    }

    /**
     * Builds {@code /api/browser/{serialno}/{action}}.
     *
     * @param serialno the browser device serialno
     * @param action the action name
     * @return the request path
     */
    public static String browserPath(String serialno, String action) {
        return "/api/browser/" + encode(serialno) + "/" + action;
    }

    /**
     * Builds {@code /api/computer/{serialno}/{action}}.
     *
     * @param serialno the computer device serialno
     * @param action the action name
     * @return the request path
     */
    public static String computerPath(String serialno, String action) {
        return "/api/computer/" + encode(serialno) + "/" + action;
    }

    /**
     * URL-encodes a path segment so a serialno cannot break out of it.
     */
    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    /**
     * Renders query parameters, skipping null and empty values.
     *
     * @param params the parameters
     * @return the query string, including the leading {@code ?}, or empty
     */
    static String buildQuery(Map<String, ?> params) {
        if (params == null) {
            return "";
        }
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value == null || value.toString().isEmpty()) {
                continue;
            }
            query.append(query.length() == 0 ? "?" : "&")
                    .append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(value.toString()));
        }
        return query.toString();
    }

    /**
     * Builds a request body with a stable field order, so a request is
     * byte-identical across runs. {@code Map.of} guarantees no ordering, which
     * would make bodies churn between invocations.
     *
     * @param keyValues alternating field names and values
     * @return an ordered body map
     */
    static Map<String, Object> orderedBody(Object... keyValues) {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            fields.put((String) keyValues[i], keyValues[i + 1]);
        }
        return fields;
    }

    // ========== Transport ==========

    /**
     * Returns the browser platform API.
     *
     * @return the browser API, bound to this transport
     */
    public BrowserApi browser() {
        return browserApi;
    }

    /**
     * Returns the computer platform API.
     *
     * @return the computer API, bound to this transport
     */
    public ComputerApi computer() {
        return computerApi;
    }

    /**
     * Returns the base URL.
     *
     * @return the normalized base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the object mapper used for request and response bodies.
     *
     * @return the object mapper
     */
    ObjectMapper mapper() {
        return objectMapper;
    }

    /**
     * Sends a request and decodes the JSON response.
     *
     * @param method the HTTP method
     * @param path the request path, already built
     * @param body the JSON body, or null for none
     * @param override the per-call deadline, or null to use the client default
     * @return the decoded response body
     * @throws DeviceBaseException on either failure layer
     */
    JsonNode request(String method, String path, Map<String, ?> body, Duration override)
            throws DeviceBaseException {
        HttpResponse<String> response = send(
                method, path, body, override, HttpResponse.BodyHandlers.ofString());
        String text = response.body();
        if (text == null || text.isEmpty()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(text);
        } catch (IOException e) {
            throw new DeviceBaseException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a request and returns the raw response bytes.
     *
     * @param method the HTTP method
     * @param path the request path, already built
     * @param override the per-call deadline, or null to use the client default
     * @return the raw response body
     * @throws DeviceBaseException on either failure layer
     */
    byte[] requestBytes(String method, String path, Duration override) throws DeviceBaseException {
        return send(method, path, null, override, HttpResponse.BodyHandlers.ofByteArray()).body();
    }

    /**
     * Sends a request, mapping both failure layers onto exceptions.
     *
     * <p>A non-2xx HTTP status is raised as the matching exception; an HTTP 200
     * whose body carries a non-2xx envelope {@code code} is raised as a
     * {@link BusinessException}.</p>
     */
    private <T> HttpResponse<T> send(String method, String path, Map<String, ?> body,
            Duration override, HttpResponse.BodyHandler<T> handler) throws DeviceBaseException {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(override != null ? override : timeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json");

            if ("GET".equals(method)) {
                requestBuilder.GET();
            } else if (body != null) {
                requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(body)));
            } else {
                requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpResponse<T> response = httpClient.send(requestBuilder.build(), handler);
            checkStatus(response.statusCode(), response.body());
            return response;
        } catch (IOException e) {
            throw new DeviceBaseException("Request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DeviceBaseException("Request interrupted: " + e.getMessage(), e);
        }
    }

    /**
     * Raises for a non-2xx status, and for a business error inside an otherwise
     * successful response.
     */
    private void checkStatus(int statusCode, Object body) throws DeviceBaseException {
        String text = body instanceof String ? (String) body : "";
        if (statusCode >= 400) {
            throw errorForStatus(statusCode, text);
        }
        if (body instanceof String) {
            checkEnvelope((String) body);
        }
    }

    /**
     * Maps a non-2xx HTTP status onto the matching exception, carrying the
     * server's own body so its message reaches the caller.
     */
    private DeviceBaseException errorForStatus(int statusCode, String body) {
        String message = "API error (HTTP " + statusCode + "): " + truncate(body);
        switch (statusCode) {
            case 401:
                return new AuthenticationException(message);
            case 404:
                return new DeviceNotFoundException(message);
            case 400:
            case 422:
                // The gateway reports validation failures as 400; 422 is kept for
                // compatibility with older deployments.
                return new ValidationException(message);
            default:
                return new DeviceBaseException(message, statusCode);
        }
    }

    /**
     * Surfaces a business error carried inside an otherwise successful
     * response.
     *
     * <p>Bodies that are not an envelope (arrays, empty, non-JSON) are left
     * alone, as are codes inside the 2xx range.</p>
     */
    private void checkEnvelope(String text) throws BusinessException {
        if (text == null || text.isEmpty()) {
            return;
        }
        String trimmed = text.stripLeading();
        if (!trimmed.startsWith("{")) {
            return;
        }
        JsonNode parsed;
        try {
            parsed = objectMapper.readTree(trimmed);
        } catch (IOException e) {
            return;
        }
        JsonNode codeNode = parsed.get("code");
        if (codeNode == null || !codeNode.isInt()) {
            return;
        }
        int code = codeNode.asInt();
        if (code >= 200 && code < 300) {
            return;
        }
        throw new BusinessException(code, text);
    }

    private static String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 4096 ? body : body.substring(0, 4096) + "… (truncated)";
    }

    /**
     * Closes the HTTP client and releases resources.
     */
    @Override
    public void close() {
        // HttpClient doesn't require explicit close in Java 11+
    }

    // ========== Mobile ==========

    private Map<String, Object> toMap(JsonNode response) {
        return objectMapper.convertValue(response, new TypeReference<>() { });
    }

    private OperationResult operation(JsonNode response) {
        return OperationResult.fromMap(toMap(response));
    }

    /**
     * Gets detailed information about a device.
     *
     * @param serial the device serialno
     * @return DeviceInfo containing device status and connection state
     * @throws DeviceNotFoundException if the device is not found
     * @throws ValidationException if the serial is invalid
     */
    public DeviceInfo getDeviceInfo(String serial) throws DeviceBaseException {
        JsonNode response = request("POST", mobilePath(serial, "deviceinfo"), null, null);
        return DeviceInfo.fromMap(serial, toMap(response));
    }

    /**
     * Performs a single tap at the specified coordinates.
     *
     * @param serial the device serialno
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
     * @param serial the device serialno
     * @param point the coordinates where the tap should occur
     * @return OperationResult indicating success or failure
     */
    public OperationResult tap(String serial, Point point) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "tap"), point.toMap(), null));
    }

    /**
     * Performs a double tap at the specified coordinates.
     *
     * @param serial the device serialno
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
     * @param serial the device serialno
     * @param point the coordinates where the double tap should occur
     * @return OperationResult indicating success or failure
     */
    public OperationResult doubleTap(String serial, Point point) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "double_tap"), point.toMap(), null));
    }

    /**
     * Performs a long press at the specified coordinates.
     *
     * @param serial the device serialno
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
     * @param serial the device serialno
     * @param point the coordinates where the long press should occur
     * @return OperationResult indicating success or failure
     */
    public OperationResult longPress(String serial, Point point) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "long_press"), point.toMap(), null));
    }

    /**
     * Performs a swipe gesture from start to end coordinates.
     *
     * @param serial the device serialno
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
     * @param serial the device serialno
     * @param bounds the start and end coordinates
     * @return OperationResult indicating success or failure
     */
    public OperationResult swipe(String serial, Bounds bounds) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "swipe"), bounds.toMap(), null));
    }

    /**
     * Simulates the device back button press.
     *
     * @param serial the device serialno
     * @return OperationResult indicating success or failure
     */
    public OperationResult back(String serial) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "back"), null, null));
    }

    /**
     * Simulates the device home button press.
     *
     * @param serial the device serialno
     * @return OperationResult indicating success or failure
     */
    public OperationResult home(String serial) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "home"), null, null));
    }

    /**
     * Launches an application on the device.
     *
     * @param serial the device serialno
     * @param appName the package name or identifier of the app to launch
     * @return OperationResult indicating success or failure
     */
    public OperationResult launchApp(String serial, String appName) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "launch_app"),
                new LaunchAppRequest(appName).toMap(), null));
    }

    /**
     * Stops an application on the device.
     *
     * @param serial the device serialno
     * @param appName the package name or identifier of the app to stop
     * @return OperationResult indicating success or failure
     */
    public OperationResult stopApp(String serial, String appName) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "stop_app"),
                new LaunchAppRequest(appName).toMap(), null));
    }

    /**
     * Stops the app currently in the foreground.
     *
     * @param serial the device serialno
     * @return OperationResult indicating success or failure
     */
    public OperationResult stopCurrentApp(String serial) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "stop_current_app"), null, null));
    }

    /**
     * Runs a shell command on the device (adb/hdc platforms only).
     *
     * <p>The command's own exit status comes back in the payload as
     * {@code data.exitCode} — a non-zero value is not an API error.</p>
     *
     * @param serial the device serialno
     * @param command the shell command
     * @return OperationResult whose data carries exitCode/stdout/stderr
     */
    public OperationResult bash(String serial, String command) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "bash"),
                orderedBody("command", command), null));
    }

    /**
     * Inputs text into the currently focused field.
     *
     * @param serial the device serialno
     * @param text the text to input
     * @return OperationResult indicating success or failure
     */
    public OperationResult inputText(String serial, String text) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "input"),
                new InputTextRequest(text).toMap(), null));
    }

    /**
     * Clears text in the currently focused field.
     *
     * @param serial the device serialno
     * @return OperationResult indicating success or failure
     */
    public OperationResult clearText(String serial) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "clear_text"), null, null));
    }

    /**
     * Gets information about the currently running foreground app.
     *
     * @param serial the device serialno
     * @return AppInfo containing the current app name and details
     */
    public AppInfo getCurrentApp(String serial) throws DeviceBaseException {
        return AppInfo.fromMap(toMap(request("POST", mobilePath(serial, "current_app"), null, null)));
    }

    /**
     * Gets the current UI hierarchy structure.
     *
     * @param serial the device serialno
     * @return HierarchyInfo containing the UI element tree
     */
    public HierarchyInfo dumpHierarchy(String serial) throws DeviceBaseException {
        return HierarchyInfo.fromMap(
                toMap(request("POST", mobilePath(serial, "dump_hierarchy"), null, null)));
    }

    /**
     * Installs a package from a path on the agent host.
     *
     * <p>The path is resolved on the agent machine that owns the device, not
     * locally. The call returns an install id for a background task, polled
     * with {@link #installStatus}.</p>
     *
     * @param serial the device serialno
     * @param appPath the package path on the agent host
     * @return OperationResult whose data carries the install id
     */
    public OperationResult installApp(String serial, String appPath) throws DeviceBaseException {
        return operation(request("POST", mobilePath(serial, "install_app"),
                orderedBody("app_path", appPath), null));
    }

    /**
     * Queries a background install task.
     *
     * @param serial the device serialno
     * @param installId the install id returned by {@link #installApp}
     * @return OperationResult describing the install
     */
    public OperationResult installStatus(String serial, String installId)
            throws DeviceBaseException {
        String path = mobilePath(serial, "install_status")
                + buildQuery(orderedBody("install_id", installId));
        return operation(request("GET", path, null, null));
    }

    /**
     * Gets a screenshot of the device screen as image bytes.
     *
     * <p>The server decides the format (JPEG for every platform), so the bytes
     * are returned without a decoded wrapper. This is a cross-family route: the
     * server dispatches {@code /v1/screen/{serialno}} by device type, so browser
     * and computer serials are valid too.</p>
     *
     * @param serial the device serialno
     * @return raw image bytes
     * @throws DeviceNotFoundException if the device is not found
     */
    public byte[] getScreenshot(String serial) throws DeviceBaseException {
        return requestBytes("POST", mobilePath(serial, "screen"), null);
    }

    /**
     * Gets a screenshot using POST.
     *
     * @param serial the device serialno
     * @return raw image bytes
     * @deprecated {@link #getScreenshot} now posts to the same route; this is a
     *     duplicate kept for source compatibility.
     */
    @Deprecated
    public byte[] getScreenshotPost(String serial) throws DeviceBaseException {
        return getScreenshot(serial);
    }

    /**
     * Downloads screenshot as a file attachment.
     *
     * <p>This is an SDK-only extra with no equivalent in the Devicebase CLI: it
     * hits {@code GET /v1/screenshot/{serialno}} rather than the cross-family
     * {@code /v1/screen} route.</p>
     *
     * @param serial the device serialno
     * @return raw image bytes
     */
    public byte[] downloadScreenshot(String serial) throws DeviceBaseException {
        return requestBytes("GET", "/v1/screenshot/" + encode(serial), null);
    }

    // ========== Devices ==========

    /**
     * Lists the devices accessible to the current API key.
     *
     * <p>Filtering is resolved server-side. {@code type} accepts either a
     * category bucket ({@code mobile|browser|computer}) or a system type
     * ({@code android|harmonyos|ios|macos|windows|linux|chrome|chromium|edge|other});
     * buckets match against the device's {@code os_type}, because a device row
     * only carries the coarse type.</p>
     *
     * <p>This is how a serialno is discovered before driving a device.</p>
     *
     * @param keyword optional keyword filter, matched across several columns
     * @param type optional category bucket or system type
     * @param state optional state filter (busy/free/offline)
     * @param limit optional maximum number of devices (the server defaults to 10)
     * @return the matching devices
     * @throws DeviceBaseException if the request fails
     */
    public List<DeviceResponse> listDevices(String keyword, String type, String state, Integer limit)
            throws DeviceBaseException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("keyword", keyword);
        params.put("type", type);
        params.put("state", state);
        params.put("limit", limit);

        JsonNode response = request("GET", "/v1/devices" + buildQuery(params), null, null);
        JsonNode data = response.get("data");
        if (data == null || data.isNull()) {
            return List.of();
        }
        return objectMapper.convertValue(data, new TypeReference<>() { });
    }

    /**
     * Lists all devices the current API key can see.
     *
     * @return the matching devices
     * @throws DeviceBaseException if the request fails
     */
    public List<DeviceResponse> listDevices() throws DeviceBaseException {
        return listDevices(null, null, null, null);
    }

    /**
     * Lists devices of one category.
     *
     * @param type a category bucket (mobile/browser/computer) or system type
     * @return the matching devices
     * @throws DeviceBaseException if the request fails
     */
    public List<DeviceResponse> listDevices(String type) throws DeviceBaseException {
        return listDevices(null, type, null, null);
    }

    // ========== Legacy platform API ==========
    //
    // The methods below target the /api/v1/devices/* route family, which the
    // current gateway does not serve — every one of them answers 404. They are
    // kept, and marked deprecated, so existing callers keep compiling while the
    // replacement (listDevices) is adopted. Do not use them in new code.
    //
    // The replacement for listDevices/getDevice/getDeviceStats is
    // listDevices(keyword, type, state, limit), which reads the live
    // /v1/devices route.

    /**
     * Gets a device by ID.
     *
     * @param deviceId the device ID
     * @return the device response
     * @throws DeviceBaseException if the request fails
     * @deprecated the {@code /api/v1/devices/{id}} route is not served by the
     *     current gateway and answers 404. Use {@link #listDevices} instead.
     */
    @Deprecated
    public DeviceResponse getDevice(int deviceId) throws DeviceBaseException {
        JsonNode response = request("GET", "/api/v1/devices/" + deviceId, null, null);
        ApiResponse<DeviceResponse> apiResponse = objectMapper.convertValue(
                response, new TypeReference<>() { });
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
     * @throws DeviceBaseException if the request fails
     * @deprecated the {@code /api/v1/devices/stats} route is not served by the
     *     current gateway and answers 404. Use {@link #listDevices} and count
     *     client-side instead.
     */
    @Deprecated
    public DeviceStatsData getDeviceStats(boolean includeShared) throws DeviceBaseException {
        JsonNode response = request(
                "GET", "/api/v1/devices/stats?include_shared=" + includeShared, null, null);
        ApiResponse<Map<String, Integer>> apiResponse = objectMapper.convertValue(
                response, new TypeReference<>() { });
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
     * @throws DeviceBaseException if the request fails
     * @deprecated the {@code /api/v1/devices/usage/*} routes are not served by
     *     the current gateway and answer 404.
     */
    @Deprecated
    public StartDeviceUsageResponse startDeviceUsage(int deviceId) throws DeviceBaseException {
        JsonNode response = request(
                "POST", "/api/v1/devices/usage/start", Map.of("device_id", deviceId), null);
        ApiResponse<StartDeviceUsageResponse> apiResponse = objectMapper.convertValue(
                response, new TypeReference<>() { });
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
     * @throws DeviceBaseException if the request fails
     * @deprecated the {@code /api/v1/devices/usage/*} routes are not served by
     *     the current gateway and answer 404.
     */
    @Deprecated
    public HeartbeatResponse sendHeartbeat(String sessionId) throws DeviceBaseException {
        JsonNode response = request(
                "POST", "/api/v1/devices/usage/heartbeat", Map.of("session_id", sessionId), null);
        ApiResponse<HeartbeatResponse> apiResponse = objectMapper.convertValue(
                response, new TypeReference<>() { });
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
     * @throws DeviceBaseException if the request fails
     * @deprecated the {@code /api/v1/devices/usage/*} routes are not served by
     *     the current gateway and answer 404.
     */
    @Deprecated
    public StopDeviceUsageResponse stopDeviceUsage(String sessionId) throws DeviceBaseException {
        JsonNode response = request(
                "POST", "/api/v1/devices/usage/stop", Map.of("session_id", sessionId), null);
        ApiResponse<StopDeviceUsageResponse> apiResponse = objectMapper.convertValue(
                response, new TypeReference<>() { });
        if (!apiResponse.isSuccess()) {
            throw new DeviceBaseException("API error: " + apiResponse.getError());
        }
        return apiResponse.getData();
    }

    /**
     * Gets the active session information.
     *
     * @return the active session response
     * @throws DeviceBaseException if the request fails
     * @deprecated the {@code /api/v1/devices/usage/*} routes are not served by
     *     the current gateway and answer 404.
     */
    @Deprecated
    public GetActiveSessionResponse getActiveSession() throws DeviceBaseException {
        JsonNode response = request("GET", "/api/v1/devices/usage/active", null, null);
        ApiResponse<GetActiveSessionResponse> apiResponse = objectMapper.convertValue(
                response, new TypeReference<>() { });
        if (!apiResponse.isSuccess()) {
            throw new DeviceBaseException("API error: " + apiResponse.getError());
        }
        return apiResponse.getData();
    }
}
