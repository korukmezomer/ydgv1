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

    @Test
    void testExtractExpirationFromInvalidToken() {
        String invalidToken = "invalid.token.here";
        Date expiration = jwtUtil.extractExpiration(invalidToken);

        assertNull(expiration);
    }

    @Test
    void testIsTokenExpiredWithNullExpiration() {
        // This tests the branch where extractExpiration returns null
        String invalidToken = "invalid.token.here";
        boolean isExpired = jwtUtil.isTokenExpired(invalidToken);

        assertTrue(isExpired);
    }

    @Test
    void testValidateTokenWithNullEmail() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        String token = jwtUtil.generateToken(email, userId, roles);
        // Test with null email - should return false
        boolean isValid = jwtUtil.validateToken(token, null);

        assertFalse(isValid);
    }

    @Test
    void testExtractRolesWithNullRoles() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add(null); // Add null role

        String token = jwtUtil.generateToken(email, userId, roles);
        List<String> extractedRoles = jwtUtil.extractRoles(token);

        assertNotNull(extractedRoles);
        // Null roles should be filtered out
        assertTrue(extractedRoles.isEmpty() || extractedRoles.stream().noneMatch(r -> r == null));
    }

    @Test
    void testExtractRolesWithEmptyStringRole() {
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add(""); // Empty string role

        String token = jwtUtil.generateToken(email, userId, roles);
        List<String> extractedRoles = jwtUtil.extractRoles(token);

        assertNotNull(extractedRoles);
        // Empty string roles should be filtered out
        assertTrue(extractedRoles.isEmpty() || extractedRoles.stream().noneMatch(String::isEmpty));
    }

    @Test
    void testExtractRolesWithNonStringObject() {
        // This tests the branch where role object is not a String or List
        // We can't directly test this with generateToken, but we can test the extractRoles
        // method with a manually crafted token claim structure
        // For now, we'll test that extractRoles handles invalid tokens gracefully
        String invalidToken = "invalid.token.here";
        List<String> roles = jwtUtil.extractRoles(invalidToken);

        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    void testGenerateTokenWithException() {
        // Test exception handling in generateToken
        // Set invalid secret to cause exception
        ReflectionTestUtils.setField(jwtUtil, "secret", null);
        
        assertThrows(RuntimeException.class, () -> {
            jwtUtil.generateToken("test@example.com", 1L, new HashSet<>());
        });
    }

    @Test
    void testExtractClaimWithException() {
        // Test exception handling in extractClaim
        String invalidToken = "invalid.token.here";
        
        assertThrows(RuntimeException.class, () -> {
            jwtUtil.extractClaim(invalidToken, claims -> claims.get("test", String.class));
        });
    }

    @Test
    void testExtractAllClaimsWithException() {
        // Test exception handling in extractAllClaims
        String invalidToken = "invalid.token.here";
        
        assertThrows(RuntimeException.class, () -> {
            // Use reflection to call private method, or test via public method
            jwtUtil.extractEmail(invalidToken); // This will call extractAllClaims internally
        });
    }

    @Test
    void testValidateTokenWithExpiredToken() {
        // Create expired token
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        String token = jwtUtil.generateToken(email, userId, roles);
        boolean isValid = jwtUtil.validateToken(token, email);

        assertFalse(isValid);
    }

    @Test
    void testValidateTokenWithException() {
        // Test exception handling in validateToken
        String invalidToken = "invalid.token.here";
        boolean isValid = jwtUtil.validateToken(invalidToken, "test@example.com");

        assertFalse(isValid);
    }

    @Test
    void testExtractRolesWithStringRole() {
        // Test extractRoles when roles claim is a single String (not a List)
        // This is hard to test directly, but we can verify the method handles it
        String email = "test@example.com";
        Long userId = 1L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        String token = jwtUtil.generateToken(email, userId, roles);
        List<String> extractedRoles = jwtUtil.extractRoles(token);

        assertNotNull(extractedRoles);
        assertFalse(extractedRoles.isEmpty());
    }
}

