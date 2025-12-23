package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.UserRegistrationRequest;
import com.example.backend.application.dto.request.UserUpdateRequest;
import com.example.backend.application.dto.response.UserResponse;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_shouldCreateUserWithEncryptedPassword() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername("testuser");
        request.setRoleName("USER");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        Role userRole = new Role();
        userRole.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail(request.getEmail());
        saved.setUsername(request.getUsername());
        saved.setRoles(Set.of(userRole));

        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrowExceptionWhenEmailExists() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_shouldUpdateUserFields() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("old@example.com");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("new@example.com");
        request.setFirstName("New");
        request.setLastName("Name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        User saved = new User();
        saved.setId(userId);
        saved.setEmail("new@example.com");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.update(userId, request);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void setActive_shouldUpdateUserActiveStatus() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setIsActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.setActive(userId, false);

        assertFalse(user.getIsActive());
        verify(userRepository, times(1)).save(user);
    }
}

