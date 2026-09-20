package cn.devicebase.http;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the browser route family — {@code /api/browser/{serialno}/{action}}.
 *
 * <p>The exceptions the API declares are unchecked, so each call can be passed
 * as a plain lambda and the assertions live in one place.</p>
 */
class BrowserApiTest {

    private static final String SERIAL = "db-mtsi49bf0mqb";
    private static final String BASE = "/api/browser/" + SERIAL + "/";

    /** Asserts the request a call produced. */
    private void assertCall(RecordingServer server, String method, String path, String body,
            Runnable call) {
        call.run();

        RecordingServer.Recorded request = server.last();
        assertEquals(method, request.method, path);
        assertEquals(path, request.path);
        if (body == null) {
            assertTrue(request.body == null || request.body.isEmpty(),
                    path + ": expected no body, got " + request.body);
        } else {
            assertEquals(body, request.body, path);
        }
    }

    @Test
    void postActionsUseTheContractPathAndBody() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            BrowserApi browser = new DeviceBaseHttpClient(server.url(), "k").browser();

            assertCall(server, "POST", BASE + "navigate",
                    "{\"url\":\"https://example.com\"}",
                    () -> browser.navigate(SERIAL, "https://example.com"));
            assertCall(server, "POST", BASE + "refresh", null,
                    () -> browser.refresh(SERIAL));
            assertCall(server, "POST", BASE + "go_back", null,
                    () -> browser.goBack(SERIAL));
            assertCall(server, "POST", BASE + "go_forward", null,
                    () -> browser.goForward(SERIAL));
            assertCall(server, "POST", BASE + "input", "{\"text\":\"hello\"}",
                    () -> browser.input(SERIAL, "hello"));
            assertCall(server, "POST", BASE + "click", "{\"selector\":\"button#submit\"}",
                    () -> browser.click(SERIAL, "button#submit"));
            assertCall(server, "POST", BASE + "fill",
                    "{\"selector\":\"#search\",\"value\":\"devicebase\"}",
                    () -> browser.fill(SERIAL, "#search", "devicebase"));
            assertCall(server, "POST", BASE + "select",
                    "{\"selector\":\"#country\",\"value\":\"CN\"}",
                    () -> browser.select(SERIAL, "#country", "CN"));
            assertCall(server, "POST", BASE + "execute", "{\"script\":\"document.title\"}",
                    () -> browser.execute(SERIAL, "document.title"));
            assertCall(server, "POST", BASE + "hotkey", "{\"keys\":[\"Meta\",\"a\"]}",
                    () -> browser.hotkey(SERIAL, List.of("Meta", "a")));
            assertCall(server, "POST", BASE + "launch", null,
                    () -> browser.launch(SERIAL));
            assertCall(server, "POST", BASE + "close", null,
                    () -> browser.close(SERIAL));
        }
    }

    @Test
    void tabActionsUseTheNestedPath() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            BrowserApi browser = new DeviceBaseHttpClient(server.url(), "k").browser();

            assertCall(server, "POST", BASE + "tab/open", "{\"url\":\"https://example.com\"}",
                    () -> browser.tabOpen(SERIAL, "https://example.com"));
            assertCall(server, "POST", BASE + "tab/close", "{\"tab_id\":\"tab-7\"}",
                    () -> browser.tabClose(SERIAL, "tab-7"));
            assertCall(server, "POST", BASE + "tab/close_all", null,
                    () -> browser.tabCloseAll(SERIAL));
            assertCall(server, "POST", BASE + "tab/switch", "{\"tab_id\":\"tab-7\"}",
                    () -> browser.tabSwitch(SERIAL, "tab-7"));
        }
    }

    /**
     * The read-only actions are GETs and carry their selectors as query
     * parameters, not in a body.
     */
    @Test
    void readOnlyActionsAreGetsWithQueryParameters() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            BrowserApi browser = new DeviceBaseHttpClient(server.url(), "k").browser();

            assertCall(server, "GET", BASE + "state", null, () -> browser.state(SERIAL));
            assertCall(server, "GET", BASE + "tabs", null, () -> browser.tabs(SERIAL));

            assertCall(server, "GET", BASE + "text", null, () -> browser.text(SERIAL, "#a"));
            assertEquals("#a", RecordingServer.queryParam(server.last(), "selector"));

            assertCall(server, "GET", BASE + "exists", null, () -> browser.exists(SERIAL, ".m"));
            assertEquals(".m", RecordingServer.queryParam(server.last(), "selector"));

            assertCall(server, "GET", BASE + "attribute", null,
                    () -> browser.attribute(SERIAL, "a.logo", "href"));
            RecordingServer.Recorded request = server.last();
            assertEquals("a.logo", RecordingServer.queryParam(request, "selector"));
            assertEquals("href", RecordingServer.queryParam(request, "attribute"));
        }
    }

    @Test
    void selectorsAreUrlEncodedInTheQuery() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            BrowserApi browser = new DeviceBaseHttpClient(server.url(), "k").browser();

            browser.text(SERIAL, "a.link[name=x]");

            Map<String, String> params = RecordingServer.queryParams(server.last());
            // Recorded already decoded by the server, so this proves the value
            // survived the round trip intact.
            assertEquals("a.link[name=x]", params.get("selector"));
        }
    }

    @Test
    void serialnosAreUrlEscaped() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            new DeviceBaseHttpClient(server.url(), "k").browser().state("a b");

            assertEquals("/api/browser/a%20b/state", server.last().path);
        }
    }
}
