package com.example.backend.infrastructure.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        // Bu method JWT token'dan user ID'yi çıkarır
        // Şimdilik authentication'dan email alıp, email'den user ID bulunabilir
        // Veya JWT token'dan direkt user ID çıkarılabilir
        return null; // Placeholder - implement edilmeli
    }
}

