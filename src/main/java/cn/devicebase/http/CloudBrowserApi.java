package cn.devicebase.http;

import cn.devicebase.exception.DeviceBaseException;
import cn.devicebase.exception.ValidationException;
import cn.devicebase.model.CloudBrowserCreateResult;
import cn.devicebase.model.CloudBrowserQuota;
import cn.devicebase.model.CloudBrowserStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cloud browser lifecycle — {@code POST /v1/browser/create},
 * {@code DELETE /v1/browser/{serialno}}, {@code GET /v1/browser/{serialno}/status}
 * and {@code GET /v1/browser/quota}.
 *
 * <p>Not to be confused with {@link BrowserApi}: those are device actions against
 * an already registered browser ({@code /api/browser/{serialno}/{action}}), while
 * these four are the account-level resource lifecycle that creates and destroys
 * one. A <em>cloud browser</em> is a browser the platform runs for you on its own
 * cluster; a Chrome you attached yourself is not one of them — see
 * {@link cn.devicebase.model.DeviceResponse#isCloud()}.</p>
 *
 * <p>Obtain an instance with {@link DeviceBaseHttpClient#cloudBrowser()}.</p>
 *
 * <p>Status codes decide whether a retry is worth it and surface as the matching
 * exception: 409 is a conflict retrying will not fix (quota exhausted, or the
 * identifier is not a cloud browser), 502 a platform-to-node problem somebody has
 * to repair, 503 the temporary kind (no capacity right now, or the node could not
 * be reached).</p>
 *
 * @author Richie
 * @version 1.0.0
 */
public class CloudBrowserApi {

    /** The server trims and bounds the name the same way. */
    private static final int NAME_MAX_LENGTH = 100;

    /** Wait bounds — the server's own (it rejects anything outside them). */
    private static final int DEFAULT_WAIT_SECONDS = 15;
    private static final int MAX_WAIT_SECONDS = 60;

    /**
     * The platform's node-call budget is 20s, and the wait comes after it.
     *
     * <p>The per-call deadline is this plus the wait, so the client never gives
     * up on a request the server is still answering.</p>
     */
    private static final Duration CREATE_TIMEOUT_SLACK = Duration.ofSeconds(45);

    private final DeviceBaseHttpClient http;

    CloudBrowserApi(DeviceBaseHttpClient http) {
        this.http = http;
    }

    /**
     * The {@code data} object of an envelope, or an empty one.
     *
     * <p>An empty object rather than a null node: converting null would hand
     * back a null DTO, and a caller reading {@code status.isRegistered()} — the
     * normal answer while a browser is still starting up — would get a
     * NullPointerException instead of {@code false}.</p>
     */
    private JsonNode dataOf(JsonNode response) {
        JsonNode data = response.get("data");
        return data == null || data.isNull() ? http.mapper().createObjectNode() : data;
    }

    /**
     * Builds {@code /v1/browser/{identifier}}, escaping the path segment.
     *
     * <p>The identifier is caller-supplied — a serialno, or the {@code serial}
     * returned by creation — so unlike a device serialno it is not guaranteed to
     * be URL-safe.</p>
     *
     * @param identifier the serialno or device_sn
     * @return the request path
     */
    public static String cloudBrowserPath(String identifier) {
        String value = identifier == null ? "" : identifier;
        return "/v1/browser/" + URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    // --- Creation ---

    /**
     * Asks the platform for a new cloud browser.
     *
     * <p>The platform picks the machine itself, so there is nothing here to
     * choose a node with. Creation is asynchronous — the browser has to start
     * and register itself — so the call waits for that (15 seconds by default)
     * and hands back the {@code serialno} every other browser method takes.
     * Nobody should have to write a poll loop to use what they just created.</p>
     *
     * <p>A cloud browser always runs headless: it lives on a machine nobody is
     * looking at, and there is no argument for that.</p>
     *
     * @param name label shown in the device list, at most 100 characters; null
     *     lets the server apply its own naming rule
     * @param windowSize the {@code 1366x768} shape the node parses; null leaves
     *     the driver's default
     * @param waitSeconds how long to wait for it to register, 0-60. {@code 0}
     *     answers immediately, which suits creating a batch; null waits the
     *     server's own default of 15
     * @return the creation result — {@code serialno} (empty until it is up),
     *     {@code deviceSn}, {@code name} and {@code registered}
     * @throws ValidationException if a name, window size or wait was rejected
     *     locally, before any request
     * @throws DeviceBaseException if the request fails — 409 when the quota is
     *     exhausted, 503 when no node had room or was reachable, 502 when the
     *     platform and the node failed to authenticate each other
     */
    public CloudBrowserCreateResult create(String name, String windowSize, Integer waitSeconds)
            throws DeviceBaseException {
        if (waitSeconds != null && (waitSeconds < 0 || waitSeconds > MAX_WAIT_SECONDS)) {
            throw new ValidationException(
                    "wait_seconds must be between 0 and " + MAX_WAIT_SECONDS + ", got " + waitSeconds);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        if (name != null) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                throw new ValidationException("name cannot be empty");
            }
            if (trimmed.length() > NAME_MAX_LENGTH) {
                throw new ValidationException(
                        "name is longer than " + NAME_MAX_LENGTH + " characters");
            }
            body.put("name", trimmed);
        }
        if (windowSize != null) {
            body.put("window_size", validatedWindowSize(windowSize));
        }
        if (waitSeconds != null) {
            body.put("wait_seconds", waitSeconds);
        }

        int wait = waitSeconds == null ? DEFAULT_WAIT_SECONDS : waitSeconds;
        JsonNode response = http.request(
                "POST", "/v1/browser/create", body, CREATE_TIMEOUT_SLACK.plusSeconds(wait));
        return http.mapper().convertValue(dataOf(response), CloudBrowserCreateResult.class);
    }

    /**
     * Asks the platform for a new cloud browser, with the server naming it and
     * waiting its default.
     *
     * @return the creation result
     * @throws DeviceBaseException if the request fails
     */
    public CloudBrowserCreateResult create() throws DeviceBaseException {
        return create(null, null, null);
    }

    // --- Deletion ---

    /**
     * Destroys a cloud browser and its profile. Irreversible.
     *
     * <p>Not the same as {@link BrowserApi#close(String)}: that only stops the
     * CDP engine of a browser you still have.</p>
     *
     * <p>The call does not wait for the machine. The platform queues the reap and
     * the node collects it on its next heartbeat, so this succeeds even while the
     * node is offline.</p>
     *
     * @param identifier the platform serialno, or the {@code serial} from creation
     * @throws ValidationException if the identifier is blank
     * @throws DeviceBaseException if the request fails — 404 when it does not
     *     exist, is not yours, or was already deleted; 409 when the device is not
     *     a cloud browser
     */
    public void delete(String identifier) throws DeviceBaseException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new ValidationException("identifier cannot be empty");
        }
        http.request("DELETE", cloudBrowserPath(identifier), null, null);
    }

    // --- Inspection ---

    /**
     * Reports whether the browser has registered itself yet.
     *
     * <p>The device row appears only when the node registers the browser, a
     * moment after creation returns, so this is how the wait is spent. "Not up
     * yet" is a normal answer — {@link CloudBrowserStatus#isRegistered()} is
     * false and nothing is thrown — so polling this in a loop is quiet.</p>
     *
     * @param identifier the {@code serial} from creation, or the serialno once
     *     it is up; either resolves
     * @return the registration status
     * @throws ValidationException if the identifier is blank
     * @throws DeviceBaseException if the request fails
     */
    public CloudBrowserStatus status(String identifier) throws DeviceBaseException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new ValidationException("identifier cannot be empty");
        }
        JsonNode response =
                http.request("GET", cloudBrowserPath(identifier) + "/status", null, null);
        return http.mapper().convertValue(dataOf(response), CloudBrowserStatus.class);
    }

    /**
     * Returns how many cloud browsers this account may still create.
     *
     * <p>Counted from the same source as the check creation performs, so the two
     * cannot disagree. Only cloud browsers count — browsers attached by hand are
     * not part of the allowance.</p>
     *
     * @return the quota reading
     * @throws DeviceBaseException if the request fails
     */
    public CloudBrowserQuota quota() throws DeviceBaseException {
        JsonNode response = http.request("GET", "/v1/browser/quota", null, null);
        return http.mapper().convertValue(dataOf(response), CloudBrowserQuota.class);
    }

    /** Rejects a window size the node would not parse, before the round trip. */
    private static String validatedWindowSize(String windowSize) {
        String size = windowSize.trim();
        int separator = size.indexOf('x');
        String width = separator < 0 ? "" : size.substring(0, separator);
        String height = separator < 0 ? "" : size.substring(separator + 1);
        if (separator < 0 || !isBoundedDigits(width) || !isBoundedDigits(height)) {
            throw new ValidationException(
                    "window_size must look like 1366x768, got \"" + windowSize + "\"");
        }
        return size;
    }

    /** One to five digits — the shape the node's parser accepts. */
    private static boolean isBoundedDigits(String value) {
        if (value.isEmpty() || value.length() > 5) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
