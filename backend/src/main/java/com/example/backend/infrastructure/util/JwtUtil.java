package com.example.backend.infrastructure.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, Long userId, java.util.Set<String> roller) {
        try {
            logger.debug("Generating token for email: {}, userId: {}, roles: {}", email, userId, roller);
            String token = Jwts.builder()
                    .subject(email)
                    .claim("userId", userId)
                    .claim("roller", new java.util.ArrayList<>(roller))
                    .claim("kullaniciAdi", email.split("@")[0]) // Basit kullanıcı adı
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getSigningKey())
                    .compact();
            logger.debug("Token generated successfully");
            return token;
        } catch (Exception e) {
            logger.error("Error generating token for email: {}", email, e);
            throw new RuntimeException("Token oluşturulurken hata oluştu", e);
        }
    }

    public String extractEmail(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            logger.error("Error extracting email from token", e);
            return null;
        }
    }

    public Long extractUserId(String token) {
        try {
            return extractClaim(token, claims -> claims.get("userId", Long.class));
        } catch (Exception e) {
            logger.error("Error extracting user ID from token", e);
            return null;
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            logger.error("Error extracting expiration from token", e);
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            logger.error("Error extracting claim from token", e);
            throw new RuntimeException("Token'dan claim çıkarılırken hata oluştu", e);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Error parsing token claims", e);
            throw new RuntimeException("Token parse edilemedi", e);
        }
    }

    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            if (expiration == null) {
                return true;
            }
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.error("Error checking token expiration", e);
            return true;
        }
    }

    public Boolean validateToken(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            if (tokenEmail == null) {
                logger.warn("Token email is null");
                return false;
            }
            boolean isValid = tokenEmail.equals(email) && !isTokenExpired(token);
            logger.debug("Token validation result for email {}: {}", email, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error validating token for email: {}", email, e);
            return false;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object rollerObj = claims.get("roller");
            logger.debug("Extracting roles from token, rollerObj type: {}", rollerObj != null ? rollerObj.getClass().getName() : "null");
            
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
                    logger.debug("Extracted roles from token: {}", roles);
                    return roles;
                } else if (rollerObj instanceof String) {
                    // Handle single role as string
                    logger.debug("Single role found: {}", rollerObj);
                    return Collections.singletonList((String) rollerObj);
                } else {
                    logger.warn("Unexpected role object type: {}", rollerObj.getClass().getName());
                }
            } else {
                logger.warn("No roles found in token claims");
            }
        } catch (Exception e) {
            logger.error("Error extracting roles from token", e);
        }
        return Collections.emptyList();
    }
}
