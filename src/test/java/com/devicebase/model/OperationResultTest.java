package com.devicebase.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OperationResult class.
 */
class OperationResultTest {

    @Test
    void constructor_shouldCreateOperationResult() {
        Map<String, Object> data = Map.of("message", "Success");
        OperationResult result = new OperationResult(true, data);

        assertTrue(result.isSuccess());
        assertEquals("Success", result.get("message"));
    }

    @Test
    void constructor_withNullData_shouldUseEmptyMap() {
        OperationResult result = new OperationResult(true, null);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void fromMap_withSuccess_shouldSetSuccessTrue() {
        Map<String, Object> data = Map.of("success", true);
        OperationResult result = OperationResult.fromMap(data);

        assertTrue(result.isSuccess());
    }

    @Test
    void fromMap_withSuccessFalse_shouldSetSuccessFalse() {
        Map<String, Object> data = Map.of("success", false);
        OperationResult result = OperationResult.fromMap(data);

        assertFalse(result.isSuccess());
    }

    @Test
    void fromMap_withNullSuccess_shouldDefaultToTrue() {
        Map<String, Object> data = Map.of("message", "test");
        OperationResult result = OperationResult.fromMap(data);

        assertTrue(result.isSuccess());
    }

    @Test
    void fromMap_withNullData_shouldReturnSuccessTrue() {
        OperationResult result = OperationResult.fromMap(null);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void equals_withSameValues_shouldBeEqual() {
        OperationResult result1 = new OperationResult(true, Map.of("key", "value1"));
        OperationResult result2 = new OperationResult(true, Map.of("key", "value2"));

        assertEquals(result1, result2);
    }

    @Test
    void equals_withDifferentSuccess_shouldNotBeEqual() {
        OperationResult result1 = new OperationResult(true, Map.of());
        OperationResult result2 = new OperationResult(false, Map.of());

        assertNotEquals(result1, result2);
    }

    @Test
    void toString_shouldContainSuccessStatus() {
        OperationResult result = new OperationResult(true, Map.of());

        assertTrue(result.toString().contains("success=true"));
    }
}