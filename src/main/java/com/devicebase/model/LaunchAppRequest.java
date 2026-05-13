package com.devicebase.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Request payload for launching an application.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LaunchAppRequest {

    @JsonProperty("app_name")
    private final String appName;

    /**
     * Creates a new LaunchAppRequest.
     *
     * @param appName the package name or identifier of the app to launch
     */
    public LaunchAppRequest(@JsonProperty("app_name") String appName) {
        this.appName = appName;
    }

    /**
     * Returns the app name.
     *
     * @return the package name or identifier
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Converts this request to a Map for JSON serialization.
     *
     * @return map containing app_name
     */
    public Map<String, String> toMap() {
        return Map.of("app_name", appName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LaunchAppRequest that = (LaunchAppRequest) o;
        return Objects.equals(appName, that.appName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName);
    }

    @Override
    public String toString() {
        return "LaunchAppRequest{appName='" + appName + "'}";
    }
}