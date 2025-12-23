package com.example.backend.application.service;

import com.example.backend.application.dto.request.UserLoginRequest;
import com.example.backend.application.dto.response.JwtResponse;

public interface AuthService {
    
    JwtResponse login(UserLoginRequest request);
    
    void logout();
}

