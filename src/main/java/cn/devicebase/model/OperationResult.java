package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Result of a device control operation.
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
public final class OperationResult {

    private final boolean success;
    private final Map<String, Object> data;

    /**
     * Creates a new OperationResult.
     *
     * @param success whether the operation was successful
     * @param data the raw API response data
     */
    public OperationResult(boolean success, Map<String, Object> data) {
        this.success = success;
        this.data = data != null ? Collections.unmodifiableMap(data) : Collections.emptyMap();
    }

    /**
     * Returns whether the operation was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the raw data from the API response.
     *
     * @return unmodifiable map of operation result data
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
        OperationResult that = (OperationResult) o;
        return success == that.success && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, data);
    }

    @Override
    public String toString() {
        return "OperationResult{success=" + success + ", data=" + data + "}";
    }

    /**
     * Creates an OperationResult from API response data.
     *
     * @param data the API response data map
     * @return a new OperationResult instance
     */
    public static OperationResult fromMap(Map<String, Object> data) {
        if (data == null) {
            return new OperationResult(true, Collections.emptyMap());
        }
        Object successObj = data.get("success");
        boolean success = successObj == null || !(successObj instanceof Boolean)
            || (Boolean) successObj;
        return new OperationResult(success, data);
    }
}