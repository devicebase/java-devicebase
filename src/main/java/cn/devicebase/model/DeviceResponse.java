package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * One device row from {@code GET /v1/devices}.
 *
 * <p>The device identifier is {@link #getSerialno()} — the platform-issued key
 * (e.g. {@code db-mttul4i41di8}) that every control method takes. Its physical
 * counterpart is {@link #getDeviceSn()}; the gateway resolves either, but the
 * serialno is primary.</p>
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DeviceResponse {

    @JsonProperty("id")
    private final int id;

    /** The platform key. See the creator for the legacy {@code serial} fallback. */
    @JsonProperty("serialno")
    private final String serialno;

    @JsonProperty("device_sn")
    private final String deviceSn;

    @JsonProperty("state")
    private final String state;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("alias_name")
    private final String aliasName;

    @JsonProperty("udid")
    private final String udid;

    @JsonProperty("type")
    private final String type;

    @JsonProperty("brand")
    private final String brand;

    @JsonProperty("model")
    private final String model;

    @JsonProperty("os_type")
    private final String osType;

    @JsonProperty("os_version")
    private final String osVersion;

    @JsonProperty("display")
    private final String display;

    @JsonProperty("location")
    private final String location;

    @JsonProperty("operator")
    private final String operator;

    @JsonProperty("network")
    private final String network;

    @JsonProperty("updated_at")
    private final Instant updatedAt;

    /**
     * Builds a row. {@code legacySerial} carries the older Python service's
     * spelling of the identifier column; prefer {@code serialno} when both are
     * present. It is a separate creator parameter rather than a
     * {@code @JsonAlias} because an alias makes the outcome depend on which key
     * appears last in the JSON.
     */
    @JsonCreator
    public DeviceResponse(
            @JsonProperty("id") int id,
            @JsonProperty("serialno") String serialno,
            @JsonProperty("serial") String legacySerial,
            @JsonProperty("device_sn") String deviceSn,
            @JsonProperty("state") String state,
            @JsonProperty("name") String name,
            @JsonProperty("alias_name") String aliasName,
            @JsonProperty("udid") String udid,
            @JsonProperty("type") String type,
            @JsonProperty("brand") String brand,
            @JsonProperty("model") String model,
            @JsonProperty("os_type") String osType,
            @JsonProperty("os_version") String osVersion,
            @JsonProperty("display") String display,
            @JsonProperty("location") String location,
            @JsonProperty("operator") String operator,
            @JsonProperty("network") String network,
            @JsonProperty("updated_at") Instant updatedAt) {
        this.id = id;
        // The platform key wins when both are sent.
        this.serialno = serialno != null ? serialno : legacySerial;
        this.deviceSn = deviceSn;
        this.state = state;
        this.name = name;
        this.aliasName = aliasName;
        this.udid = udid;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.osType = osType;
        this.osVersion = osVersion;
        this.display = display;
        this.location = location;
        this.operator = operator;
        this.network = network;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the device ID.
     *
     * @return the numeric device ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the platform serialno — the identifier every control method takes.
     *
     * @return the platform serialno
     */
    public String getSerialno() {
        return serialno;
    }

    /**
     * Returns the physical device serialno (a UUID).
     *
     * @return the device_sn UUID
     */
    public String getDeviceSn() {
        return deviceSn;
    }

    /**
     * Returns the device state: {@code busy}, {@code free} or {@code offline}.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the device name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the user-set alias.
     *
     * @return the alias
     */
    public String getAliasName() {
        return aliasName;
    }

    /**
     * Returns the device UDID.
     *
     * @return the udid
     */
    public String getUdid() {
        return udid;
    }

    /**
     * Returns the coarse device type: {@code adb}, {@code hdc}, {@code ios},
     * {@code browser} or {@code computer}.
     *
     * @return the device type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the device brand.
     *
     * @return the brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Returns the device model.
     *
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * Returns the operating system, e.g. {@code Android}, {@code macOS},
     * {@code Chrome}.
     *
     * <p>{@code --type} filters for system types are resolved against this
     * field, because a device row only carries the coarse {@link #getType()}: a
     * Chrome browser is {@code type=browser} with {@code os_type=Chrome}.</p>
     *
     * @return the operating system
     */
    public String getOsType() {
        return osType;
    }

    /**
     * Returns the operating system version.
     *
     * @return the OS version
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * Returns the screen resolution.
     *
     * @return the display size
     */
    public String getDisplay() {
        return display;
    }

    /**
     * Returns the device location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the mobile carrier.
     *
     * @return the operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Returns the network type.
     *
     * @return the network
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Returns when the device row was last updated.
     *
     * <p>The server sends a local time with no zone offset
     * ({@code 2026-09-20T15:11:31}), which is read as the local zone.</p>
     *
     * @return the last update time, or null
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ========== Deprecated accessors ==========

    /**
     * Returns the platform serialno.
     *
     * @return the platform serialno
     * @deprecated the list route returns {@code serialno}, never {@code serial};
     *     use {@link #getSerialno()}.
     */
    @Deprecated
    public String getSerial() {
        return serialno;
    }

    /**
     * Returns the device description.
     *
     * @return always null
     * @deprecated the list route does not return a {@code description}.
     */
    @Deprecated
    public String getDescription() {
        return null;
    }

    /**
     * Returns the per-device server URL.
     *
     * @return always null
     * @deprecated {@code server_url} is not a field of the list route.
     */
    @Deprecated
    public String getServerUrl() {
        return null;
    }

    /**
     * Returns the owning user ID.
     *
     * @return always 0
     * @deprecated the list route does not return a {@code user_id}.
     */
    @Deprecated
    public int getUserId() {
        return 0;
    }

    /**
     * Returns whether the device is shared.
     *
     * @return always false
     * @deprecated the list route does not return an {@code is_shared} flag.
     */
    @Deprecated
    public boolean isShared() {
        return false;
    }

    /**
     * Returns when the device row was created.
     *
     * @return always null
     * @deprecated the list route does not return {@code created_at}.
     */
    @Deprecated
    public Instant getCreatedAt() {
        return null;
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
        return "DeviceResponse{id=" + id + ", serialno='" + serialno + "', name='" + name
            + "', type='" + type + "', state='" + state + "'}";
    }
}
