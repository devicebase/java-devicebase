package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Response when stopping device usage.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class StopDeviceUsageResponse {

    @JsonProperty("success")
    private final boolean success;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("duration_minutes")
    private final int durationMinutes;

    @JsonProperty("credits_charged")
    private final int creditsCharged;

    @JsonProperty("duration_text")
    private final String durationText;

    @JsonProperty("final_balance")
    private final int finalBalance;

    public StopDeviceUsageResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("message") String message,
            @JsonProperty("duration_minutes") int durationMinutes,
            @JsonProperty("credits_charged") int creditsCharged,
            @JsonProperty("duration_text") String durationText,
            @JsonProperty("final_balance") int finalBalance) {
        this.success = success;
        this.message = message;
        this.durationMinutes = durationMinutes;
        this.creditsCharged = creditsCharged;
        this.durationText = durationText;
        this.finalBalance = finalBalance;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getCreditsCharged() {
        return creditsCharged;
    }

    public String getDurationText() {
        return durationText;
    }

    public int getFinalBalance() {
        return finalBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StopDeviceUsageResponse that = (StopDeviceUsageResponse) o;
        return success == that.success && durationMinutes == that.durationMinutes
            && creditsCharged == that.creditsCharged && finalBalance == that.finalBalance
            && Objects.equals(message, that.message)
            && Objects.equals(durationText, that.durationText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, durationMinutes, creditsCharged,
            durationText, finalBalance);
    }

    @Override
    public String toString() {
        return "StopDeviceUsageResponse{success=" + success + ", message='" + message
            + "', durationMinutes=" + durationMinutes + ", creditsCharged=" + creditsCharged + "}";
    }
}