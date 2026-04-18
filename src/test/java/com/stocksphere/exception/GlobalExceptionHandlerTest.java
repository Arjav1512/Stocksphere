package com.stocksphere.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ── 400 validation errors ──────────────────────────────────────────────────

    @Test
    void testIllegalArgumentReturns400() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleIllegalArgument(new IllegalArgumentException("bad symbol"));
        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void testIllegalArgumentBodyHasErrorCode() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleIllegalArgument(new IllegalArgumentException("test"));
        assertEquals("VALIDATION_ERROR", resp.getBody().get("error"));
    }

    @Test
    void testIllegalArgumentMessagePassedThrough() {
        String msg = "Stock symbol is required and cannot be blank";
        ResponseEntity<Map<String, Object>> resp =
                handler.handleIllegalArgument(new IllegalArgumentException(msg));
        assertEquals(msg, resp.getBody().get("message"));
    }

    @Test
    void testMissingParamReturns400() throws Exception {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("symbol", "String");
        ResponseEntity<Map<String, Object>> resp = handler.handleMissingParam(ex);
        assertEquals(400, resp.getStatusCode().value());
        assertEquals("MISSING_PARAMETER", resp.getBody().get("error"));
    }

    @Test
    void testMissingParamMessageContainsParamName() throws Exception {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("symbol", "String");
        ResponseEntity<Map<String, Object>> resp = handler.handleMissingParam(ex);
        String message = (String) resp.getBody().get("message");
        assertTrue(message.contains("symbol"), "Message should reference the missing param name");
    }

    // ── 500 internal errors — must NOT leak exception details ─────────────────

    @Test
    void testRuntimeExceptionReturns500() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleRuntime(new RuntimeException("NullPointerException at line 42"));
        assertEquals(500, resp.getStatusCode().value());
    }

    @Test
    void testRuntimeExceptionDoesNotLeakMessage() {
        String internalDetail = "NullPointerException at com.stocksphere.service.TrendService:99";
        ResponseEntity<Map<String, Object>> resp =
                handler.handleRuntime(new RuntimeException(internalDetail));
        String responseMessage = (String) resp.getBody().get("message");
        assertFalse(responseMessage.contains("NullPointerException"),
                "Internal exception class names must not appear in response");
        assertFalse(responseMessage.contains("TrendService"),
                "Internal class names must not appear in response");
        assertFalse(responseMessage.contains("line 42"),
                "Internal line references must not appear in response");
        assertFalse(responseMessage.contains(internalDetail),
                "Raw exception message must not be passed through");
    }

    @Test
    void testRuntimeExceptionHasErrorCode() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleRuntime(new RuntimeException("internal detail"));
        assertEquals("INTERNAL_ERROR", resp.getBody().get("error"));
    }

    @Test
    void testGenericExceptionReturns500() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleGeneric(new Exception("some checked exception detail"));
        assertEquals(500, resp.getStatusCode().value());
    }

    @Test
    void testGenericExceptionDoesNotLeakMessage() {
        String internalDetail = "Connection refused to database at 10.0.0.1:5432";
        ResponseEntity<Map<String, Object>> resp =
                handler.handleGeneric(new Exception(internalDetail));
        String responseMessage = (String) resp.getBody().get("message");
        assertFalse(responseMessage.contains("database"),
                "Internal infrastructure details must not appear in response");
        assertFalse(responseMessage.contains("10.0.0.1"),
                "Internal IP addresses must not appear in response");
        assertFalse(responseMessage.contains(internalDetail));
    }

    @Test
    void testGenericExceptionHasErrorCode() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleGeneric(new Exception("detail"));
        assertEquals("SERVER_ERROR", resp.getBody().get("error"));
    }

    // ── Response envelope structure ────────────────────────────────────────────

    @Test
    void testResponseBodyHasTimestamp() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleIllegalArgument(new IllegalArgumentException("test"));
        assertNotNull(resp.getBody().get("timestamp"));
        assertFalse(resp.getBody().get("timestamp").toString().isBlank());
    }

    @Test
    void testResponseBodyHasStatusField() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleIllegalArgument(new IllegalArgumentException("test"));
        assertEquals(400, resp.getBody().get("status"));
    }

    @Test
    void testResponseBodyHasAllRequiredFields() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleRuntime(new RuntimeException("err"));
        Map<String, Object> body = resp.getBody();
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("message"));
    }
}
