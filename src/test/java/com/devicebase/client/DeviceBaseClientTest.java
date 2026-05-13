package com.devicebase.client;

import com.devicebase.exception.AuthenticationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeviceBaseClient class.
 */
class DeviceBaseClientTest {

    @AfterEach
    void tearDown() {
        // Clean up environment variables for test isolation
    }

    @Test
    void constructor_withValidApiKey_shouldCreateClient() {
        // This test verifies the constructor doesn't throw when API key is provided
        // We can't make actual API calls without a real device
        DeviceBaseClient client = new DeviceBaseClient("test-api-key", "device-serial");

        assertEquals("device-serial", client.getSerial());
        assertNotNull(client.getHttpClient());
    }

    @Test
    void constructor_withNullApiKey_shouldThrowException() {
        // Clear the environment variable to ensure test isolation
        String originalValue = System.getenv("DEVICEBASE_API_KEY");
        if (originalValue != null) {
            System.getenv().remove("DEVICEBASE_API_KEY");
        }

        try {
            assertThrows(AuthenticationException.class, () -> {
                new DeviceBaseClient(null, "device-serial");
            });
        } finally {
            // Restore original value
            if (originalValue != null) {
                System.getenv().put("DEVICEBASE_API_KEY", originalValue);
            }
        }
    }

    @Test
    void constructor_withEmptyApiKey_shouldThrowException() {
        // Clear the environment variable to ensure test isolation
        String originalValue = System.getenv("DEVICEBASE_API_KEY");
        if (originalValue != null) {
            System.getenv().remove("DEVICEBASE_API_KEY");
        }

        try {
            assertThrows(AuthenticationException.class, () -> {
                new DeviceBaseClient("", "device-serial");
            });
        } finally {
            // Restore original value
            if (originalValue != null) {
                System.getenv().put("DEVICEBASE_API_KEY", originalValue);
            }
        }
    }

    @Test
    void constructor_withNullSerial_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            new DeviceBaseClient("test-api-key", null);
        });
    }

    @Test
    void getSerial_shouldReturnConfiguredSerial() {
        DeviceBaseClient client = new DeviceBaseClient("test-api-key", "my-device-serial");

        assertEquals("my-device-serial", client.getSerial());
    }

    @Test
    void getBaseUrl_shouldReturnConfiguredBaseUrl() {
        DeviceBaseClient client = new DeviceBaseClient(
            "test-api-key", "device-serial",
            "https://custom.api.example.com"
        );

        assertEquals("https://custom.api.example.com", client.getBaseUrl());
    }

    @Test
    void defaultBaseUrl_shouldBeUsedWhenNotSpecified() {
        DeviceBaseClient client = new DeviceBaseClient(
            "test-api-key", "device-serial"
        );

        assertEquals(DeviceBaseClient.DEFAULT_BASE_URL, client.getBaseUrl());
    }

    @Test
    void close_shouldNotThrow() {
        DeviceBaseClient client = new DeviceBaseClient("test-api-key", "device-serial");

        assertDoesNotThrow(() -> client.close());
    }

    @Test
    void tryWithResources_shouldCloseAutomatically() {
        // Verify try-with-resources pattern works
        assertDoesNotThrow(() -> {
            try (DeviceBaseClient client = new DeviceBaseClient(
                    "test-api-key", "device-serial")) {
                // Just verify client is created
                assertNotNull(client);
            }
        });
    }
}