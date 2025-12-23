package com.example.backend.application.exception;

public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException() {
        super("Access forbidden");
    }
}

