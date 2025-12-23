package com.example.backend.infrastructure.security;

import com.example.backend.application.exception.UnauthorizedException;
import com.example.backend.infrastructure.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class JwtTokenProvider {

    @Autowired
    private JwtUtil jwtUtil;

    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            // Eğer authentication null ise, request header'dan token'ı al
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    return jwtUtil.extractUserId(token);
                }
            }
            throw new UnauthorizedException("Kullanıcı kimliği alınamadı");
        }

        // Authentication'dan user ID'yi al
        // JwtAuthenticationFilter'da user ID'yi details'e ekliyoruz
        if (authentication.getDetails() instanceof Long) {
            return (Long) authentication.getDetails();
        }

        // Alternatif: Email'den user ID bul
        String email = authentication.getName();
        if (email != null) {
            // Request header'dan token'ı al ve user ID'yi çıkar
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    return jwtUtil.extractUserId(token);
                }
            }
        }

        throw new UnauthorizedException("Kullanıcı kimliği alınamadı");
    }
}

