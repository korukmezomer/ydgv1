package com.example.backend.infrastructure.security;

import com.example.backend.application.exception.UnauthorizedException;
import com.example.backend.infrastructure.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserIdFromAuthentication_shouldReturnUserIdFromDetails() {
        Long userId = 123L;
        when(authentication.getDetails()).thenReturn(userId);
        
        Long result = jwtTokenProvider.getUserIdFromAuthentication(authentication);
        
        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromAuthentication_shouldExtractFromTokenWhenDetailsNotLong() {
        Long userId = 123L;
        String email = "test@example.com";
        String token = "Bearer test-token";
        
        when(authentication.getDetails()).thenReturn("not-long");
        when(authentication.getName()).thenReturn(email);
        
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);
        
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.extractUserId("test-token")).thenReturn(userId);
        
        Long result = jwtTokenProvider.getUserIdFromAuthentication(authentication);
        
        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromAuthentication_shouldThrowExceptionWhenAuthenticationNullAndNoToken() {
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);
        
        when(request.getHeader("Authorization")).thenReturn(null);
        
        assertThrows(UnauthorizedException.class, () -> {
            jwtTokenProvider.getUserIdFromAuthentication(null);
        });
    }

    @Test
    void getUserIdFromAuthentication_shouldExtractFromTokenWhenAuthenticationNull() {
        Long userId = 123L;
        String token = "Bearer test-token";
        
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);
        
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.extractUserId("test-token")).thenReturn(userId);
        
        Long result = jwtTokenProvider.getUserIdFromAuthentication(null);
        
        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromAuthentication_shouldThrowExceptionWhenNoTokenAvailable() {
        String email = "test@example.com";
        
        when(authentication.getDetails()).thenReturn("not-long");
        when(authentication.getName()).thenReturn(email);
        
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);
        
        when(request.getHeader("Authorization")).thenReturn(null);
        
        assertThrows(UnauthorizedException.class, () -> {
            jwtTokenProvider.getUserIdFromAuthentication(authentication);
        });
    }

    @Test
    void getUserIdFromAuthentication_shouldHandleNullRequestAttributes() {
        RequestContextHolder.setRequestAttributes(null);
        
        assertThrows(UnauthorizedException.class, () -> {
            jwtTokenProvider.getUserIdFromAuthentication(null);
        });
    }

    @Test
    void getUserIdFromAuthentication_shouldHandleTokenWithoutBearerPrefix() {
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);
        
        when(request.getHeader("Authorization")).thenReturn("invalid-token");
        
        assertThrows(UnauthorizedException.class, () -> {
            jwtTokenProvider.getUserIdFromAuthentication(null);
        });
    }
}

