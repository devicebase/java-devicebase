package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Whether a created cloud browser has registered itself yet.
 *
 * <p>{@link #isRegistered()} is {@code false} while the browser is still
 * starting up — that is a normal answer, not a failure, so polling is quiet.
 * The remaining fields are only meaningful once it is {@code true}.</p>
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CloudBrowserStatus {

    @JsonProperty("registered")
    private final boolean registered;

    @JsonProperty("device_id")
    private final int deviceId;

    @JsonProperty("serialno")
    private final String serialno;

    @JsonProperty("name")
    private final String name;

    /** The name the caller asked for when creating it; empty when none was given. */
    @JsonProperty("alias_name")
    private final String aliasName;

    @JsonProperty("state")
    private final String state;

    @JsonProperty("server_url")
    private final String serverUrl;

    @JsonProperty("is_cloud")
    private final boolean cloud;

    /**
     * Creates a status reading.
     *
     * @param registered whether the device row exists yet
     * @param deviceId the device id, once registered
     * @param serialno the identifier to drive it with, once registered
     * @param name the registered device name
     * @param state the connection state (busy/free/offline)
     * @param serverUrl the relay host the platform reaches it through
     * @param cloud whether the platform built this one rather than it being
     *     attached by hand
     */
    @JsonCreator
    public CloudBrowserStatus(
            @JsonProperty("registered") boolean registered,
            @JsonProperty("device_id") int deviceId,
            @JsonProperty("serialno") String serialno,
            @JsonProperty("name") String name,
            @JsonProperty("alias_name") String aliasName,
            @JsonProperty("state") String state,
            @JsonProperty("server_url") String serverUrl,
            @JsonProperty("is_cloud") boolean cloud) {
        this.registered = registered;
        this.deviceId = deviceId;
        this.serialno = serialno == null ? "" : serialno;
        this.name = name == null ? "" : name;
        this.aliasName = aliasName == null ? "" : aliasName;
        this.state = state == null ? "" : state;
        this.serverUrl = serverUrl == null ? "" : serverUrl;
        this.cloud = cloud;
    }

    /**
     * Returns whether the browser has registered itself yet.
     *
     * @return true once the device row exists
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Returns the device id.
     *
     * @return the numeric device id, or 0 while unregistered
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * Returns the identifier to drive the browser with.
     *
     * @return the platform serialno; empty while unregistered
     */
    public String getSerialno() {
        return serialno;
    }

    /**
     * Returns the registered device name.
     *
     * @return the name; empty while unregistered
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name the caller asked for when creating it.
     *
     * @return the alias; empty when none was given
     */
    public String getAliasName() {
        return aliasName;
    }

    /**
     * Returns the connection state.
     *
     * @return busy, free or offline; empty while unregistered
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the relay host the platform reaches the browser through.
     *
     * @return the server URL; empty while unregistered
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Returns whether the platform built this browser.
     *
     * @return true for a cloud browser, false for one attached by hand
     */
    public boolean isCloud() {
        return cloud;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CloudBrowserStatus)) {
            return false;
        }
        CloudBrowserStatus other = (CloudBrowserStatus) o;
        return registered == other.registered
                && deviceId == other.deviceId
                && cloud == other.cloud
                && serialno.equals(other.serialno)
                && name.equals(other.name)
                && aliasName.equals(other.aliasName)
                && state.equals(other.state)
                && serverUrl.equals(other.serverUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registered, deviceId, serialno, name, aliasName, state, serverUrl, cloud);
    }

    @Override
    public String toString() {
        return "CloudBrowserStatus{registered=" + registered + ", serialno='" + serialno
                + "', state='" + state + "', isCloud=" + cloud + "}";
    }
}
