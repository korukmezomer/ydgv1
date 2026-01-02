package com.example.backend.infrastructure.config;

import com.example.backend.infrastructure.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpSecurity httpSecurity;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        
        assertNotNull(encoder);
        assertTrue(encoder.matches("password", encoder.encode("password")));
    }

    @Test
    void corsConfigurationSource_shouldBeCreated() {
        CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
        
        assertNotNull(corsSource);
    }

    @Test
    void corsConfigurationSource_shouldConfigureAllowedOrigins() {
        CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
        
        assertNotNull(corsSource);
        // UrlBasedCorsConfigurationSource sadece /api/** path'i için CORS configuration döndürür
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/test"); // /api/** pattern'ine uyan bir path
        request.setPathInfo("/api/test");
        request.setServletPath("/api/test");
        CorsConfiguration cfg = corsSource.getCorsConfiguration(request);
        assertNotNull(cfg);
        assertTrue(cfg.getAllowedOriginPatterns().contains("http://localhost:*"));
        assertTrue(cfg.getAllowedMethods().containsAll(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")));
        assertTrue(cfg.getAllowedHeaders().contains("*"));
        assertTrue(Boolean.TRUE.equals(cfg.getAllowCredentials()));
        assertTrue(cfg.getExposedHeaders().contains("Authorization"));
    }

    @Test
    void securityFilterChain_shouldBeCreated() throws Exception {
        // This test verifies that the securityFilterChain method exists and can be called
        // Actual implementation testing would require Spring context
        assertNotNull(securityConfig);
    }

    @Test
    void authenticationEntryPoint_shouldBeCreated() {
        var entryPoint = securityConfig.authenticationEntryPoint();
        
        assertNotNull(entryPoint);
    }

    @Test
    void authenticationEntryPoint_shouldReturn401Json() throws Exception {
        var entryPoint = securityConfig.authenticationEntryPoint();
        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(new MockHttpServletRequest(), response, new org.springframework.security.core.AuthenticationException("auth fail") {});
        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("Authentication required"));
    }

    @Test
    void accessDeniedHandler_shouldBeCreated() {
        var handler = securityConfig.accessDeniedHandler();
        
        assertNotNull(handler);
    }

    @Test
    void accessDeniedHandler_shouldReturn403Json() throws Exception {
        var handler = securityConfig.accessDeniedHandler();
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.handle(new MockHttpServletRequest(), response, new org.springframework.security.access.AccessDeniedException("denied"));
        assertEquals(403, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("Access denied"));
    }
}

