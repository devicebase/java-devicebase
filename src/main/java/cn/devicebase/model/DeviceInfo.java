package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Device information returned by the API.
 *
 * <p>This is a flexible container that adapts to the actual API response structure.
 * The raw data from the API is stored in the data map.</p>
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class DeviceInfo {

    private final String serialno;
    private final Map<String, Object> data;

    /**
     * Creates a new DeviceInfo.
     *
     * @param serialno the device unique identifier
     * @param data the raw API response data
     */
    public DeviceInfo(String serialno, Map<String, Object> data) {
        this.serialno = serialno;
        this.data = data != null ? Collections.unmodifiableMap(data) : Collections.emptyMap();
    }

    /**
     * Returns the device serialno.
     *
     * @return the device unique identifier
     */
    public String getSerialno() {
        return serialno;
    }

    /**
     * Returns the device serialno.
     *
     * @return the device unique identifier
     * @deprecated use {@link #getSerialno()}. Removed in the next major release.
     */
    @Deprecated
    public String getSerial() {
        return serialno;
    }

    /**
     * Returns the raw data from the API response.
     *
     * @return unmodifiable map of device information
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Gets a value from the data map by key.
     *
     * @param key the data key
     * @return the value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Object value = data.get(key);
        return value != null ? (T) value : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceInfo deviceInfo = (DeviceInfo) o;
        return Objects.equals(serialno, deviceInfo.serialno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialno);
    }

    @Override
    public String toString() {
        return "DeviceInfo{serialno='" + serialno + "', data=" + data + "}";
    }

    /**
     * Creates a DeviceInfo from API response data.
     *
     * @param serialno the device serialno
     * @param data the API response data map
     * @return a new DeviceInfo instance
     */
    public static DeviceInfo fromMap(String serialno, Map<String, Object> data) {
        return new DeviceInfo(serialno, data);
    }
}