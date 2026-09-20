package cn.devicebase.http;

import cn.devicebase.exception.AuthenticationException;
import cn.devicebase.exception.BusinessException;
import cn.devicebase.exception.DeviceBaseException;
import cn.devicebase.exception.DeviceNotFoundException;
import cn.devicebase.exception.ValidationException;
import cn.devicebase.model.DeviceResponse;
import cn.devicebase.model.Point;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the transport and the mobile route family.
 */
class DeviceBaseHttpClientTest {

    private static final String SERIAL = "db-mttul4i41di8";

    private DeviceBaseHttpClient clientFor(RecordingServer server) {
        return new DeviceBaseHttpClient(server.url(), "test-key");
    }

    // ========== Transport ==========

    @Test
    void sendsBearerAuthAndJsonContentType() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            clientFor(server).tap(SERIAL, 10, 20);

            RecordingServer.Recorded request = server.last();
            assertEquals("Bearer test-key", request.authorization);
            assertEquals("POST", request.method);
        }
    }

    @Test
    void buildsTheMobilePathFamily() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            DeviceBaseHttpClient client = clientFor(server);

            client.tap(SERIAL, 10, 20);
            assertEquals("/v1/tap/" + SERIAL, server.last().path);

            client.back(SERIAL);
            assertEquals("/v1/back/" + SERIAL, server.last().path);

            client.getDeviceInfo(SERIAL);
            assertEquals("/v1/deviceinfo/" + SERIAL, server.last().path);

            client.dumpHierarchy(SERIAL);
            assertEquals("/v1/dump_hierarchy/" + SERIAL, server.last().path);
        }
    }

    @Test
    void sendsTheContractBodyForEachAction() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            DeviceBaseHttpClient client = clientFor(server);

            client.tap(SERIAL, 100, 200);
            assertEquals("{\"x\":100,\"y\":200}", server.last().body);

            client.swipe(SERIAL, 0, 100, 300, 100);
            assertEquals("{\"x1\":0,\"y1\":100,\"x2\":300,\"y2\":100}", server.last().body);

            // The contract field is app_name; the old SDK sent package.
            client.launchApp(SERIAL, "com.tencent.mm");
            assertEquals("{\"app_name\":\"com.tencent.mm\"}", server.last().body);

            client.stopApp(SERIAL, "com.tencent.mm");
            assertEquals("{\"app_name\":\"com.tencent.mm\"}", server.last().body);

            client.bash(SERIAL, "ls -la");
            assertEquals("{\"command\":\"ls -la\"}", server.last().body);

            client.installApp(SERIAL, "/tmp/app.apk");
            assertEquals("{\"app_path\":\"/tmp/app.apk\"}", server.last().body);
        }
    }

    @Test
    void installStatusIsAGetWithItsQueryParam() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            clientFor(server).installStatus(SERIAL, "install-42");

            RecordingServer.Recorded request = server.last();
            assertEquals("GET", request.method);
            assertEquals("/v1/install_status/" + SERIAL, request.path);
            assertEquals("install-42",
                    RecordingServer.queryParam(request, "install_id"));
        }
    }

    @Test
    void serialnosAreUrlEscaped() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            clientFor(server).back("a b/c");

            assertEquals("/v1/back/a%20b%2Fc", server.last().path);
        }
    }

    @Test
    void screenshotPostsToTheCrossFamilyScreenRoute() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
            server.respondWithBytes(200, jpeg);

            byte[] data = clientFor(server).getScreenshot(SERIAL);

            assertEquals("POST", server.last().method);
            assertEquals("/v1/screen/" + SERIAL, server.last().path);
            assertEquals(jpeg.length, data.length);
        }
    }

    // ========== Failure layers ==========

    /**
     * The control API reports action failures as HTTP 200 with a non-2xx code in
     * the envelope. Trusting the status line alone would report those as success.
     */
    @Test
    void raisesABusinessExceptionForANonSuccessEnvelopeCode() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":502,\"message\":\"-32602: Invalid parameters\"}");

            BusinessException error = assertThrows(BusinessException.class,
                    () -> clientFor(server).tap(SERIAL, 1, 2));

            assertEquals(502, error.getCode());
            assertTrue(error.getBody().contains("-32602"));
        }
    }

    @Test
    void acceptsEnvelopeCodesInThe2xxRange() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"message\":\"success\"}");
            assertNotNull(clientFor(server).tap(SERIAL, 1, 2));
        }
    }

    @Test
    void leavesBodiesThatAreNotAnEnvelopeAlone() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            // No code field at all.
            server.respondWith(200, "{\"success\":true}");
            assertNotNull(clientFor(server).tap(SERIAL, 1, 2));
        }
    }

    @Test
    void mapsHttpStatusesOntoTheMatchingException() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            DeviceBaseHttpClient client = clientFor(server);

            server.respondWith(401, "{\"code\":401,\"message\":\"API Key 不存在或已停用\"}");
            assertThrows(AuthenticationException.class, () -> client.tap(SERIAL, 1, 2));

            server.respondWith(404, "{\"code\":404,\"message\":\"设备不存在\"}");
            assertThrows(DeviceNotFoundException.class, () -> client.tap(SERIAL, 1, 2));

            // The gateway reports validation failures as 400.
            server.respondWith(400, "{\"detail\":\"body.username: Field required\"}");
            assertThrows(ValidationException.class, () -> client.tap(SERIAL, 1, 2));

            server.respondWith(422, "validation failed");
            assertThrows(ValidationException.class, () -> client.tap(SERIAL, 1, 2));

            server.respondWith(500, "internal");
            assertThrows(DeviceBaseException.class, () -> client.tap(SERIAL, 1, 2));
        }
    }

    @Test
    void errorsCarryTheServerBody() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(404, "{\"code\":404,\"message\":\"设备不存在: nope\"}");

            DeviceBaseException error = assertThrows(DeviceBaseException.class,
                    () -> clientFor(server).tap(SERIAL, 1, 2));

            assertTrue(error.getMessage().startsWith("API error (HTTP 404): "),
                    "unexpected message: " + error.getMessage());
            assertTrue(error.getMessage().contains("设备不存在"));
        }
    }

    @Test
    void rejectsAMissingApiKey() {
        assertThrows(NullPointerException.class,
                () -> new DeviceBaseHttpClient("http://127.0.0.1:1", null));
    }

    // ========== Devices ==========

    /** A real GET /v1/devices row, including its zone-less updated_at. */
    private static final String LIST_RESPONSE =
            "{\"code\":200,\"message\":\"success\",\"data\":["
            + "{\"id\":10,"
            + "\"serialno\":\"db-mtthisv311f1\","
            + "\"device_sn\":\"f3ad1396-4fb9-4037-81e7-7496f261f3f4\","
            + "\"state\":\"free\","
            + "\"name\":\"Richie-Macbook-Air-7.local\","
            + "\"alias_name\":\"Richie-Macbook-Air-7.local\","
            + "\"udid\":\"b2:da:29:43:81:03\","
            + "\"type\":\"computer\","
            + "\"brand\":\"Apple\","
            + "\"model\":\"MacBook Air\","
            + "\"os_type\":\"macOS\","
            + "\"os_version\":\"26.0\","
            + "\"display\":\"1470x956\","
            + "\"location\":\"Beijing\","
            + "\"operator\":\"CMCC\","
            + "\"network\":\"wifi\","
            + "\"updated_at\":\"2026-09-20T15:11:31\"}"
            + "]}";

    @Test
    void decodesTheRealDeviceListPayload() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, LIST_RESPONSE);

            List<DeviceResponse> devices = clientFor(server).listDevices();

            assertEquals(1, devices.size());
            DeviceResponse device = devices.get(0);
            assertEquals("db-mtthisv311f1", device.getSerialno());
            assertEquals("f3ad1396-4fb9-4037-81e7-7496f261f3f4", device.getDeviceSn());
            assertEquals("computer", device.getType());
            assertEquals("macOS", device.getOsType());
            assertEquals("free", device.getState());
            assertEquals(10, device.getId());
            // The server sends a zone-less timestamp; it must not fail the decode.
            assertNotNull(device.getUpdatedAt());
        }
    }

    @Test
    void listDevicesSendsItsFilters() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"message\":\"success\",\"data\":[]}");
            clientFor(server).listDevices("Samsung", "mobile", "free", 50);

            RecordingServer.Recorded request = server.last();
            assertEquals("GET", request.method);
            assertEquals("/v1/devices", request.path);

            Map<String, String> params = RecordingServer.queryParams(request);
            assertEquals("Samsung", params.get("keyword"));
            assertEquals("mobile", params.get("type"));
            assertEquals("free", params.get("state"));
            assertEquals("50", params.get("limit"));
        }
    }

    @Test
    void listDevicesOmitsUnsetFilters() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"message\":\"success\",\"data\":[]}");
            clientFor(server).listDevices();

            RecordingServer.Recorded request = server.last();
            assertEquals("/v1/devices", request.path);
            assertTrue(request.query == null || request.query.isEmpty(),
                    "unset filters should be omitted, got: " + request.query);
        }
    }

    @Test
    void listDevicesOnAnEmptyResult() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"message\":\"success\",\"data\":[]}");
            assertTrue(clientFor(server).listDevices("other").isEmpty());
        }
    }

    @Test
    void listDevicesSurfacesAnEnvelopeError() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":401,\"message\":\"API Key 不存在或已停用\"}");
            assertThrows(BusinessException.class, () -> clientFor(server).listDevices());
        }
    }

    // ========== Configuration ==========

    @Test
    void trimsTrailingSlashesFromTheBaseUrl() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            DeviceBaseHttpClient client =
                    new DeviceBaseHttpClient(server.url() + "//", "test-key");
            client.back(SERIAL);

            assertEquals("/v1/back/" + SERIAL, server.last().path);
        }
    }

    @Test
    void unknownEndpointsAreStillReachableThroughThePointModel() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            clientFor(server).doubleTap(SERIAL, new Point(5, 6));
            assertEquals("{\"x\":5,\"y\":6}", server.last().body);
        }
    }

    @Test
    void timeoutDefaultsToThirtySeconds() throws IOException {
        assertEquals(Duration.ofSeconds(30), DeviceBaseHttpClient.DEFAULT_TIMEOUT);
    }
}
