package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Device information from the platform API.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DeviceResponse {

    @JsonProperty("serial")
    private final String serial;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("type")
    private final String type;

    @JsonProperty("state")
    private final String state;

    @JsonProperty("os_version")
    private final String osVersion;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("server_url")
    private final String serverUrl;

    @JsonProperty("id")
    private final int id;

    @JsonProperty("user_id")
    private final int userId;

    @JsonProperty("is_shared")
    private final boolean isShared;

    @JsonProperty("created_at")
    private final Instant createdAt;

    @JsonProperty("updated_at")
    private final Instant updatedAt;

    public DeviceResponse(
            @JsonProperty("serial") String serial,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("state") String state,
            @JsonProperty("os_version") String osVersion,
            @JsonProperty("description") String description,
            @JsonProperty("server_url") String serverUrl,
            @JsonProperty("id") int id,
            @JsonProperty("user_id") int userId,
            @JsonProperty("is_shared") boolean isShared,
            @JsonProperty("created_at") Instant createdAt,
            @JsonProperty("updated_at") Instant updatedAt) {
        this.serial = serial;
        this.name = name;
        this.type = type;
        this.state = state;
        this.osVersion = osVersion;
        this.description = description;
        this.serverUrl = serverUrl;
        this.id = id;
        this.userId = userId;
        this.isShared = isShared;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getSerial() {
        return serial;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getState() {
        return state;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getDescription() {
        return description;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isShared() {
        return isShared;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceResponse that = (DeviceResponse) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DeviceResponse{id=" + id + ", serial='" + serial + "', name='" + name
            + "', type='" + type + "', state='" + state + "'}";
    }
}