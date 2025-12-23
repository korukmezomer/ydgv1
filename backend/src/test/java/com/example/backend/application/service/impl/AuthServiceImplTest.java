package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.UserLoginRequest;
import com.example.backend.application.dto.response.JwtResponse;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.infrastructure.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_shouldReturnJwtResponseWithToken() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("encoded_password");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));

        when(userRepository.findActiveByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anySet())).thenReturn("jwt_token");

        JwtResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        verify(jwtUtil, times(1)).generateToken(eq("test@example.com"), eq(1L), anySet());
    }

    @Test
    void login_shouldThrowExceptionWhenUserNotFound() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password");

        when(userRepository.findActiveByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    void login_shouldThrowExceptionWhenPasswordIncorrect() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong_password");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded_password");

        when(userRepository.findActiveByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }
}

