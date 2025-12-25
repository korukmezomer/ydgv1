package com.example.backend.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testSecret = "test-secret-key-for-jwt-util-test-minimum-256-bits-required-here-for-security";
    private Long testExpiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    @Test
    void testGenerateToken() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        roles.add("ADMIN");

        String token = jwtUtil.generateToken(email, userId, roles);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractEmail() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        String token = jwtUtil.generateToken(email, userId, roles);
        String extractedEmail = jwtUtil.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void testExtractUserId() {
        String email = "test@example.com";
        Long userId = 123L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        String token = jwtUtil.generateToken(email, userId, roles);
        Long extractedUserId = jwtUtil.extractUserId(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void testExtractExpiration() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        String token = jwtUtil.generateToken(email, userId, roles);
        Date expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testExtractRoles() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        roles.add("ADMIN");
        roles.add("WRITER");

        String token = jwtUtil.generateToken(email, userId, roles);
        List<String> extractedRoles = jwtUtil.extractRoles(token);

        assertNotNull(extractedRoles);
        assertEquals(3, extractedRoles.size());
        assertTrue(extractedRoles.contains("USER"));
        assertTrue(extractedRoles.contains("ADMIN"));
        assertTrue(extractedRoles.contains("WRITER"));
    }

    @Test
    void testExtractRolesEmpty() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();

        String token = jwtUtil.generateToken(email, userId, roles);
        List<String> extractedRoles = jwtUtil.extractRoles(token);

        assertNotNull(extractedRoles);
        assertTrue(extractedRoles.isEmpty());
    }

    @Test
    void testValidateToken() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        String token = jwtUtil.generateToken(email, userId, roles);
        boolean isValid = jwtUtil.validateToken(token, email);

        assertTrue(isValid);
    }

    @Test
    void testValidateTokenWrongEmail() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        String token = jwtUtil.generateToken(email, userId, roles);
        boolean isValid = jwtUtil.validateToken(token, "wrong@example.com");

        assertFalse(isValid);
    }

    @Test
    void testIsTokenExpired() {
        // Create a token with very short expiration
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // Already expired
        
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        String token = jwtUtil.generateToken(email, userId, roles);
        boolean isExpired = jwtUtil.isTokenExpired(token);

        assertTrue(isExpired);
    }

    @Test
    void testIsTokenNotExpired() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        String token = jwtUtil.generateToken(email, userId, roles);
        boolean isExpired = jwtUtil.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void testExtractEmailFromInvalidToken() {
        String invalidToken = "invalid.token.here";
        String email = jwtUtil.extractEmail(invalidToken);

        assertNull(email);
    }

    @Test
    void testExtractUserIdFromInvalidToken() {
        String invalidToken = "invalid.token.here";
        Long userId = jwtUtil.extractUserId(invalidToken);

        assertNull(userId);
    }

    @Test
    void testExtractRolesFromInvalidToken() {
        String invalidToken = "invalid.token.here";
        List<String> roles = jwtUtil.extractRoles(invalidToken);

        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        boolean isValid = jwtUtil.validateToken(invalidToken, "test@example.com");

        assertFalse(isValid);
    }
}

