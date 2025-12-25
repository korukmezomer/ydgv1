package com.example.backend.infrastructure.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, Long userId, java.util.Set<String> roller) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("roller", new java.util.ArrayList<>(roller))
                .claim("kullaniciAdi", email.split("@")[0]) // Basit kullanıcı adı
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object rollerObj = claims.get("roller");
            if (rollerObj != null) {
                if (rollerObj instanceof List) {
                    List<?> rollerList = (List<?>) rollerObj;
                    List<String> roles = rollerList.stream()
                            .map(obj -> {
                                if (obj == null) return null;
                                // Handle both String and other types
                                if (obj instanceof String) {
                                    return (String) obj;
                                }
                                return obj.toString();
                            })
                            .filter(role -> role != null && !role.isEmpty())
                            .collect(java.util.stream.Collectors.toList());
                    return roles;
                } else if (rollerObj instanceof String) {
                    // Handle single role as string
                    return Collections.singletonList((String) rollerObj);
                }
            }
        } catch (Exception e) {
            // Log error for debugging
            System.err.println("Error extracting roles from token: " + e.getMessage());
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
