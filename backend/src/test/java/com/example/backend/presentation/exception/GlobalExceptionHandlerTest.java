package com.example.backend.presentation.exception;

import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ForbiddenException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleResourceNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().get("message"));
        assertEquals("404 NOT_FOUND", response.getBody().get("status"));
    }

    @Test
    void testHandleBadRequestException() {
        BadRequestException exception = new BadRequestException("Bad request");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadRequestException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request", response.getBody().get("message"));
        assertEquals("400 BAD_REQUEST", response.getBody().get("status"));
    }

    @Test
    void testHandleUnauthorizedException() {
        UnauthorizedException exception = new UnauthorizedException("Unauthorized");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUnauthorizedException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().get("message"));
        assertEquals("401 UNAUTHORIZED", response.getBody().get("status"));
    }

    @Test
    void testHandleForbiddenException() {
        ForbiddenException exception = new ForbiddenException("Forbidden");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleForbiddenException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody().get("message"));
        assertEquals("403 FORBIDDEN", response.getBody().get("status"));
    }

    @Test
    void testHandleAuthenticationException() {
        AuthenticationException exception = new AuthenticationException("Authentication failed") {};
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAuthenticationException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().get("message").contains("Authentication failed"));
        assertEquals("401 UNAUTHORIZED", response.getBody().get("status"));
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccessDeniedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().get("message").contains("Access denied"));
        assertEquals("403 FORBIDDEN", response.getBody().get("status"));
    }

    @Test
    void testHandleMissingRequestHeaderException() {
        MissingRequestHeaderException exception = new MissingRequestHeaderException("Authorization", null);
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleMissingRequestHeaderException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().get("message").contains("Authorization"));
        assertEquals("401 UNAUTHORIZED", response.getBody().get("status"));
    }

    @Test
    void testHandleRuntimeException() {
        RuntimeException exception = new RuntimeException("Runtime error");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Runtime error", response.getBody().get("message"));
        assertEquals("400 BAD_REQUEST", response.getBody().get("status"));
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "email", "Email is required"));
        bindingResult.addError(new FieldError("object", "password", "Password is required"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().get("message"));
        assertEquals("400 BAD_REQUEST", response.getBody().get("status"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertNotNull(errors);
        assertEquals("Email is required", errors.get("email"));
        assertEquals("Password is required", errors.get("password"));
    }

    @Test
    void testHandleGenericException() {
        Exception exception = new Exception("Generic error");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
        assertEquals("500 INTERNAL_SERVER_ERROR", response.getBody().get("status"));
    }
}

