package com.devicebase.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * UI hierarchy information returned by the API.
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
public final class HierarchyInfo {

    private final Map<String, Object> data;

    /**
     * Creates a new HierarchyInfo.
     *
     * @param data the raw API response data
     */
    public HierarchyInfo(Map<String, Object> data) {
        this.data = data != null ? Collections.unmodifiableMap(data) : Collections.emptyMap();
    }

    /**
     * Returns the raw data from the API response.
     *
     * @return unmodifiable map of hierarchy information
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
        HierarchyInfo that = (HierarchyInfo) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "HierarchyInfo{data=" + data + "}";
    }

    /**
     * Creates a HierarchyInfo from API response data.
     *
     * @param data the API response data map
     * @return a new HierarchyInfo instance
     */
    public static HierarchyInfo fromMap(Map<String, Object> data) {
        return new HierarchyInfo(data);
    }
}