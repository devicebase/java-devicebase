package cn.devicebase;

import cn.devicebase.client.DeviceBaseClient;
import cn.devicebase.exception.AuthenticationException;
import cn.devicebase.exception.DeviceBaseException;
import cn.devicebase.exception.DeviceNotFoundException;
import cn.devicebase.exception.ValidationException;
import cn.devicebase.model.AppInfo;
import cn.devicebase.model.Bounds;
import cn.devicebase.model.DeviceInfo;
import cn.devicebase.model.DeviceResponse;
import cn.devicebase.model.DeviceStatsData;
import cn.devicebase.model.GetActiveSessionResponse;
import cn.devicebase.model.HeartbeatResponse;
import cn.devicebase.model.HierarchyInfo;
import cn.devicebase.model.OperationResult;
import cn.devicebase.model.Point;
import cn.devicebase.model.StartDeviceUsageResponse;
import cn.devicebase.model.StopDeviceUsageResponse;
import cn.devicebase.websocket.MinicapClient;
import cn.devicebase.websocket.MinitouchClient;

/**
 * DeviceBase Java SDK - Device automation platform.
 *
 * <p>This is the main entry point for the DeviceBase SDK. Use the
 * {@link DeviceBaseClient} class for all device operations.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * import cn.devicebase.DeviceBaseClient;
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