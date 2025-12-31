package com.example.backend.application.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void badRequestException_shouldCreateWithMessage() {
        String message = "Bad request error";
        BadRequestException exception = new BadRequestException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void resourceNotFoundException_shouldCreateWithMessage() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void resourceNotFoundException_shouldCreateWithResourceNameAndId() {
        String resourceName = "User";
        Long id = 123L;
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, id);
        
        assertNotNull(exception);
        assertEquals("User with id 123 not found", exception.getMessage());
    }

    @Test
    void resourceNotFoundException_shouldCreateWithResourceNameAndIdentifier() {
        String resourceName = "Story";
        String identifier = "slug-123";
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, identifier);
        
        assertNotNull(exception);
        assertEquals("Story with identifier slug-123 not found", exception.getMessage());
    }

    @Test
    void unauthorizedException_shouldCreateWithMessage() {
        String message = "Unauthorized access";
        UnauthorizedException exception = new UnauthorizedException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void unauthorizedException_shouldCreateWithDefaultMessage() {
        UnauthorizedException exception = new UnauthorizedException();
        
        assertNotNull(exception);
        assertEquals("Unauthorized access", exception.getMessage());
    }

    @Test
    void forbiddenException_shouldCreateWithMessage() {
        String message = "Access forbidden";
        ForbiddenException exception = new ForbiddenException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void forbiddenException_shouldCreateWithDefaultMessage() {
        ForbiddenException exception = new ForbiddenException();
        
        assertNotNull(exception);
        assertEquals("Access forbidden", exception.getMessage());
    }
}

