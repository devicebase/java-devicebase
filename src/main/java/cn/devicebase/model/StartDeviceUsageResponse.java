package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Response when starting device usage.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class StartDeviceUsageResponse {

    @JsonProperty("success")
    private final boolean success;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("session_id")
    private final String sessionId;

    @JsonProperty("start_time")
    private final Instant startTime;

    @JsonProperty("device_name")
    private final String deviceName;

    public StartDeviceUsageResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("message") String message,
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("start_time") Instant startTime,
            @JsonProperty("device_name") String deviceName) {
        this.success = success;
        this.message = message;
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.deviceName = deviceName;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StartDeviceUsageResponse that = (StartDeviceUsageResponse) o;
        return success == that.success && Objects.equals(message, that.message)
            && Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, sessionId);
    }

    @Override
    public String toString() {
        return "StartDeviceUsageResponse{success=" + success + ", message='" + message
            + "', sessionId='" + sessionId + "'}";
    }
}