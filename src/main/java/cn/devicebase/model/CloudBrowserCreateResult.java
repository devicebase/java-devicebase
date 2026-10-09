package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * What a successful cloud browser creation hands back.
 *
 * <p>Creating is asynchronous — the browser has to start and register itself —
 * so the call waits for that and reports both halves: {@link #getSerialno()}
 * once the browser exists as a device, and {@link #getDeviceSn()} either way.</p>
 *
 * <p>{@link #isRegistered()} being {@code false} is <strong>not</strong> a
 * failure: the browser is still starting. Ask again with
 * {@link cn.devicebase.http.CloudBrowserApi#status(String)} using the device_sn,
 * or delete it with the same value.</p>
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CloudBrowserCreateResult {

    @JsonProperty("serialno")
    private final String serialno;

    @JsonProperty("device_sn")
    private final String deviceSn;

    @JsonProperty("name")
    private final String name;

    /**
     * The name the caller asked for, verbatim — the one to show a user.
     *
     * <p>{@link #getName()} is the platform's own identity for this browser; a
     * node names its instance "{@code <requested>-<port>}", which is not what a
     * caller should be shown. The platform pins this at creation and later
     * re-registrations cannot change it.</p>
     */
    @JsonProperty("alias_name")
    private final String aliasName;

    @JsonProperty("registered")
    private final boolean registered;

    /**
     * Creates a creation result.
     *
     * @param serialno the platform identifier, empty until the browser registers
     * @param deviceSn the node-side identifier it registers under
     * @param name the platform's identity name for this browser
     * @param aliasName the name the caller asked for, empty when none was given
     * @param registered whether the device row exists yet
     */
    @JsonCreator
    public CloudBrowserCreateResult(
            @JsonProperty("serialno") String serialno,
            @JsonProperty("device_sn") String deviceSn,
            @JsonProperty("name") String name,
            @JsonProperty("alias_name") String aliasName,
            @JsonProperty("registered") boolean registered) {
        this.serialno = serialno == null ? "" : serialno;
        this.deviceSn = deviceSn == null ? "" : deviceSn;
        this.name = name == null ? "" : name;
        this.aliasName = aliasName == null ? "" : aliasName;
        this.registered = registered;
    }

    /**
     * Returns the platform identifier every other browser method takes.
     *
     * @return the serialno; empty until the browser has registered
     */
    public String getSerialno() {
        return serialno;
    }

    /**
     * Returns the node-side identifier the browser registers under.
     *
     * <p>Valid as a handle whether or not registration has happened yet — both
     * {@code status} and {@code delete} accept it.</p>
     *
     * @return the device_sn; never null
     */
    public String getDeviceSn() {
        return deviceSn;
    }

    /**
     * Returns the platform's identity name for this browser.
     *
     * @return the name; never null. For what the caller asked for, see
     *     {@link #getAliasName()}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name the caller asked for, verbatim.
     *
     * @return the alias; empty when no name was given
     */
    public String getAliasName() {
        return aliasName;
    }

    /**
     * Returns whether the browser has registered itself yet.
     *
     * @return true once the device row exists
     */
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CloudBrowserCreateResult)) {
            return false;
        }
        CloudBrowserCreateResult other = (CloudBrowserCreateResult) o;
        return registered == other.registered
                && serialno.equals(other.serialno)
                && deviceSn.equals(other.deviceSn)
                && name.equals(other.name)
                && aliasName.equals(other.aliasName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialno, deviceSn, name, aliasName, registered);
    }

    @Override
    public String toString() {
        return "CloudBrowserCreateResult{serialno='" + serialno + "', deviceSn='" + deviceSn
                + "', name='" + name + "', registered=" + registered + "}";
    }
}
