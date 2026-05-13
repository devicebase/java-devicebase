# DeviceBase Java SDK

A Java SDK for the DeviceBase device automation platform.

## Overview

DeviceBase is a cloud platform for remote Android device control and automation. This Java SDK provides a convenient interface to interact with DeviceBase devices programmatically.

## Features

- **Device Control**: Tap, swipe, input text, launch apps, and more
- **Screenshots**: Capture device screenshots in JPEG format
- **UI Inspection**: Dump UI hierarchy for automation scripting
- **WebSocket Streaming**: Real-time screen streaming via Minicap
- **Touch Control**: Fine-grained touch control via Minitouch
- **Platform API**: Manage devices, sessions, and usage tracking

## Requirements

- Java 11 or higher
- Internet connection to DeviceBase API

## Installation

### Maven

```xml
<dependency>
    <groupId>com.devicebase</groupId>
    <artifactId>devicebase-sdk</artifactId>
    <version>2026.5.13</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.devicebase:devicebase-sdk:2026.5.13'
```

### Manual Installation

Download the JAR from the releases page and add it to your project's classpath.

## Quick Start

```java
import com.devicebase.DeviceBaseClient;
import com.devicebase.model.DeviceInfo;

public class Example {
    public static void main(String[] args) {
        // Create client with API key and device serial
        try (DeviceBaseClient client = new DeviceBaseClient(
                "your-api-key",
                "device-serial-number")) {

            // Get device information
            DeviceInfo info = client.getDeviceInfo();
            System.out.println("Device: " + info.getSerial());

            // Control the device
            client.tap(100, 200);           // Tap at coordinates
            client.swipe(100, 500, 100, 100);  // Swipe up
            client.launchApp("com.example.app"); // Launch an app
            client.inputText("Hello World");    // Input text

            // Get screenshot
            byte[] screenshot = client.getScreenshot();
        }
    }
}
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DEVICEBASE_API_KEY` | Your DeviceBase API key | Required |
| `DEVICEBASE_BASE_URL` | API base URL | `https://api.devicebase.cn` |

### Constructor Options

```java
// Using default base URL
DeviceBaseClient client = new DeviceBaseClient(apiKey, serial);

// Using custom base URL
DeviceBaseClient client = new DeviceBaseClient(apiKey, serial, "https://custom.api.example.com");

// Using custom timeout
DeviceBaseClient client = new DeviceBaseClient(apiKey, serial, baseUrl, Duration.ofSeconds(60));
```

## API Reference

### Device Control

```java
// Touch operations
client.tap(x, y);                    // Single tap
client.doubleTap(x, y);              // Double tap
client.longPress(x, y);              // Long press
client.swipe(x1, y1, x2, y2);       // Swipe gesture

// Navigation
client.back();                       // Press back button
client.home();                       // Press home button

// App operations
client.launchApp("com.example.app"); // Launch app by package name
client.getCurrentApp();              // Get current foreground app

// Text input
client.inputText("text");            // Input text
client.clearText();                  // Clear text

// UI inspection
client.dumpHierarchy();              // Get UI hierarchy

// Screenshots
client.getScreenshot();              // Get screenshot as JPEG bytes
client.downloadScreenshot();        // Download screenshot
```

### Platform API

```java
// Device management
client.listDevices();                        // List all devices
client.listDevices(keyword, type, state, page, pageSize); // Filtered list
client.getDevice(deviceId);                 // Get device by ID
client.getDeviceStats(includeShared);       // Get device statistics

// Session management
client.startDeviceUsage(deviceId);          // Start using a device
client.sendHeartbeat(sessionId);             // Send heartbeat
client.stopDeviceUsage(sessionId);           // Stop using a device
client.getActiveSession();                  // Get active session
```

### WebSocket Clients

```java
// Minicap - Screen streaming
try (MinicapClient minicap = new MinicapClient(baseUrl, serial, apiKey)) {
    byte[] frame = minicap.captureFrame();
    // Process JPEG frame
}

// Minitouch - Touch control
try (MinitouchClient minitouch = new MinitouchClient(baseUrl, serial, apiKey)) {
    minitouch.connect();
    minitouch.tap(100, 200);
    minitouch.swipe(100, 500, 100, 100, 300, 10);
}
```

## Error Handling

The SDK provides specific exception types for different error scenarios:

```java
import com.devicebase.exception.*;

try {
    DeviceBaseClient client = new DeviceBaseClient(apiKey, serial);
    client.tap(100, 200);
} catch (AuthenticationException e) {
    // Invalid API key
} catch (DeviceNotFoundException e) {
    // Device not found or offline
} catch (ValidationException e) {
    // Invalid request parameters
} catch (DeviceBaseException e) {
    // Other API errors
}
```

## Building from Source

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Create JAR
./gradlew jar

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

## Publishing to Maven Central

The SDK is configured for publishing to Maven Central via Sonatype. Set the following environment variables:

- `SONATYPE_USERNAME` - Sonatype OSSRH username
- `SONATYPE_PASSWORD` - Sonatype OSSRH password
- `SIGNING_KEY_ID` - GPG key ID
- `SIGNING_KEY` - GPG private key (armored)
- `SIGNING_PASSWORD` - GPG key password

Then run:

```bash
./gradlew publish
```

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Links

- [DeviceBase Website](https://devicebase.cn)
- [GitHub Repository](https://github.com/uusense/java-devicebase)
- [Python SDK](https://github.com/uusense/python-devicebase)
- [Issue Tracker](https://github.com/uusense/java-devicebase/issues)