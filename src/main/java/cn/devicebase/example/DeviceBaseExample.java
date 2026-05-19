package cn.devicebase.example;

import cn.devicebase.client.DeviceBaseClient;
import cn.devicebase.model.DeviceInfo;
import cn.devicebase.model.OperationResult;

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
    private static final String DEVICE_SERIAL = "your-device-serial-number";

    public static void main(String[] args) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("Please set DEVICEBASE_API_KEY environment variable");
            System.err.println("Example: export DEVICEBASE_API_KEY=your-api-key");
            return;
        }

        System.out.println("DeviceBase Java SDK Example");
        System.out.println("============================");

        // Use try-with-resources to ensure proper cleanup
        try (DeviceBaseClient client = new DeviceBaseClient(API_KEY, DEVICE_SERIAL)) {
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
        System.out.println("Device Serial: " + info.getSerial());
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
}