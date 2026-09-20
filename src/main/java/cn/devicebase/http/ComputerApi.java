package cn.devicebase.http;

import cn.devicebase.exception.DeviceBaseException;
import cn.devicebase.model.OperationResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.devicebase.http.DeviceBaseHttpClient.orderedBody;

/**
 * Computer (desktop) platform API.
 *
 * <p>Path family: {@code POST/GET /api/computer/{serialno}/{action}}.</p>
 *
 * <p>The serialno is the platform serialno of a registered computer device — the
 * {@code serialno} field from
 * {@link DeviceBaseHttpClient#listDevices(String) listDevices("computer")}.
 * Coordinates are absolute screen pixels. The read-only actions
 * ({@code position}, {@code screen_size}, {@code permissions}) carry no
 * body.</p>
 *
 * <p>Obtain an instance with {@link DeviceBaseHttpClient#computer()}.</p>
 *
 * @author Richie
 */
public class ComputerApi {

    /** The server's own default bash timeout, used only to size the client deadline. */
    public static final int DEFAULT_BASH_TIMEOUT_SECONDS = 120;

    /**
     * Headroom added on top of a blocking action's own budget, covering connect
     * and body-read overhead.
     */
    public static final Duration ACTION_TIMEOUT_MARGIN = Duration.ofSeconds(15);

    /** Mouse buttons accepted by {@link #click}. */
    public static final String BUTTON_LEFT = "left";

    /** @see #BUTTON_LEFT */
    public static final String BUTTON_RIGHT = "right";

    /** @see #BUTTON_LEFT */
    public static final String BUTTON_MIDDLE = "middle";

    /** Scroll directions accepted by {@link #scroll}. */
    public static final String SCROLL_UP = "up";

    /** @see #SCROLL_UP */
    public static final String SCROLL_DOWN = "down";

    /** @see #SCROLL_UP */
    public static final String SCROLL_LEFT = "left";

    /** @see #SCROLL_UP */
    public static final String SCROLL_RIGHT = "right";

    private final DeviceBaseHttpClient http;

    ComputerApi(DeviceBaseHttpClient http) {
        this.http = http;
    }

    /**
     * Bounds the HTTP deadline for a bash call.
     *
     * <p>It must exceed the requested command timeout, or the client would abort
     * a command the server is still running and report a transport error.</p>
     *
     * @param timeoutSeconds the requested command budget; 0 means the server default
     * @return the client deadline
     */
    public static Duration bashTimeout(int timeoutSeconds) {
        int seconds = timeoutSeconds > 0 ? timeoutSeconds : DEFAULT_BASH_TIMEOUT_SECONDS;
        return Duration.ofSeconds(seconds).plus(ACTION_TIMEOUT_MARGIN);
    }

    /**
     * The same idea for {@link #wait}, whose budget arrives as milliseconds.
     *
     * @param milliseconds the requested wait
     * @return the client deadline
     */
    public static Duration waitTimeout(int milliseconds) {
        return Duration.ofMillis(Math.max(0, milliseconds)).plus(ACTION_TIMEOUT_MARGIN);
    }

    private Map<String, Object> toMap(com.fasterxml.jackson.databind.JsonNode response) {
        return http.mapper().convertValue(response, new TypeReference<>() { });
    }

    private OperationResult post(String serialno, String action, Map<String, ?> body, Duration timeout)
            throws DeviceBaseException {
        return OperationResult.fromMap(
                toMap(http.request("POST", DeviceBaseHttpClient.computerPath(serialno, action),
                        body, timeout)));
    }

    private OperationResult get(String serialno, String action) throws DeviceBaseException {
        return OperationResult.fromMap(
                toMap(http.request("GET", DeviceBaseHttpClient.computerPath(serialno, action),
                        null, null)));
    }

    // --- Mouse ---

    /**
     * Clicks at absolute screen coordinates.
     *
     * @param serialno the computer device serialno
     * @param x horizontal screen coordinate
     * @param y vertical screen coordinate
     * @param button {@code left}/{@code right}/{@code middle}, or null for the
     *     server default ({@code left})
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult click(String serialno, int x, int y, String button)
            throws DeviceBaseException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("x", x);
        body.put("y", y);
        if (button != null && !button.isEmpty()) {
            body.put("button", button);
        }
        return post(serialno, "click", body, null);
    }

    /**
     * Double clicks at absolute screen coordinates (left button).
     *
     * @param serialno the computer device serialno
     * @param x horizontal screen coordinate
     * @param y vertical screen coordinate
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult doubleClick(String serialno, int x, int y) throws DeviceBaseException {
        return post(serialno, "double_click", orderedBody("x", x, "y", y), null);
    }

    /**
     * Presses and holds the left button at the coordinates.
     *
     * @param serialno the computer device serialno
     * @param x horizontal screen coordinate
     * @param y vertical screen coordinate
     * @param durationSeconds the hold duration in seconds; 0 leaves the driver default
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult longClick(String serialno, int x, int y, int durationSeconds)
            throws DeviceBaseException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("x", x);
        body.put("y", y);
        if (durationSeconds > 0) {
            body.put("duration", durationSeconds);
        }
        return post(serialno, "long_click", body, null);
    }

    /**
     * Moves the mouse to absolute screen coordinates without clicking.
     *
     * @param serialno the computer device serialno
     * @param x horizontal screen coordinate
     * @param y vertical screen coordinate
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult move(String serialno, int x, int y) throws DeviceBaseException {
        return post(serialno, "move", orderedBody("x", x, "y", y), null);
    }

    /**
     * Presses the left button at (x1,y1), moves to (x2,y2) and releases.
     *
     * @param serialno the computer device serialno
     * @param x1 starting X coordinate
     * @param y1 starting Y coordinate
     * @param x2 ending X coordinate
     * @param y2 ending Y coordinate
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult drag(String serialno, int x1, int y1, int x2, int y2)
            throws DeviceBaseException {
        return post(serialno, "drag", orderedBody("x1", x1, "y1", y1, "x2", x2, "y2", y2), null);
    }

    /**
     * Scrolls the mouse wheel.
     *
     * @param serialno the computer device serialno
     * @param direction {@code up}/{@code down}/{@code left}/{@code right}
     * @param amount wheel steps; 0 leaves the driver default
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult scroll(String serialno, String direction, int amount)
            throws DeviceBaseException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("direction", direction);
        if (amount > 0) {
            body.put("amount", amount);
        }
        return post(serialno, "scroll", body, null);
    }

    // --- Keyboard ---

    /**
     * Types text at the current caret of the focused app.
     *
     * @param serialno the computer device serialno
     * @param text the text to type
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult typeText(String serialno, String text) throws DeviceBaseException {
        return post(serialno, "type_text", orderedBody("text", text), null);
    }

    /**
     * Presses a single key, e.g. {@code Enter} or {@code F5}.
     *
     * @param serialno the computer device serialno
     * @param key the key name
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult press(String serialno, String key) throws DeviceBaseException {
        return post(serialno, "press", orderedBody("key", key), null);
    }

    /**
     * Presses the given keys together.
     *
     * @param serialno the computer device serialno
     * @param keys the keys to press together
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult hotkey(String serialno, List<String> keys) throws DeviceBaseException {
        return post(serialno, "hotkey", orderedBody("keys", keys), null);
    }

    // --- System ---

    /**
     * Gets the current mouse position.
     *
     * @param serialno the computer device serialno
     * @return OperationResult whose data carries the position
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult position(String serialno) throws DeviceBaseException {
        return get(serialno, "position");
    }

    /**
     * Gets the primary screen size.
     *
     * @param serialno the computer device serialno
     * @return OperationResult whose data carries the screen size
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult screenSize(String serialno) throws DeviceBaseException {
        return get(serialno, "screen_size");
    }

    /**
     * Gets the desktop-control permission status.
     *
     * @param serialno the computer device serialno
     * @return OperationResult whose data carries the permission status
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult permissions(String serialno) throws DeviceBaseException {
        return get(serialno, "permissions");
    }

    /**
     * Launches a desktop application.
     *
     * @param serialno the computer device serialno
     * @param appName the application name
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult launchApp(String serialno, String appName) throws DeviceBaseException {
        return post(serialno, "launch_app", orderedBody("app_name", appName), null);
    }

    // --- Blocking actions ---

    /**
     * Blocks for the given duration in milliseconds.
     *
     * <p>The deadline is widened to cover the wait itself — the shared 30s
     * default would abort any wait longer than that.</p>
     *
     * @param serialno the computer device serialno
     * @param milliseconds the duration to block for
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult waitFor(String serialno, int milliseconds) throws DeviceBaseException {
        return post(serialno, "wait", orderedBody("seconds", milliseconds / 1000.0),
                waitTimeout(milliseconds));
    }

    /**
     * Runs a shell command on the host machine that owns this device.
     *
     * <p><b>Danger tier:</b> the command runs as the desktop user, unsandboxed,
     * under the platform default shell ({@code /bin/sh} on macOS/Linux,
     * {@code cmd.exe} on Windows), so bash-only syntax such as {@code [[ ]]} may
     * not work. Treat it as shell access.</p>
     *
     * <p>The command's own exit status comes back in the payload as
     * {@code data.exitCode} — a non-zero value is not an API error, so this
     * returns normally for it.</p>
     *
     * @param serialno the computer device serialno
     * @param command the shell command
     * @param timeoutSeconds the server-side command budget in seconds; 0 lets the
     *     server apply its 120s default
     * @return OperationResult whose data carries exitCode/stdout/stderr
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult bash(String serialno, String command, int timeoutSeconds)
            throws DeviceBaseException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("command", command);
        if (timeoutSeconds > 0) {
            body.put("timeout", timeoutSeconds);
        }
        return post(serialno, "bash", body, bashTimeout(timeoutSeconds));
    }
}
