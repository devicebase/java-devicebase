package com.devicebase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Response for device usage heartbeat.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class HeartbeatResponse {

    @JsonProperty("success")
    private final boolean success;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("remaining_seconds")
    private final Integer remainingSeconds;

    public HeartbeatResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("message") String message,
            @JsonProperty("remaining_seconds") Integer remainingSeconds) {
        this.success = success;
        this.message = message;
        this.remainingSeconds = remainingSeconds;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Integer getRemainingSeconds() {
        return remainingSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HeartbeatResponse that = (HeartbeatResponse) o;
        return success == that.success && Objects.equals(message, that.message)
            && Objects.equals(remainingSeconds, that.remainingSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, remainingSeconds);
    }

    @Override
    public String toString() {
        return "HeartbeatResponse{success=" + success + ", message='" + message
            + "', remainingSeconds=" + remainingSeconds + "}";
    }
}