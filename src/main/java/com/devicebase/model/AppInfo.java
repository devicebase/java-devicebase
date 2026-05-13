package com.devicebase.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Information about the currently running application on the device.
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
public final class AppInfo {

    private final Map<String, Object> data;

    /**
     * Creates a new AppInfo.
     *
     * @param data the raw API response data
     */
    public AppInfo(Map<String, Object> data) {
        this.data = data != null ? Collections.unmodifiableMap(data) : Collections.emptyMap();
    }

    /**
     * Returns the raw data from the API response.
     *
     * @return unmodifiable map of app information
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
        AppInfo appInfo = (AppInfo) o;
        return Objects.equals(data, appInfo.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "AppInfo{data=" + data + "}";
    }

    /**
     * Creates an AppInfo from API response data.
     *
     * @param data the API response data map
     * @return a new AppInfo instance
     */
    public static AppInfo fromMap(Map<String, Object> data) {
        return new AppInfo(data);
    }
}