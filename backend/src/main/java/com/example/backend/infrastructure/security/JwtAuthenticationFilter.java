package com.example.backend.infrastructure.security;

import com.example.backend.infrastructure.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token = authHeader.substring(7);
            final String email = jwtUtil.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.validateToken(token, email)) {
                    Long userId = jwtUtil.extractUserId(token);
                    
                    // Rolleri token'dan çıkar
                    List<SimpleGrantedAuthority> authorities = extractAuthoritiesFromToken(token);
                    logger.debug("Setting authentication for user: " + email + " with authorities: " + authorities);
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities
                    );
                    authToken.setDetails(userId); // User ID'yi details'e ekle
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set in SecurityContext");
                } else {
                    logger.warn("Token validation failed for email: " + email);
                }
            } else {
                if (email == null) {
                    logger.warn("Email is null in token");
                }
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    logger.debug("Authentication already exists in SecurityContext");
                }
            }
        } catch (Exception e) {
            logger.error("JWT token validation failed", e);
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> extractAuthoritiesFromToken(String token) {
        try {
            List<String> roller = jwtUtil.extractRoles(token);
            logger.debug("Extracted roles from token: " + roller);
            if (roller != null && !roller.isEmpty()) {
                List<SimpleGrantedAuthority> authorities = roller.stream()
                        .map(role -> {
                            String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                            logger.debug("Creating authority: " + authority);
                            return new SimpleGrantedAuthority(authority);
                        })
                        .collect(Collectors.toList());
                logger.debug("Created authorities: " + authorities);
                return authorities;
            } else {
                logger.warn("No roles found in token or roles list is empty");
            }
        } catch (Exception e) {
            logger.error("Failed to extract roles from token", e);
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}

