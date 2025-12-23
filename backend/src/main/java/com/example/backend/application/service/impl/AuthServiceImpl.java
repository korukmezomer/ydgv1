package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.UserLoginRequest;
import com.example.backend.application.dto.response.JwtResponse;
import com.example.backend.application.exception.UnauthorizedException;
import com.example.backend.application.service.AuthService;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.infrastructure.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public JwtResponse login(UserLoginRequest request) {
        User user = userRepository.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email veya şifre hatalı"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email veya şifre hatalı");
        }

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), roles);

        JwtResponse response = new JwtResponse();
        response.setToken(token);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));

        return response;
    }

    @Override
    public void logout() {
        // JWT stateless olduğu için client-side token silinir
        // İsterseniz token blacklist ekleyebilirsiniz
    }
}
