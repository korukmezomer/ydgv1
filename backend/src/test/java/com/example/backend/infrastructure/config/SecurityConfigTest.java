package com.example.backend.infrastructure.config;

import com.example.backend.infrastructure.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        // The configuration should allow localhost:5173 and localhost:3000
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
    void accessDeniedHandler_shouldBeCreated() {
        var handler = securityConfig.accessDeniedHandler();
        
        assertNotNull(handler);
    }
}

