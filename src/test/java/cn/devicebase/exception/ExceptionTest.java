package cn.devicebase.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for exception classes.
 */
class ExceptionTest {

    @Test
    void deviceBaseException_withMessage_shouldStoreMessage() {
        DeviceBaseException exception = new DeviceBaseException("Test error");

        assertEquals("Test error", exception.getMessage());
        assertNull(exception.getStatusCode());
    }

    @Test
    void deviceBaseException_withMessageAndStatusCode_shouldStoreBoth() {
        DeviceBaseException exception = new DeviceBaseException("Test error", 500);

        assertEquals("Test error", exception.getMessage());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void deviceBaseException_withCause_shouldStoreCause() {
        RuntimeException cause = new RuntimeException("Original error");
        DeviceBaseException exception = new DeviceBaseException("Test error", cause);

        assertEquals("Test error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void deviceNotFoundException_shouldHave404Status() {
        DeviceNotFoundException exception = new DeviceNotFoundException("Device not found");

        assertEquals("Device not found", exception.getMessage());
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void validationException_shouldHave422Status() {
        ValidationException exception = new ValidationException("Invalid input");

        assertEquals("Invalid input", exception.getMessage());
        assertEquals(422, exception.getStatusCode());
    }

    @Test
    void authenticationException_shouldHave401Status() {
        AuthenticationException exception = new AuthenticationException("Invalid API key");

        assertEquals("Invalid API key", exception.getMessage());
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void deviceNotFoundException_withCause_shouldStoreBoth() {
        RuntimeException cause = new RuntimeException("Original error");
        DeviceNotFoundException exception = new DeviceNotFoundException("Device not found", cause);

        assertEquals("Device not found", exception.getMessage());
        assertEquals(404, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void allExceptions_shouldBeCatchableAsDeviceBaseException() {
        DeviceBaseException e1 = new DeviceBaseException("Test");
        DeviceNotFoundException e2 = new DeviceNotFoundException("Test");
        ValidationException e3 = new ValidationException("Test");
        AuthenticationException e4 = new AuthenticationException("Test");

        assertTrue(e1 instanceof Exception);
        assertTrue(e2 instanceof DeviceBaseException);
        assertTrue(e3 instanceof DeviceBaseException);
        assertTrue(e4 instanceof DeviceBaseException);
    }
}