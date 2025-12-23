package com.example.backend.application.service;

import com.example.backend.application.dto.request.UserRegistrationRequest;
import com.example.backend.application.dto.request.UserUpdateRequest;
import com.example.backend.application.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse register(UserRegistrationRequest request);

    UserResponse findById(Long id);

    UserResponse findByEmail(String email);

    Page<UserResponse> findAll(Pageable pageable);

    Page<UserResponse> search(String search, Pageable pageable);

    UserResponse update(Long id, UserUpdateRequest request);

    void delete(Long id);

    void setActive(Long id, Boolean active);
}

