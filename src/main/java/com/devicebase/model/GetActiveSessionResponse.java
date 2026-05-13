package com.devicebase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Response for getting active session information.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GetActiveSessionResponse {

    @JsonProperty("has_active_session")
    private final boolean hasActiveSession;

    @JsonProperty("status")
    private final String status;

    @JsonProperty("session_id")
    private final String sessionId;

    @JsonProperty("device_id")
    private final Integer deviceId;

    @JsonProperty("device_name")
    private final String deviceName;

    @JsonProperty("start_time")
    private final Instant startTime;

    @JsonProperty("last_heartbeat")
    private final Instant lastHeartbeat;

    public GetActiveSessionResponse(
            @JsonProperty("has_active_session") boolean hasActiveSession,
            @JsonProperty("status") String status,
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("device_id") Integer deviceId,
            @JsonProperty("device_name") String deviceName,
            @JsonProperty("start_time") Instant startTime,
            @JsonProperty("last_heartbeat") Instant lastHeartbeat) {
        this.hasActiveSession = hasActiveSession;
        this.status = status;
        this.sessionId = sessionId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.startTime = startTime;
        this.lastHeartbeat = lastHeartbeat;
    }

    public boolean hasActiveSession() {
        return hasActiveSession;
    }

    public String getStatus() {
        return status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetActiveSessionResponse that = (GetActiveSessionResponse) o;
        return hasActiveSession == that.hasActiveSession
            && Objects.equals(status, that.status)
            && Objects.equals(sessionId, that.sessionId)
            && Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasActiveSession, status, sessionId, deviceId);
    }

    @Override
    public String toString() {
        return "GetActiveSessionResponse{hasActiveSession=" + hasActiveSession
            + ", status='" + status + "', sessionId='" + sessionId + "', deviceId=" + deviceId + "}";
    }
}