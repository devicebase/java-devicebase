package cn.devicebase.http;

import cn.devicebase.exception.DeviceBaseException;
import cn.devicebase.model.UserCheckin;
import cn.devicebase.model.UserInfo;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;

/**
 * The account itself — {@code GET /v1/user/info} and {@code POST /v1/user/checkin}.
 *
 * <p>The third account-level group, next to the device list and
 * {@link CloudBrowserApi}: none of them addresses a device, so none takes a
 * serialno. This one answers "whose key is this, and what does the account
 * have" — the name, phone, points balance and registration date, plus the daily
 * check-in that earns more points.</p>
 *
 * <p>Only ever your own account: the API key decides whose record this is, and
 * there is no argument for pointing it at somebody else.</p>
 *
 * <p>Obtain an instance with {@link DeviceBaseHttpClient#user()}.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
public class UserApi {

    private final DeviceBaseHttpClient http;

    UserApi(DeviceBaseHttpClient http) {
        this.http = http;
    }

    /**
     * The {@code data} object of an envelope, or an empty one.
     */
    private JsonNode dataOf(JsonNode response) {
        JsonNode data = response.get("data");
        return data == null || data.isNull() ? http.mapper().createObjectNode() : data;
    }

    /**
     * Returns the account behind the API key.
     *
     * <p>The name ({@code username}), phone ({@code mobile}), points balance
     * ({@code credits}), registration time and {@code canCheckin} — whether
     * today's reward is still unclaimed.</p>
     *
     * @return the account record
     * @throws DeviceBaseException if the request fails — 401 when the key is
     *     missing or unrecognised, 404 when the key is valid but its account is
     *     gone
     */
    public UserInfo info() throws DeviceBaseException {
        JsonNode response = http.request("GET", "/v1/user/info", null, null);
        return http.mapper().convertValue(dataOf(response), UserInfo.class);
    }

    /**
     * Claims the daily points: 25 on the first day, +10 per consecutive day, up
     * to 95 a day.
     *
     * <p>Claiming twice in one day is <strong>not</strong> an error — the second
     * call returns {@link UserCheckin#isAlreadyChecked()} true and grants
     * nothing. That is deliberate: it makes this call safe to put in a daily
     * scheduled task that may fire more than once, without the task having to
     * ask {@code canCheckin} first.</p>
     *
     * <p>An empty JSON object is sent rather than no body at all: the call has
     * no parameters either way, and {@code {}} is what a deployment predating
     * the server's body-less-POST tolerance also accepts.</p>
     *
     * @return the claim result — points earned, streak, and the platform's own
     *     message
     * @throws DeviceBaseException if the request fails
     */
    public UserCheckin checkin() throws DeviceBaseException {
        JsonNode response = http.request("POST", "/v1/user/checkin", Collections.emptyMap(), null);
        return http.mapper().convertValue(dataOf(response), UserCheckin.class);
    }
}
