package com.devicebase.model;

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

    private final String serial;
    private final Map<String, Object> data;

    /**
     * Creates a new DeviceInfo.
     *
     * @param serial the device unique identifier
     * @param data the raw API response data
     */
    public DeviceInfo(String serial, Map<String, Object> data) {
        this.serial = serial;
        this.data = data != null ? Collections.unmodifiableMap(data) : Collections.emptyMap();
    }

    /**
     * Returns the device serial number.
     *
     * @return the device unique identifier
     */
    public String getSerial() {
        return serial;
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
        return Objects.equals(serial, deviceInfo.serial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serial);
    }

    @Override
    public String toString() {
        return "DeviceInfo{serial='" + serial + "', data=" + data + "}";
    }

    /**
     * Creates a DeviceInfo from API response data.
     *
     * @param serial the device serial
     * @param data the API response data map
     * @return a new DeviceInfo instance
     */
    public static DeviceInfo fromMap(String serial, Map<String, Object> data) {
        return new DeviceInfo(serial, data);
    }
}