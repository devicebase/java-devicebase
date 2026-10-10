package cn.devicebase.http;

import cn.devicebase.exception.AuthenticationException;
import cn.devicebase.model.UserCheckin;
import cn.devicebase.model.UserInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the account surface — {@code /v1/user/*}.
 *
 * <p>Like the cloud browser lifecycle, these calls address no device; unlike
 * it, they take no arguments at all — the API key decides whose account this
 * is.</p>
 */
class UserApiTest {

    private UserApi apiFor(RecordingServer server) {
        return new DeviceBaseHttpClient(server.url(), "k").user();
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
    void infoReadsTheAccountWithNoArguments() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"data\":{}}");

            assertCall(server, "GET", "/v1/user/info", null, () -> {
                try {
                    apiFor(server).info();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Test
    void infoDecodesTheRecord() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"message\":\"success\",\"data\":{"
                    + "\"id\":42,\"username\":\"richie\",\"mobile\":\"13800138000\","
                    + "\"credits\":1259,\"registered_at\":\"2026-01-02T03:04:05\","
                    + "\"can_checkin\":true}}");

            UserInfo info = apiFor(server).info();

            assertEquals(42, info.getId());
            assertEquals("richie", info.getUsername());
            assertEquals("13800138000", info.getMobile());
            assertEquals(1259, info.getCredits());
            assertEquals("2026-01-02T03:04:05", info.getRegisteredAt());
            assertTrue(info.isCanCheckin());
        }
    }

    @Test
    void checkinSendsAnEmptyObject() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"data\":{}}");

            // 空对象而不是没有请求体：签到没有参数，而 {} 在还没有「空体容忍」的
            // 部署上也照样被接受（请求总是带着 content-type: application/json）。
            assertCall(server, "POST", "/v1/user/checkin", "{}", () -> {
                try {
                    apiFor(server).checkin();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Test
    void checkinDecodesTheReward() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(200, "{\"code\":200,\"data\":{\"success\":true,"
                    + "\"credits_earned\":35,\"consecutive_days\":2,\"already_checked\":false,"
                    + "\"message\":\"签到成功！获得35积分\",\"credits\":1294}}");

            UserCheckin reward = apiFor(server).checkin();

            assertTrue(reward.isSuccess());
            assertEquals(35, reward.getCreditsEarned());
            assertEquals(2, reward.getConsecutiveDays());
            assertFalse(reward.isAlreadyChecked());
            assertEquals("签到成功！获得35积分", reward.getMessage());
            assertEquals(1294, reward.getCredits());
        }
    }

    @Test
    void alreadyCheckedIsANormalAnswerNotAnError() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            // 「已领过」是正常回答：每日定时任务可以无脑重复执行它
            server.respondWith(200, "{\"code\":200,\"data\":{\"success\":false,"
                    + "\"credits_earned\":0,\"consecutive_days\":2,\"already_checked\":true,"
                    + "\"message\":\"今日已签到\",\"credits\":1294}}");

            UserCheckin reward = apiFor(server).checkin();

            assertTrue(reward.isAlreadyChecked());
            assertFalse(reward.isSuccess());
            assertEquals(0, reward.getCreditsEarned());
            assertEquals("今日已签到", reward.getMessage());
        }
    }

    @Test
    void missingKeySurfacesAsAuthenticationException() throws Exception {
        try (RecordingServer server = new RecordingServer()) {
            server.respondWith(401,
                    "{\"code\":401,\"message\":\"缺少 API Key，请通过 Authorization: Bearer、"
                            + "X-API-Key 头部或 ?api_key= 参数提供\",\"trace_id\":\"t\"}");

            AuthenticationException unauthorized = assertThrows(
                    AuthenticationException.class, () -> apiFor(server).info());
            assertEquals(401, unauthorized.getStatusCode().intValue());
        }
    }
}
