package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.UserRegistrationRequest;
import com.example.backend.application.dto.request.UserUpdateRequest;
import com.example.backend.application.dto.response.UserResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
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

import java.util.List;
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

    @Test
    void register_shouldThrowExceptionWhenUsernameExists() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setUsername("existinguser");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findById_shouldReturnUserResponse() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.findById(userId));
    }

    @Test
    void findByEmail_shouldReturnUserResponse() {
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setUsername("testuser");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserResponse response = userService.findByEmail(email);

        assertNotNull(response);
        assertEquals(email, response.getEmail());
    }

    @Test
    void findByEmail_shouldThrowExceptionWhenNotFound() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.findByEmail(email));
    }

    @Test
    void update_shouldThrowExceptionWhenEmailExists() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("old@example.com");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("existing@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.update(userId, request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_shouldUpdatePasswordWhenProvided() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPassword("old_encoded_password");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com");
        request.setPassword("newpassword123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword123")).thenReturn("new_encoded_password");

        User saved = new User();
        saved.setId(userId);
        saved.setPassword("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        userService.update(userId, request);

        verify(passwordEncoder, times(1)).encode("newpassword123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void delete_shouldSetUserInactive() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setIsActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.delete(userId);

        assertFalse(user.getIsActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void delete_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(userId));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findAll_shouldReturnPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponse> response = userService.findAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void search_shouldReturnPageOfMatchingUsers() {
        String searchTerm = "test";
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.search("%test%", pageable)).thenReturn(userPage);

        Page<UserResponse> response = userService.search(searchTerm, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(userRepository, times(1)).search("%test%", pageable);
    }

    @Test
    void register_shouldMapAdminRoleToUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername("testuser");
        request.setRoleName("ADMIN");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        Role userRole = new Role();
        userRole.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail(request.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        verify(roleRepository, times(1)).findByName("USER");
        verify(roleRepository, never()).findByName("ADMIN");
    }

    @Test
    void register_shouldMapReaderRoleToUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername("testuser");
        request.setRoleName("READER");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        Role userRole = new Role();
        userRole.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail(request.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        verify(roleRepository, times(1)).findByName("USER");
    }

    @Test
    void register_shouldMapAuthorRoleToWriter() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername("testuser");
        request.setRoleName("AUTHOR");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        Role writerRole = new Role();
        writerRole.setName("WRITER");
        when(roleRepository.findByName("WRITER")).thenReturn(Optional.of(writerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail(request.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        verify(roleRepository, times(1)).findByName("WRITER");
    }

    @Test
    void register_shouldUseUserRoleWhenRoleNameIsNull() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername("testuser");
        request.setRoleName(null);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        Role userRole = new Role();
        userRole.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail(request.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        verify(roleRepository, times(1)).findByName("USER");
    }

    @Test
    void register_shouldUseUserRoleWhenRoleNameIsEmpty() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername("testuser");
        request.setRoleName("");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        Role userRole = new Role();
        userRole.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail(request.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        verify(roleRepository, times(1)).findByName("USER");
    }

    @Test
    void register_shouldThrowExceptionWhenRoleNotFound() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername("testuser");
        request.setRoleName("WRITER");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(roleRepository.findByName("WRITER")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldHandleNullUsername() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername(null);
        request.setRoleName("USER");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(null)).thenReturn(false);

        Role userRole = new Role();
        userRole.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail(request.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_shouldNotCheckEmailWhenEmailUnchanged() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com"); // Same email
        request.setFirstName("New");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User saved = new User();
        saved.setId(userId);
        saved.setEmail("test@example.com");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.update(userId, request);

        assertNotNull(response);
        verify(userRepository, never()).existsByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_shouldNotUpdatePasswordWhenPasswordIsNull() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPassword("old_encoded_password");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com");
        request.setPassword(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User saved = new User();
        saved.setId(userId);
        saved.setPassword("old_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        userService.update(userId, request);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_shouldNotUpdatePasswordWhenPasswordIsEmpty() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPassword("old_encoded_password");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com");
        request.setPassword("");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User saved = new User();
        saved.setId(userId);
        saved.setPassword("old_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        userService.update(userId, request);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.update(userId, request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void setActive_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.setActive(userId, true));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findById_shouldThrowResourceNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(userId));
    }

    @Test
    void findByEmail_shouldThrowResourceNotFoundException() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findByEmail(email));
    }

    @Test
    void register_shouldThrowBadRequestExceptionWhenEmailExists() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrowBadRequestExceptionWhenUsernameExists() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setUsername("existinguser");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_shouldThrowBadRequestExceptionWhenEmailExists() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("old@example.com");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("existing@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.update(userId, request));
        verify(userRepository, never()).save(any(User.class));
    }
}

