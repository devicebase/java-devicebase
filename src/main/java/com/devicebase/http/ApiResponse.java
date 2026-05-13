package com.devicebase.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Generic API response wrapper for platform API responses.
 *
 * <p>All platform API responses follow this envelope format:</p>
 * <pre>{@code
 * {
 *   "success": true,
 *   "data": { ... },
 *   "error": null,
 *   "total": 100
 * }
 * }</pre>
 *
 * @param <T> the type of the data payload
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse<T> {

    @JsonProperty("success")
    private final boolean success;

    @JsonProperty("data")
    private final T data;

    @JsonProperty("error")
    private final String error;

    @JsonProperty("total")
    private final Integer total;

    public ApiResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("data") T data,
            @JsonProperty("error") String error,
            @JsonProperty("total") Integer total) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.total = total;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public Integer getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "ApiResponse{success=" + success + ", error='" + error + "', total=" + total + "}";
    }
}