package com.devicebase.http;

import com.devicebase.model.DeviceInfo;
import com.devicebase.model.OperationResult;
import com.devicebase.model.Point;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiResponse class.
 */
class ApiResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldDeserializeSuccessResponse() throws Exception {
        String json = """
            {
                "success": true,
                "data": {"key": "value"},
                "error": null,
                "total": 10
            }
            """;

        ApiResponse<Map> response = objectMapper.readValue(json,
            objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, Map.class));

        assertTrue(response.isSuccess());
        assertEquals("value", response.getData().get("key"));
        assertNull(response.getError());
        assertEquals(10, response.getTotal());
    }

    @Test
    void shouldDeserializeErrorResponse() throws Exception {
        String json = """
            {
                "success": false,
                "data": null,
                "error": "Something went wrong",
                "total": null
            }
            """;

        ApiResponse<Object> response = objectMapper.readValue(json,
            objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, Object.class));

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Something went wrong", response.getError());
        assertNull(response.getTotal());
    }

    @Test
    void toString_shouldContainRelevantFields() {
        ApiResponse<String> response = new ApiResponse<>(true, "test-data", null, 5);

        String result = response.toString();

        assertTrue(result.contains("success=true"));
        assertTrue(result.contains("total=5"));
    }
}