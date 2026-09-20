package cn.devicebase.example;

import cn.devicebase.client.DeviceBaseClient;
import cn.devicebase.model.DeviceInfo;
import cn.devicebase.model.DeviceResponse;
import cn.devicebase.model.OperationResult;

import java.util.List;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Example demonstrating DeviceBase Java SDK usage.
 *
 * <p>This example shows common use cases for the DeviceBase SDK:</p>
 * <ul>
 *   <li>Creating a client connection</li>
 *   <li>Getting device information</li>
 *   <li>Performing touch operations</li>
 *   <li>Taking screenshots</li>
 *   <li>Launching applications</li>
 * </ul>
 */
public class DeviceBaseExample {

    // Configuration
    private static final String API_KEY = System.getenv("DEVICEBASE_API_KEY");
    private static final String DEVICE_SERIALNO = "your-device-serial-number";

    public static void main(String[] args) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("Please set DEVICEBASE_API_KEY environment variable");
            System.err.println("Example: export DEVICEBASE_API_KEY=your-api-key");
            return;
        }

        System.out.println("DeviceBase Java SDK Example");
        System.out.println("============================");

        // Use try-with-resources to ensure proper cleanup
        try (DeviceBaseClient client = new DeviceBaseClient(API_KEY, DEVICE_SERIALNO)) {
            // Example: Get device information
            exampleGetDeviceInfo(client);

            // Example: Touch operations
            exampleTouchOperations(client);

            // Example: Take screenshot
            exampleScreenshot(client);

            // Example: Launch app
            exampleLaunchApp(client);

            // Example: Input text
            exampleInputText(client);

            // Example: Discover the devices behind the other two platforms
            exampleListDevices(client);

            // Example: Drive a browser over CDP
            exampleBrowser(client);

            // Example: Drive a desktop
            exampleComputer(client);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Demonstrates getting device information.
     */
    private static void exampleGetDeviceInfo(DeviceBaseClient client) throws Exception {
        System.out.println("\n--- Get Device Info ---");

        DeviceInfo info = client.getDeviceInfo();
        System.out.println("Device Serial: " + info.getSerialno());
        System.out.println("Device Data: " + info.getData());
    }

    /**
     * Demonstrates touch operations.
     */
    private static void exampleTouchOperations(DeviceBaseClient client) throws Exception {
        System.out.println("\n--- Touch Operations ---");

        // Single tap at (100, 200)
        OperationResult result = client.tap(100, 200);
        System.out.println("Tap at (100, 200): " + (result.isSuccess() ? "Success" : "Failed"));

        // Double tap at (100, 200)
        result = client.doubleTap(100, 200);
        System.out.println("Double tap at (100, 200): " + (result.isSuccess() ? "Success" : "Failed"));

        // Long press at (100, 200)
        result = client.longPress(100, 200);
        System.out.println("Long press at (100, 200): " + (result.isSuccess() ? "Success" : "Failed"));

        // Swipe from (100, 500) to (100, 100) - swipe up
        result = client.swipe(100, 500, 100, 100);
        System.out.println("Swipe up: " + (result.isSuccess() ? "Success" : "Failed"));
    }

    /**
     * Demonstrates navigation operations.
     */
    private static void exampleNavigation(DeviceBaseClient client) throws Exception {
        System.out.println("\n--- Navigation ---");

        // Press back button
        OperationResult result = client.back();
        System.out.println("Back button: " + (result.isSuccess() ? "Success" : "Failed"));

        // Press home button
        result = client.home();
        System.out.println("Home button: " + (result.isSuccess() ? "Success" : "Failed"));
    }

    /**
     * Demonstrates taking screenshots.
     */
    private static void exampleScreenshot(DeviceBaseClient client) throws Exception {
        System.out.println("\n--- Screenshot ---");

        byte[] screenshot = client.getScreenshot();
        System.out.println("Screenshot size: " + screenshot.length + " bytes");

        // Save to file (optional)
        // try (FileOutputStream fos = new FileOutputStream("screenshot.jpg")) {
        //     fos.write(screenshot);
        //     System.out.println("Screenshot saved to screenshot.jpg");
        // }
    }

    /**
     * Demonstrates launching an application.
     */
    private static void exampleLaunchApp(DeviceBaseClient client) throws Exception {
        System.out.println("\n--- Launch App ---");

        String appPackage = "com.android.browser";
        OperationResult result = client.launchApp(appPackage);
        System.out.println("Launch " + appPackage + ": " + (result.isSuccess() ? "Success" : "Failed"));
    }

    /**
     * Demonstrates text input.
     */
    private static void exampleInputText(DeviceBaseClient client) throws Exception {
        System.out.println("\n--- Text Input ---");

        // Input text
        OperationResult result = client.inputText("Hello World");
        System.out.println("Input text: " + (result.isSuccess() ? "Success" : "Failed"));

        // Clear text
        result = client.clearText();
        System.out.println("Clear text: " + (result.isSuccess() ? "Success" : "Failed"));
    }

    /**
     * Demonstrates device discovery.
     *
     * <p>This is the entry point for every platform: a serialno is only
     * meaningful within one family, and this is how it is found.</p>
     */
    private static void exampleListDevices(DeviceBaseClient client) throws Exception {
        System.out.println("\n--- List Devices ---");

        for (String category : new String[] {"mobile", "browser", "computer"}) {
            List<DeviceResponse> devices = client.listDevices(category);
            System.out.println(category + ": " + devices.size() + " device(s)");
            for (DeviceResponse device : devices) {
                System.out.println("  " + device.getSerialno()
                        + "  type=" + device.getType()
                        + "  os_type=" + device.getOsType()
                        + "  state=" + device.getState());
            }
        }
    }

    /**
     * Demonstrates driving a registered browser device over CDP.
     *
     * <p>Browser methods take the browser device's serialno, which is different
     * from the mobile serialno the client was constructed with.</p>
     */
    private static void exampleBrowser(DeviceBaseClient client) throws Exception {
        System.out.println("\n--- Browser ---");

        List<DeviceResponse> browsers = client.listDevices("browser");
        if (browsers.isEmpty()) {
            System.out.println("No browser device registered; skipping");
            return;
        }
        String serialno = browsers.get(0).getSerialno();

        client.browser().navigate(serialno, "https://example.com");
        client.browser().fill(serialno, "#search", "devicebase");
        client.browser().click(serialno, "button[type=submit]");

        OperationResult state = client.browser().state(serialno);
        System.out.println("Browser state: " + state.getData());
    }

    /**
     * Demonstrates driving a registered desktop.
     *
     * <p>{@code wait} takes milliseconds and {@code bash}'s timeout takes
     * seconds; both widen the HTTP deadline to cover the block.</p>
     */
    private static void exampleComputer(DeviceBaseClient client) throws Exception {
        System.out.println("\n--- Computer ---");

        List<DeviceResponse> computers = client.listDevices("computer");
        if (computers.isEmpty()) {
            System.out.println("No computer device registered; skipping");
            return;
        }
        String serialno = computers.get(0).getSerialno();

        System.out.println("Screen: " + client.computer().screenSize(serialno).getData());
        System.out.println("Mouse:  " + client.computer().position(serialno).getData());

        // The command's own exit status arrives in data.exitCode, not as an error.
        OperationResult ran = client.computer().bash(serialno, "echo hello", 30);
        System.out.println("bash exitCode: " + ran.getData().get("exitCode"));
    }
}