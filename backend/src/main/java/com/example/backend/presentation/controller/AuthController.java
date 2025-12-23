package com.example.backend.presentation.controller;

import com.example.backend.application.dto.request.UserLoginRequest;
import com.example.backend.application.dto.request.UserRegistrationRequest;
import com.example.backend.application.dto.response.JwtResponse;
import com.example.backend.application.dto.response.UserResponse;
import com.example.backend.application.service.AuthService;
import com.example.backend.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/kayit")
    public ResponseEntity<UserResponse> kayitOl(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/giris")
    public ResponseEntity<JwtResponse> girisYap(@Valid @RequestBody UserLoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cikis")
    public ResponseEntity<Void> cikisYap() {
        authService.logout();
        return ResponseEntity.ok().build();
    }
}

