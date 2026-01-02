package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.UserRegistrationRequest;
import com.example.backend.application.dto.request.UserUpdateRequest;
import com.example.backend.application.dto.response.UserResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.UserService;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.domain.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Bu email adresi zaten kullanılıyor");
        }
        
        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Bu kullanıcı adı zaten kullanılıyor");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setIsActive(true);

        // Rol seçimi - ADMIN seçilemez, varsayılan USER
        // Eski rol isimlerini yeni isimlere çevir (geriye dönük uyumluluk)
        String secilenRol = request.getRoleName();
        if (secilenRol == null || secilenRol.isEmpty() || secilenRol.equals("ADMIN")) {
            secilenRol = "USER";
        } else if (secilenRol.equals("READER")) {
            secilenRol = "USER";
        } else if (secilenRol.equals("AUTHOR")) {
            secilenRol = "WRITER";
        }
        
        final String finalRolAdi = secilenRol;
        Role role = roleRepository.findByName(finalRolAdi)
                .orElseThrow(() -> new ResourceNotFoundException(finalRolAdi + " rolü bulunamadı"));
        user.getRoles().add(role);

        user = userRepository.save(user);
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        // Tüm kullanıcıları ID'ye göre DESC sıralı al
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
            Sort.by(Sort.Direction.DESC, "id"));
        
        // Spring Data JPA'nın varsayılan findAll metodunu kullan
        Page<User> userPage = userRepository.findAll(sortedPageable);
        
        // İçeriği ID'ye göre DESC sırala (ekstra güvenlik için)
        java.util.List<User> sortedUsers = userPage.getContent().stream()
            .sorted((u1, u2) -> Long.compare(u2.getId(), u1.getId()))
            .collect(java.util.stream.Collectors.toList());
        
        // Yeni Page oluştur
        Page<User> sortedUserPage = new PageImpl<>(
            sortedUsers, 
            sortedPageable, 
            userPage.getTotalElements()
        );
        
        return sortedUserPage.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> search(String search, Pageable pageable) {
        String searchTerm = "%" + search.toLowerCase() + "%";
        return userRepository.search(searchTerm, pageable)
                .map(this::toResponse);
    }

    @Override
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Bu email adresi zaten kullanılıyor");
        }

        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());

        user = userRepository.save(user);
        return toResponse(user);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public void setActive(Long id, Boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        user.setIsActive(active);
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUsername(user.getUsername());
        response.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        response.setCreatedAt(user.getCreatedAt());
        response.setIsActive(user.getIsActive());
        return response;
    }
}

