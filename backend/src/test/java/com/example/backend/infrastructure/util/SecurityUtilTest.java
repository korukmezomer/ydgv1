package com.example.backend.infrastructure.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityUtilTest {

    @Test
    void getCurrentUserEmail_shouldReturnNullWhenAuthenticationIsNull() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
        
        String result = SecurityUtil.getCurrentUserEmail();
        
        assertNull(result);
    }

    @Test
    void getCurrentUserEmail_shouldReturnNullWhenPrincipalIsNotString() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new Object());
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        String result = SecurityUtil.getCurrentUserEmail();
        
        assertNull(result);
    }

    @Test
    void getCurrentUserEmail_shouldReturnEmailWhenPrincipalIsString() {
        String email = "test@example.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(email);
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        String result = SecurityUtil.getCurrentUserEmail();
        
        assertEquals(email, result);
    }

    @Test
    void getCurrentUserId_shouldReturnNull() {
        // getCurrentUserId is currently a placeholder that returns null
        Long result = SecurityUtil.getCurrentUserId();
        assertNull(result);
    }

    @Test
    void getCurrentUserEmail_shouldHandleNullAuthentication() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
        
        String result = SecurityUtil.getCurrentUserEmail();
        
        assertNull(result);
    }
}

