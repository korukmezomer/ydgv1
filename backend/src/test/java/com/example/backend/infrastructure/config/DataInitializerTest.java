package com.example.backend.infrastructure.config;

import com.example.backend.domain.entity.Role;
import com.example.backend.domain.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private RoleRepository rolRepository;

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
}

