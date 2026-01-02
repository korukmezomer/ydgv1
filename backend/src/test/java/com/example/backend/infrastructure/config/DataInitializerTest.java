package com.example.backend.infrastructure.config;

import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private RoleRepository rolRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    private Role existingRole;

    @BeforeEach
    void setUp() {
        existingRole = new Role();
        existingRole.setId(1L);
        existingRole.setName("READER");
        existingRole.setIsActive(true);
    }

    @Test
    void run_shouldUpdateRoleNameWhenOldRoleExists() throws Exception {
        Role oldRole = new Role();
        oldRole.setName("READER");
        oldRole.setIsActive(true);

        when(rolRepository.findByName("READER")).thenReturn(Optional.of(oldRole));
        when(rolRepository.findByName("AUTHOR")).thenReturn(Optional.empty());
        when(rolRepository.existsByName("USER")).thenReturn(false);
        when(rolRepository.existsByName("WRITER")).thenReturn(false);
        when(rolRepository.existsByName("ADMIN")).thenReturn(false);

        dataInitializer.run();

        verify(rolRepository, times(1)).findByName("READER");
        verify(rolRepository, atLeastOnce()).existsByName("USER"); // Called in both updateRoleName and createRoleIfNotExists
        verify(rolRepository, times(1)).save(oldRole);
        assertEquals("USER", oldRole.getName());
    }

    @Test
    void run_shouldDeactivateOldRoleWhenNewRoleExists() throws Exception {
        Role oldRole = new Role();
        oldRole.setName("READER");
        oldRole.setIsActive(true);

        when(rolRepository.findByName("READER")).thenReturn(Optional.of(oldRole));
        when(rolRepository.findByName("AUTHOR")).thenReturn(Optional.empty());
        when(rolRepository.existsByName("USER")).thenReturn(true);
        when(rolRepository.existsByName("WRITER")).thenReturn(false);
        when(rolRepository.existsByName("ADMIN")).thenReturn(false);

        dataInitializer.run();

        verify(rolRepository, times(1)).findByName("READER");
        verify(rolRepository, atLeastOnce()).existsByName("USER"); // Called in both updateRoleName and createRoleIfNotExists
        verify(rolRepository, times(1)).save(oldRole);
        assertFalse(oldRole.getIsActive());
    }

    @Test
    void run_shouldCreateRoleIfNotExists() throws Exception {
        when(rolRepository.findByName("READER")).thenReturn(Optional.empty());
        when(rolRepository.existsByName("ADMIN")).thenReturn(false);
        when(rolRepository.existsByName("WRITER")).thenReturn(false);
        when(rolRepository.existsByName("USER")).thenReturn(false);

        dataInitializer.run();

        verify(rolRepository, times(3)).save(any(Role.class));
    }

    @Test
    void run_shouldNotCreateRoleIfAlreadyExists() throws Exception {
        when(rolRepository.findByName("READER")).thenReturn(Optional.empty());
        when(rolRepository.existsByName("ADMIN")).thenReturn(true);
        when(rolRepository.existsByName("WRITER")).thenReturn(true);
        when(rolRepository.existsByName("USER")).thenReturn(true);

        dataInitializer.run();

        verify(rolRepository, never()).save(any(Role.class));
    }

    @Test
    void run_shouldHandleMultipleRoleUpdates() throws Exception {
        Role readerRole = new Role();
        readerRole.setName("READER");
        Role authorRole = new Role();
        authorRole.setName("AUTHOR");

        when(rolRepository.findByName("READER")).thenReturn(Optional.of(readerRole));
        when(rolRepository.findByName("AUTHOR")).thenReturn(Optional.of(authorRole));
        when(rolRepository.existsByName("WRITER")).thenReturn(false);
        when(rolRepository.existsByName("USER")).thenReturn(false);
        when(rolRepository.existsByName("ADMIN")).thenReturn(false);

        dataInitializer.run();

        verify(rolRepository, atLeastOnce()).save(any(Role.class));
    }

    @Test
    void updateRoleName_shouldDeactivateOldWhenNewExists() {
        Role reader = new Role();
        reader.setName("READER");
        when(rolRepository.findByName("READER")).thenReturn(Optional.of(reader));
        when(rolRepository.existsByName("USER")).thenReturn(true);

        ReflectionTestUtils.invokeMethod(dataInitializer, "updateRoleName", "READER", "USER", "desc");

        assertFalse(reader.getIsActive());
        verify(rolRepository).save(reader);
    }

    @Test
    void updateRoleName_shouldRenameWhenNewNotExists() {
        Role reader = new Role();
        reader.setName("READER");
        when(rolRepository.findByName("READER")).thenReturn(Optional.of(reader));
        when(rolRepository.existsByName("USER")).thenReturn(false);

        ReflectionTestUtils.invokeMethod(dataInitializer, "updateRoleName", "READER", "USER", "desc");

        assertEquals("USER", reader.getName());
        assertEquals("desc", reader.getDescription());
        verify(rolRepository).save(reader);
    }

    @Test
    void createRoleIfNotExists_shouldSkipWhenExists() {
        when(rolRepository.existsByName("ADMIN")).thenReturn(true);

        ReflectionTestUtils.invokeMethod(dataInitializer, "createRoleIfNotExists", "ADMIN", "desc");

        verify(rolRepository, never()).save(any(Role.class));
    }

    @Test
    void createRoleIfNotExists_shouldCreateWhenMissing() {
        when(rolRepository.existsByName("ADMIN")).thenReturn(false);

        ReflectionTestUtils.invokeMethod(dataInitializer, "createRoleIfNotExists", "ADMIN", "desc");

        verify(rolRepository, times(1)).save(any(Role.class));
    }

    @Test
    void createAdminUserIfNotExists_shouldReturnWhenActiveExists() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("omer");
        when(userRepository.findActiveByEmail("omer@gmail.com")).thenReturn(Optional.of(admin));

        ReflectionTestUtils.invokeMethod(dataInitializer, "createAdminUserIfNotExists");

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void createAdminUserIfNotExists_shouldReturnWhenInactiveExists() {
        when(userRepository.findActiveByEmail("omer@gmail.com")).thenReturn(Optional.empty());
        User admin = new User();
        admin.setId(1L);
        when(userRepository.findByEmail("omer@gmail.com")).thenReturn(Optional.of(admin));

        ReflectionTestUtils.invokeMethod(dataInitializer, "createAdminUserIfNotExists");

        verify(userRepository, times(1)).findByEmail("omer@gmail.com");
    }

    @Test
    void createAdminUserIfNotExists_shouldHandleNotFound() {
        when(userRepository.findActiveByEmail("omer@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("omer@gmail.com")).thenReturn(Optional.empty());

        ReflectionTestUtils.invokeMethod(dataInitializer, "createAdminUserIfNotExists");

        verify(userRepository, times(1)).findByEmail("omer@gmail.com");
    }
}

