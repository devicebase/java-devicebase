package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * The account behind the API key.
 *
 * <p>Returned by {@link cn.devicebase.http.UserApi#info()}: the name shown in the
 * console, the phone number on the account, the points balance and when the
 * account was created, plus whether today's check-in is still unclaimed.</p>
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class UserInfo {

    @JsonProperty("id")
    private final int id;

    @JsonProperty("username")
    private final String username;

    @JsonProperty("mobile")
    private final String mobile;

    @JsonProperty("credits")
    private final int credits;

    @JsonProperty("registered_at")
    private final String registeredAt;

    @JsonProperty("can_checkin")
    private final boolean canCheckin;

    /**
     * Creates an account record.
     *
     * @param id the account id
     * @param username the name shown in the console
     * @param mobile the phone number on the account
     * @param credits the points balance (积分)
     * @param registeredAt when the account was created, naive ISO format
     * @param canCheckin whether today's check-in reward is still unclaimed
     */
    @JsonCreator
    public UserInfo(
            @JsonProperty("id") int id,
            @JsonProperty("username") String username,
            @JsonProperty("mobile") String mobile,
            @JsonProperty("credits") int credits,
            @JsonProperty("registered_at") String registeredAt,
            @JsonProperty("can_checkin") boolean canCheckin) {
        this.id = id;
        this.username = username;
        this.mobile = mobile;
        this.credits = credits;
        this.registeredAt = registeredAt;
        this.canCheckin = canCheckin;
    }

    /**
     * Returns the account id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name shown in the console.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the phone number on the account.
     *
     * @return the mobile number
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * Returns the points balance (积分).
     *
     * @return the balance
     */
    public int getCredits() {
        return credits;
    }

    /**
     * Returns when the account was created.
     *
     * @return the registration time, naive ISO format
     */
    public String getRegisteredAt() {
        return registeredAt;
    }

    /**
     * Returns whether today's check-in reward is still unclaimed.
     *
     * @return true when the daily points have not been claimed yet today
     */
    public boolean isCanCheckin() {
        return canCheckin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserInfo)) {
            return false;
        }
        UserInfo other = (UserInfo) o;
        return id == other.id && credits == other.credits && canCheckin == other.canCheckin
                && Objects.equals(username, other.username)
                && Objects.equals(mobile, other.mobile)
                && Objects.equals(registeredAt, other.registeredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, mobile, credits, registeredAt, canCheckin);
    }

    @Override
    public String toString() {
        return "UserInfo{id=" + id + ", username='" + username + "', mobile='" + mobile
                + "', credits=" + credits + ", registeredAt='" + registeredAt
                + "', canCheckin=" + canCheckin + "}";
    }
}
