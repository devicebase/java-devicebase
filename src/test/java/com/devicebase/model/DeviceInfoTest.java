package com.devicebase.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeviceInfo class.
 */
class DeviceInfoTest {

    @Test
    void constructor_shouldCreateDeviceInfo() {
        Map<String, Object> data = Map.of("status", "online", "model", "TestDevice");
        DeviceInfo info = new DeviceInfo("serial123", data);

        assertEquals("serial123", info.getSerial());
        assertEquals("online", info.get("status"));
        assertEquals("TestDevice", info.get("model"));
    }

    @Test
    void constructor_withNullData_shouldUseEmptyMap() {
        DeviceInfo info = new DeviceInfo("serial123", null);

        assertTrue(info.getData().isEmpty());
    }

    @Test
    void getData_shouldReturnUnmodifiableMap() {
        Map<String, Object> data = Map.of("key", "value");
        DeviceInfo info = new DeviceInfo("serial123", data);

        assertThrows(UnsupportedOperationException.class, () -> {
            info.getData().put("newKey", "newValue");
        });
    }

    @Test
    void get_withNonExistentKey_shouldReturnNull() {
        DeviceInfo info = new DeviceInfo("serial123", Map.of());

        assertNull(info.get("nonexistent"));
    }

    @Test
    void fromMap_shouldCreateCorrectDeviceInfo() {
        Map<String, Object> data = Map.of("name", "Device1");
        DeviceInfo info = DeviceInfo.fromMap("serial123", data);

        assertEquals("serial123", info.getSerial());
        assertEquals("Device1", info.get("name"));
    }

    @Test
    void equals_withSameSerial_shouldBeEqual() {
        DeviceInfo info1 = new DeviceInfo("serial123", Map.of("key1", "value1"));
        DeviceInfo info2 = new DeviceInfo("serial123", Map.of("key2", "value2"));

        assertEquals(info1, info2);
    }

    @Test
    void equals_withDifferentSerial_shouldNotBeEqual() {
        DeviceInfo info1 = new DeviceInfo("serial123", Map.of());
        DeviceInfo info2 = new DeviceInfo("serial456", Map.of());

        assertNotEquals(info1, info2);
    }

    @Test
    void toString_shouldContainSerial() {
        DeviceInfo info = new DeviceInfo("serial123", Map.of());

        assertTrue(info.toString().contains("serial123"));
    }
}