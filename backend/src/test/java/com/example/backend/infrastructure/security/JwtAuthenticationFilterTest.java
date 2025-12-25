package com.example.backend.infrastructure.security;

import com.example.backend.infrastructure.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_InvalidAuthorizationHeader() throws ServletException, IOException {
        request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader("Authorization", "InvalidToken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        String token = "valid.token.here";
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.validateToken(token, email)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn(userId);
        when(jwtUtil.extractRoles(token)).thenReturn(java.util.Arrays.asList("USER"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(email, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        String token = "invalid.token.here";
        String email = "test@example.com";

        request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.validateToken(token, email)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_TokenWithMultipleRoles() throws ServletException, IOException {
        String token = "valid.token.here";
        String email = "test@example.com";
        Long userId = 1L;

        request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.validateToken(token, email)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn(userId);
        when(jwtUtil.extractRoles(token)).thenReturn(java.util.Arrays.asList("USER", "ADMIN", "WRITER"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(3, SecurityContextHolder.getContext().getAuthentication().getAuthorities().size());
    }

    @Test
    void testDoFilterInternal_TokenWithNoRoles() throws ServletException, IOException {
        String token = "valid.token.here";
        String email = "test@example.com";
        Long userId = 1L;

        request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.validateToken(token, email)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn(userId);
        when(jwtUtil.extractRoles(token)).thenReturn(java.util.Collections.emptyList());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
    }

    @Test
    void testDoFilterInternal_ExceptionDuringTokenExtraction() throws ServletException, IOException {
        String token = "invalid.token.here";

        request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.extractEmail(token)).thenThrow(new RuntimeException("Token parse error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_EmailIsNull() throws ServletException, IOException {
        String token = "token.without.email";

        request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.extractEmail(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_AlreadyAuthenticated() throws ServletException, IOException {
        String token = "valid.token.here";
        String email = "test@example.com";

        // Set existing authentication
        org.springframework.security.core.Authentication existingAuth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "existing@example.com", null, null);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        // validateToken won't be called because authentication already exists

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Existing authentication should remain
        assertEquals("existing@example.com", 
            SecurityContextHolder.getContext().getAuthentication().getName());
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }
}

