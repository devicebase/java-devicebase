package com.devicebase;

import com.devicebase.client.DeviceBaseClient;
import com.devicebase.exception.AuthenticationException;
import com.devicebase.exception.DeviceBaseException;
import com.devicebase.exception.DeviceNotFoundException;
import com.devicebase.exception.ValidationException;
import com.devicebase.model.AppInfo;
import com.devicebase.model.Bounds;
import com.devicebase.model.DeviceInfo;
import com.devicebase.model.DeviceResponse;
import com.devicebase.model.DeviceStatsData;
import com.devicebase.model.GetActiveSessionResponse;
import com.devicebase.model.HeartbeatResponse;
import com.devicebase.model.HierarchyInfo;
import com.devicebase.model.OperationResult;
import com.devicebase.model.Point;
import com.devicebase.model.StartDeviceUsageResponse;
import com.devicebase.model.StopDeviceUsageResponse;
import com.devicebase.websocket.MinicapClient;
import com.devicebase.websocket.MinitouchClient;

/**
 * DeviceBase Java SDK - Device automation platform.
 *
 * <p>This is the main entry point for the DeviceBase SDK. Use the
 * {@link DeviceBaseClient} class for all device operations.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * import com.devicebase.DeviceBaseClient;
 *
 * public class Example {
 *     public static void main(String[] args) {
 *         try (DeviceBaseClient client = new DeviceBaseClient(
 *                 "your-api-key", "device-serial-number")) {
 *
 *             // Get device info
 *             DeviceInfo info = client.getDeviceInfo();
 *
 *             // Control the device
 *             client.tap(100, 200);
 *             client.launchApp("com.example.app");
 *
 *             // Get screenshot
 *             byte[] screenshot = client.getScreenshot();
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author Richie
 * @version 1.0.0
 */
public final class DeviceBase {

    private DeviceBase() {
        // Prevent instantiation
    }

    /**
     * Creates a new DeviceBaseClient.
     *
     * <p>API key is read from the DEVICEBASE_API_KEY environment variable.</p>
     *
     * @param apiKey the JWT API key for authentication
     * @param serial the device unique identifier
     * @return a new DeviceBaseClient instance
     * @throws AuthenticationException if no API key is provided
     */
    public static DeviceBaseClient createClient(String apiKey, String serial) {
        return new DeviceBaseClient(apiKey, serial);
    }

    /**
     * Creates a new DeviceBaseClient with explicit base URL.
     *
     * @param apiKey the JWT API key for authentication
     * @param serial the device unique identifier
     * @param baseUrl the base URL of the DeviceBase API
     * @return a new DeviceBaseClient instance
     * @throws AuthenticationException if no API key is provided
     */
    public static DeviceBaseClient createClient(String apiKey, String serial, String baseUrl) {
        return new DeviceBaseClient(apiKey, serial, baseUrl);
    }
}