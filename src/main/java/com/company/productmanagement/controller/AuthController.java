package com.company.productmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.company.productmanagement.dto.auth.AuthResponse;
import com.company.productmanagement.dto.auth.LoginRequest;
import com.company.productmanagement.dto.auth.RegisterRequest;
import com.company.productmanagement.service.AuthService;
import com.company.productmanagement.utils.ApiEndpointConstants;

/**
 * REST controller for authentication operations
 * Handles user registration and login
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
@RestController
@RequestMapping(ApiEndpointConstants.AUTH_BASE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Register a new user
     * 
     * @param request registration request containing user details
     * @return AuthResponse with JWT token
     */
    @PostMapping(ApiEndpointConstants.AUTH_REGISTER)
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns JWT token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Login with existing credentials
     * 
     * @param request login request containing username and password
     * @return AuthResponse with JWT token
     */
    @PostMapping(ApiEndpointConstants.AUTH_LOGIN)
    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
