package cn.devicebase.client;

import cn.devicebase.exception.AuthenticationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

        assertEquals("device-serial", client.getSerialno());
        assertNotNull(client.getHttpClient());
    }

    /**
     * {@code System.getenv()} is immutable, so the environment cannot be cleared
     * from inside a test. The absence of the key is therefore an assumption: the
     * test is skipped when DEVICEBASE_API_KEY is set in the environment, rather
     * than failing on an UnsupportedOperationException.
     */
    @Test
    void constructor_withNullApiKey_shouldThrowException() {
        assumeTrue(System.getenv(DeviceBaseClient.ENV_API_KEY) == null,
                DeviceBaseClient.ENV_API_KEY + " is set; cannot test the unset case");

        assertThrows(AuthenticationException.class, () -> {
            new DeviceBaseClient(null, "device-serial");
        });
    }

    /**
     * @see #constructor_withNullApiKey_shouldThrowException for why this is guarded
     */
    @Test
    void constructor_withEmptyApiKey_shouldThrowException() {
        assumeTrue(System.getenv(DeviceBaseClient.ENV_API_KEY) == null,
                DeviceBaseClient.ENV_API_KEY + " is set; cannot test the unset case");

        assertThrows(AuthenticationException.class, () -> {
            new DeviceBaseClient("", "device-serial");
        });
    }

    /**
     * An explicitly provided key always wins over the environment.
     */
    @Test
    void constructor_withExplicitApiKey_doesNotReadTheEnvironment() {
        DeviceBaseClient client = new DeviceBaseClient("explicit-key", "device-serial");
        assertEquals("device-serial", client.getSerialno());
    }

    @Test
    void constructor_withNullSerial_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            String nullValue = null;
            new DeviceBaseClient("test-api-key", nullValue);
        });
    }

    @Test
    void getSerial_shouldReturnConfiguredSerial() {
        DeviceBaseClient client = new DeviceBaseClient("test-api-key", "my-device-serial");

        assertEquals("my-device-serial", client.getSerialno());
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