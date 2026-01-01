package com.example.backend.infrastructure.config;

import com.example.backend.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Localhost ve Docker network'ünden erişim için CORS ayarları
        // setAllowedOriginPatterns kullanarak daha esnek origin kontrolü
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://host.docker.internal:*",  // Docker Desktop için
            "http://172.17.*:*",  // Docker bridge IP range
            "http://172.20.*:*"   // Docker network IP range
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/haberler/**").permitAll() // Public haberler için
                .requestMatchers("/api/kategoriler/**").permitAll()
                .requestMatchers("/api/dosyalar/**").permitAll() // Dosya erişimi için
                // Public GET endpoints
                .requestMatchers(HttpMethod.GET, "/api/yorumlar/**").permitAll() // Comment read endpoints
                .requestMatchers(HttpMethod.GET, "/api/etiketler/**").permitAll() // Tag read endpoints
                .requestMatchers(HttpMethod.GET, "/api/listeler/*").permitAll() // List read by ID
                .requestMatchers(HttpMethod.GET, "/api/listeler/slug/**").permitAll() // List read by slug
                .requestMatchers(HttpMethod.GET, "/api/takip/*/takipci-sayisi").permitAll() // Follower count
                .requestMatchers(HttpMethod.GET, "/api/takip/*/takip-edilen-sayisi").permitAll() // Following count
                .requestMatchers(HttpMethod.GET, "/api/takip/*/takipciler").permitAll() // Followers list
                .requestMatchers(HttpMethod.GET, "/api/takip/*/takip-edilenler").permitAll() // Following list
                .requestMatchers(HttpMethod.GET, "/api/begeniler/haber/*/sayi").permitAll() // Like count
                .requestMatchers(HttpMethod.GET, "/api/kullanicilar/**").permitAll() // User read endpoints
                .requestMatchers(HttpMethod.GET, "/api/yazar-profilleri/**").permitAll() // Author profile read endpoints
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Authentication required\",\"status\":\"401 UNAUTHORIZED\"}");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Access denied\",\"status\":\"403 FORBIDDEN\"}");
        };
    }
}
