package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * The result of claiming the daily points.
 *
 * <p>Returned by {@link cn.devicebase.http.UserApi#checkin()}.</p>
 *
 * <p>{@link #isAlreadyChecked()} is a normal outcome rather than an error:
 * claiming twice in one day answers with it true, {@link #getCreditsEarned()} 0
 * and the platform's own {@link #getMessage()} ("今日已签到"). That is what makes
 * the daily check-in safe to run from a scheduled task.</p>
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class UserCheckin {

    @JsonProperty("success")
    private final boolean success;

    @JsonProperty("credits_earned")
    private final int creditsEarned;

    @JsonProperty("consecutive_days")
    private final int consecutiveDays;

    @JsonProperty("already_checked")
    private final boolean alreadyChecked;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("credits")
    private final int credits;

    /**
     * Creates a check-in result.
     *
     * @param success whether this call actually granted points
     * @param creditsEarned the points granted, 0 when the day was already claimed
     * @param consecutiveDays the streak this claim belongs to (1 when the
     *     previous day was not claimed)
     * @param alreadyChecked whether today had already been claimed
     * @param message the platform's own wording, for display as-is
     * @param credits the balance after this call
     */
    @JsonCreator
    public UserCheckin(
            @JsonProperty("success") boolean success,
            @JsonProperty("credits_earned") int creditsEarned,
            @JsonProperty("consecutive_days") int consecutiveDays,
            @JsonProperty("already_checked") boolean alreadyChecked,
            @JsonProperty("message") String message,
            @JsonProperty("credits") int credits) {
        this.success = success;
        this.creditsEarned = creditsEarned;
        this.consecutiveDays = consecutiveDays;
        this.alreadyChecked = alreadyChecked;
        this.message = message;
        this.credits = credits;
    }

    /**
     * Returns whether this call actually granted points.
     *
     * @return true when points were granted
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the points granted by this call.
     *
     * @return the points earned; 0 when the day was already claimed
     */
    public int getCreditsEarned() {
        return creditsEarned;
    }

    /**
     * Returns the streak this claim belongs to.
     *
     * @return the consecutive days; the first day is 1
     */
    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    /**
     * Returns whether today had already been claimed.
     *
     * @return true when nothing was granted because today was already claimed
     */
    public boolean isAlreadyChecked() {
        return alreadyChecked;
    }

    /**
     * Returns the platform's own message for this outcome.
     *
     * @return the message, e.g. "签到成功！获得25积分" or "今日已签到"
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the points balance after this call.
     *
     * @return the balance
     */
    public int getCredits() {
        return credits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserCheckin)) {
            return false;
        }
        UserCheckin other = (UserCheckin) o;
        return success == other.success && creditsEarned == other.creditsEarned
                && consecutiveDays == other.consecutiveDays
                && alreadyChecked == other.alreadyChecked
                && credits == other.credits
                && Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, creditsEarned, consecutiveDays, alreadyChecked, message, credits);
    }

    @Override
    public String toString() {
        return "UserCheckin{success=" + success + ", creditsEarned=" + creditsEarned
                + ", consecutiveDays=" + consecutiveDays + ", alreadyChecked=" + alreadyChecked
                + ", message='" + message + "', credits=" + credits + "}";
    }
}
