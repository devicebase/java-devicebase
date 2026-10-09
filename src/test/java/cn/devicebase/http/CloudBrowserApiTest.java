package cn.devicebase.http;

import cn.devicebase.exception.DeviceNotFoundException;
import cn.devicebase.exception.ValidationException;
import cn.devicebase.model.CloudBrowserCreateResult;
import cn.devicebase.model.CloudBrowserQuota;
import cn.devicebase.model.CloudBrowserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the cloud browser lifecycle — {@code /v1/browser/*}.
 *
 * <p>A different surface from {@link BrowserApiTest}: these calls address no
 * device, and every one of them but create answers with a typed body rather
 * than an {@code OperationResult}.</p>
 */
class CloudBrowserApiTest {

    private static final String SERIAL = "3f2a1b4c-0000-1111-2222-333344445555";

    private CloudBrowserApi apiFor(RecordingServer server) {
        return new DeviceBaseHttpClient(server.url(), "k").cloudBrowser();
    }

    /** A call that must throw, wrapped so a lambda can carry it. */
    private static Runnable calling(ThrowingCall call) {
        return () -> {
            try {
                call.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    private interface ThrowingCall {
        void run() throws Exception;
    }

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
    void createSendsOnlyTheFieldsItWasGiven() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"data\":{}}");
            CloudBrowserApi api = apiFor(server);

            // Nothing given: an empty object, not a body full of nulls. The mode
            // is not a field at all — a cloud browser is always headless.
            assertCall(server, "POST", "/v1/browser/create", "{}", calling(api::create));

            // Every field, under the wire names.
            assertCall(server, "POST", "/v1/browser/create",
                    "{\"name\":\"my-browser\",\"window_size\":\"1366x768\",\"wait_seconds\":30}",
                    calling(() -> api.create("  my-browser  ", "1366x768", 30)));
        }
    }

    @Test
    void createDecodesTheResult() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            // The registration already happened by the time create answers —
            // which is the point of the wait: the caller gets the key it uses.
            server.respondWith(200, "{\"code\":200,\"data\":{"
                    + "\"serialno\":\"db-mtabc123\",\"device_sn\":\"3f2a-uuid\","
                    + "\"name\":\"Browser-3f2a1b4c\",\"alias_name\":\"my-browser\","
                    + "\"registered\":true}}");

            CloudBrowserCreateResult created = apiFor(server).create("b", null, null);

            assertEquals("db-mtabc123", created.getSerialno());
            assertEquals("3f2a-uuid", created.getDeviceSn());
            // name 是平台的内部身份名；调用方要的是 aliasName（原样、无端口后缀）
            assertEquals("Browser-3f2a1b4c", created.getName());
            assertEquals("my-browser", created.getAliasName());
            assertTrue(created.isRegistered());
        }
    }

    /** A locally rejected argument must not reach the server at all. */
    private void assertNoRequestWasSent(RecordingServer server) {
        assertThrows(AssertionError.class, () -> server.last(),
                "a locally rejected call must not send a request");
    }

    @Test
    void createRejectsBadArgumentsBeforeAnyRequest() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            assertThrows(ValidationException.class, () -> apiFor(server).create("   ", null, null));
            assertThrows(ValidationException.class,
                    () -> apiFor(server).create("x".repeat(101), null, null));
            assertThrows(ValidationException.class,
                    () -> apiFor(server).create(null, "1366*768", null));
            assertThrows(ValidationException.class,
                    () -> apiFor(server).create(null, "136678", null));
            assertThrows(ValidationException.class,
                    () -> apiFor(server).create(null, null, 61));

            assertNoRequestWasSent(server);
        }
    }

    @Test
    void deleteUsesTheIdentifierAndEscapesIt() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"message\":\"success\",\"data\":null}");
            CloudBrowserApi api = apiFor(server);

            assertCall(server, "DELETE", "/v1/browser/db-mtabc123", null,
                    calling(() -> api.delete("db-mtabc123")));

            // The identifier is caller-supplied, so it cannot be pasted in raw.
            assertCall(server, "DELETE", "/v1/browser/a%20b%2Fc", null,
                    calling(() -> api.delete("a b/c")));
        }
    }

    @Test
    void deleteRejectsABlankIdentifier() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            assertThrows(ValidationException.class, () -> apiFor(server).delete("  "));
            assertNoRequestWasSent(server);
        }
    }

    @Test
    void statusReportsNotYetRegisteredAsANormalAnswer() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            // "Not up yet" is a value, never an exception: polling must be quiet.
            server.respondWith(200, "{\"code\":200,\"data\":{\"registered\":false}}");

            CloudBrowserStatus status = apiFor(server).status(SERIAL);

            assertFalse(status.isRegistered());
            assertEquals("", status.getSerialno());
        }
    }

    @Test
    void statusReturnsTheRegisteredDevice() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"data\":{"
                    + "\"registered\":true,\"device_id\":42,\"serialno\":\"db-mtabc123\","
                    + "\"name\":\"Browser-3f2a1b4c\",\"alias_name\":\"my-browser\","
                    + "\"state\":\"free\","
                    + "\"server_url\":\"relay.uusense.com\",\"is_cloud\":true}}");

            CloudBrowserStatus status = apiFor(server).status(SERIAL);

            assertTrue(status.isRegistered());
            assertEquals(42, status.getDeviceId());
            assertEquals("db-mtabc123", status.getSerialno());
            assertEquals("Browser-3f2a1b4c", status.getName());
            assertEquals("my-browser", status.getAliasName());
            assertEquals("free", status.getState());
            assertEquals("relay.uusense.com", status.getServerUrl());
            assertTrue(status.isCloud());
        }
    }

    @Test
    void statusToleratesAnEnvelopeWithoutData() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"message\":\"success\",\"data\":null}");

            // An empty object rather than a null DTO: reading isRegistered()
            // is the normal answer while a browser is starting up.
            assertFalse(apiFor(server).status(SERIAL).isRegistered());
        }
    }

    @Test
    void quotaDecodesTheThreeNumbers() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200,
                    "{\"code\":200,\"data\":{\"limit\":10,\"used\":3,\"remaining\":7}}");

            CloudBrowserQuota quota = apiFor(server).quota();

            assertEquals(10, quota.getLimit());
            assertEquals(3, quota.getUsed());
            assertEquals(7, quota.getRemaining());
        }
    }

    @Test
    void errorsCarryTheirStatusCode() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(404,
                    "{\"code\":404,\"message\":\"设备不存在\",\"trace_id\":\"t\"}");

            DeviceNotFoundException notFound =
                    assertThrows(DeviceNotFoundException.class, () -> apiFor(server).delete(SERIAL));
            assertEquals(404, notFound.getStatusCode().intValue());
        }
    }
}
