package cn.devicebase.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the computer route family — {@code /api/computer/{serialno}/{action}}.
 */
class ComputerApiTest {

    private static final String SERIAL = "db-mtthisv311f1";
    private static final String BASE = "/api/computer/" + SERIAL + "/";

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
            ComputerApi computer = new DeviceBaseHttpClient(server.url(), "k").computer();

            assertCall(server, "POST", BASE + "double_click", "{\"x\":10,\"y\":20}",
                    () -> computer.doubleClick(SERIAL, 10, 20));
            assertCall(server, "POST", BASE + "move", "{\"x\":100,\"y\":100}",
                    () -> computer.move(SERIAL, 100, 100));
            assertCall(server, "POST", BASE + "drag",
                    "{\"x1\":100,\"y1\":100,\"x2\":800,\"y2\":600}",
                    () -> computer.drag(SERIAL, 100, 100, 800, 600));
            assertCall(server, "POST", BASE + "type_text", "{\"text\":\"hello\"}",
                    () -> computer.typeText(SERIAL, "hello"));
            assertCall(server, "POST", BASE + "press", "{\"key\":\"Enter\"}",
                    () -> computer.press(SERIAL, "Enter"));
            assertCall(server, "POST", BASE + "hotkey",
                    "{\"keys\":[\"Control\",\"Shift\",\"Escape\"]}",
                    () -> computer.hotkey(SERIAL, List.of("Control", "Shift", "Escape")));
            assertCall(server, "POST", BASE + "launch_app",
                    "{\"app_name\":\"Visual Studio Code\"}",
                    () -> computer.launchApp(SERIAL, "Visual Studio Code"));
        }
    }

    /**
     * The optional fields are omitted when unset, so the server applies its own
     * defaults rather than receiving an explicit zero.
     */
    @Test
    void optionalFieldsAreOmittedWhenUnset() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            ComputerApi computer = new DeviceBaseHttpClient(server.url(), "k").computer();

            // No button means the server defaults to left.
            assertCall(server, "POST", BASE + "click", "{\"x\":640,\"y\":360}",
                    () -> computer.click(SERIAL, 640, 360, null));
            // So does an empty one.
            assertCall(server, "POST", BASE + "click", "{\"x\":640,\"y\":360}",
                    () -> computer.click(SERIAL, 640, 360, ""));

            assertCall(server, "POST", BASE + "long_click", "{\"x\":1,\"y\":2}",
                    () -> computer.longClick(SERIAL, 1, 2, 0));

            assertCall(server, "POST", BASE + "scroll", "{\"direction\":\"down\"}",
                    () -> computer.scroll(SERIAL, "down", 0));

            // A zero timeout must omit the field: sending 0 would ask the server
            // for a zero-second budget instead of its 120s default.
            assertCall(server, "POST", BASE + "bash", "{\"command\":\"ls\"}",
                    () -> computer.bash(SERIAL, "ls", 0));
        }
    }

    @Test
    void optionalFieldsAreSentWhenSet() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            ComputerApi computer = new DeviceBaseHttpClient(server.url(), "k").computer();

            assertCall(server, "POST", BASE + "click",
                    "{\"x\":640,\"y\":360,\"button\":\"right\"}",
                    () -> computer.click(SERIAL, 640, 360, ComputerApi.BUTTON_RIGHT));

            assertCall(server, "POST", BASE + "long_click",
                    "{\"x\":1,\"y\":2,\"duration\":3}",
                    () -> computer.longClick(SERIAL, 1, 2, 3));

            assertCall(server, "POST", BASE + "scroll",
                    "{\"direction\":\"down\",\"amount\":5}",
                    () -> computer.scroll(SERIAL, "down", 5));

            assertCall(server, "POST", BASE + "bash",
                    "{\"command\":\"ls\",\"timeout\":30}",
                    () -> computer.bash(SERIAL, "ls", 30));
        }
    }

    @Test
    void readOnlyActionsAreGets() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            ComputerApi computer = new DeviceBaseHttpClient(server.url(), "k").computer();

            assertCall(server, "GET", BASE + "position", null,
                    () -> computer.position(SERIAL));
            assertCall(server, "GET", BASE + "screen_size", null,
                    () -> computer.screenSize(SERIAL));
            assertCall(server, "GET", BASE + "permissions", null,
                    () -> computer.permissions(SERIAL));
        }
    }

    /** The SDK takes milliseconds, like the CLI; the wire field is seconds. */
    @Test
    void waitConvertsMillisecondsToSeconds() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            ComputerApi computer = new DeviceBaseHttpClient(server.url(), "k").computer();

            assertCall(server, "POST", BASE + "wait", "{\"seconds\":2.5}",
                    () -> computer.waitFor(SERIAL, 2500));
        }
    }

    @Test
    void waitTimeoutOutlastsTheRequestedWait() {
        assertEquals(Duration.ofSeconds(15), ComputerApi.waitTimeout(0));
        assertEquals(Duration.ofMillis(2500).plusSeconds(15), ComputerApi.waitTimeout(2500));
        // A negative budget must not produce a negative deadline.
        assertEquals(Duration.ofSeconds(15), ComputerApi.waitTimeout(-5));
    }

    @Test
    void bashTimeoutOutlastsTheRequestedTimeout() {
        // An omitted timeout still has to outlast the server's 120s default.
        assertEquals(Duration.ofSeconds(135), ComputerApi.bashTimeout(0));
        assertEquals(Duration.ofSeconds(135), ComputerApi.bashTimeout(-1));
        assertEquals(Duration.ofSeconds(45), ComputerApi.bashTimeout(30));
        assertEquals(Duration.ofSeconds(615), ComputerApi.bashTimeout(600));
    }

    @Test
    void serialnosAreUrlEscaped() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            new DeviceBaseHttpClient(server.url(), "k").computer().position("a/b");

            assertEquals("/api/computer/a%2Fb/position", server.last().path);
        }
    }
}
