package cn.devicebase.http;

import cn.devicebase.exception.DeviceBaseException;
import cn.devicebase.model.OperationResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

import static cn.devicebase.http.DeviceBaseHttpClient.orderedBody;

/**
 * Browser (Chrome / Chromium / Edge over CDP) platform API.
 *
 * <p>Path family: {@code POST/GET /api/browser/{serialno}/{action...}}.</p>
 *
 * <p>The serialno is the platform serialno of a registered browser device — the
 * {@code serialno} field from
 * {@link DeviceBaseHttpClient#listDevices(String) listDevices("browser")}.
 * Selectors are CSS selectors. The read-only actions ({@code state},
 * {@code tabs}, {@code text}, {@code attribute}, {@code exists}) carry no body;
 * selectors travel as query parameters.</p>
 *
 * <p>Obtain an instance with {@link DeviceBaseHttpClient#browser()}.</p>
 *
 * @author Richie
 */
public class BrowserApi {

    private final DeviceBaseHttpClient http;

    BrowserApi(DeviceBaseHttpClient http) {
        this.http = http;
    }

    private Map<String, Object> toMap(com.fasterxml.jackson.databind.JsonNode response) {
        return http.mapper().convertValue(response, new TypeReference<>() { });
    }

    private OperationResult post(String serialno, String action, Map<String, ?> body)
            throws DeviceBaseException {
        return OperationResult.fromMap(
                toMap(http.request("POST", DeviceBaseHttpClient.browserPath(serialno, action),
                        body, null)));
    }

    private OperationResult get(String serialno, String action, Map<String, ?> params)
            throws DeviceBaseException {
        String path = DeviceBaseHttpClient.browserPath(serialno, action)
                + DeviceBaseHttpClient.buildQuery(params);
        return OperationResult.fromMap(toMap(http.request("GET", path, null, null)));
    }

    // --- Navigation ---

    /**
     * Navigates the current tab to a URL.
     *
     * @param serialno the browser device serialno
     * @param url the destination URL
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult navigate(String serialno, String url) throws DeviceBaseException {
        return post(serialno, "navigate", orderedBody("url", url));
    }

    /**
     * Reloads the current page.
     *
     * @param serialno the browser device serialno
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult refresh(String serialno) throws DeviceBaseException {
        return post(serialno, "refresh", null);
    }

    /**
     * Navigates back in the browser history.
     *
     * @param serialno the browser device serialno
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult goBack(String serialno) throws DeviceBaseException {
        return post(serialno, "go_back", null);
    }

    /**
     * Navigates forward in the browser history.
     *
     * @param serialno the browser device serialno
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult goForward(String serialno) throws DeviceBaseException {
        return post(serialno, "go_forward", null);
    }

    /**
     * Inserts text into the focused page element via CDP
     * {@code Input.insertText}, which is reliable for CJK unlike synthesised
     * key events.
     *
     * @param serialno the browser device serialno
     * @param text the text to insert
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult input(String serialno, String text) throws DeviceBaseException {
        return post(serialno, "input", orderedBody("text", text));
    }

    // --- DOM ---

    /**
     * Clicks the element matching a CSS selector.
     *
     * @param serialno the browser device serialno
     * @param selector a CSS selector
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult click(String serialno, String selector) throws DeviceBaseException {
        return post(serialno, "click", orderedBody("selector", selector));
    }

    /**
     * Clears an input and types a value into it.
     *
     * @param serialno the browser device serialno
     * @param selector a CSS selector
     * @param value the value to type
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult fill(String serialno, String selector, String value)
            throws DeviceBaseException {
        return post(serialno, "fill", orderedBody("selector", selector, "value", value));
    }

    /**
     * Picks an option in a dropdown.
     *
     * @param serialno the browser device serialno
     * @param selector a CSS selector
     * @param value the option value
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult select(String serialno, String selector, String value)
            throws DeviceBaseException {
        return post(serialno, "select", orderedBody("selector", selector, "value", value));
    }

    /**
     * Gets an element's text content.
     *
     * @param serialno the browser device serialno
     * @param selector a CSS selector
     * @return OperationResult whose data carries the text
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult text(String serialno, String selector) throws DeviceBaseException {
        return get(serialno, "text", orderedBody("selector", selector));
    }

    /**
     * Gets one attribute of an element.
     *
     * @param serialno the browser device serialno
     * @param selector a CSS selector
     * @param attribute the attribute name
     * @return OperationResult whose data carries the attribute value
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult attribute(String serialno, String selector, String attribute)
            throws DeviceBaseException {
        return get(serialno, "attribute", orderedBody("selector", selector, "attribute", attribute));
    }

    /**
     * Reports whether an element is present.
     *
     * @param serialno the browser device serialno
     * @param selector a CSS selector
     * @return OperationResult indicating existence
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult exists(String serialno, String selector) throws DeviceBaseException {
        return get(serialno, "exists", orderedBody("selector", selector));
    }

    /**
     * Evaluates JavaScript in the page.
     *
     * <p><b>Danger tier:</b> the script runs with the page's own privileges, the
     * same reach as shell access to the browser profile.</p>
     *
     * @param serialno the browser device serialno
     * @param script the JavaScript source
     * @return OperationResult whose data carries the script result
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult execute(String serialno, String script) throws DeviceBaseException {
        return post(serialno, "execute", orderedBody("script", script));
    }

    /**
     * Presses the given keys together, e.g. {@code List.of("Meta", "a")}.
     *
     * <p>Editing shortcuts (select-all, cut, copy, undo, redo) act on the page.
     * Browser-chrome shortcuts such as {@code Control+t} are <b>not
     * reachable</b> — CDP drives the page, not the browser UI.</p>
     *
     * @param serialno the browser device serialno
     * @param keys the keys to press together
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult hotkey(String serialno, List<String> keys) throws DeviceBaseException {
        return post(serialno, "hotkey", orderedBody("keys", keys));
    }

    // --- Tabs and state ---

    /**
     * Gets the current URL, title, viewport and tab count.
     *
     * @param serialno the browser device serialno
     * @return OperationResult whose data carries the state
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult state(String serialno) throws DeviceBaseException {
        return get(serialno, "state", null);
    }

    /**
     * Lists the open tabs.
     *
     * @param serialno the browser device serialno
     * @return OperationResult whose data carries the tabs
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult tabs(String serialno) throws DeviceBaseException {
        return get(serialno, "tabs", null);
    }

    /**
     * Opens a new tab at a URL.
     *
     * @param serialno the browser device serialno
     * @param url the URL to open
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult tabOpen(String serialno, String url) throws DeviceBaseException {
        return post(serialno, "tab/open", orderedBody("url", url));
    }

    /**
     * Closes one tab.
     *
     * @param serialno the browser device serialno
     * @param tabId the tab id from {@link #tabs}
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult tabClose(String serialno, String tabId) throws DeviceBaseException {
        return post(serialno, "tab/close", orderedBody("tab_id", tabId));
    }

    /**
     * Closes every tab.
     *
     * @param serialno the browser device serialno
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult tabCloseAll(String serialno) throws DeviceBaseException {
        return post(serialno, "tab/close_all", null);
    }

    /**
     * Focuses one tab.
     *
     * @param serialno the browser device serialno
     * @param tabId the tab id from {@link #tabs}
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult tabSwitch(String serialno, String tabId) throws DeviceBaseException {
        return post(serialno, "tab/switch", orderedBody("tab_id", tabId));
    }

    // --- Lifecycle ---

    /**
     * Starts the browser / CDP endpoint.
     *
     * @param serialno the browser device serialno
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult launch(String serialno) throws DeviceBaseException {
        return post(serialno, "launch", null);
    }

    /**
     * Stops the browser / CDP endpoint.
     *
     * @param serialno the browser device serialno
     * @return OperationResult indicating success or failure
     * @throws DeviceBaseException if the request fails
     */
    public OperationResult close(String serialno) throws DeviceBaseException {
        return post(serialno, "close", null);
    }
}
