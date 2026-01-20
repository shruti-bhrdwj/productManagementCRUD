package com.company.productmanagement.service;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.company.productmanagement.dto.auth.AuthResponse;
import com.company.productmanagement.dto.auth.LoginRequest;
import com.company.productmanagement.dto.auth.RegisterRequest;
import com.company.productmanagement.entity.User;
import com.company.productmanagement.repository.UserRepository;
import com.company.productmanagement.utils.JwtUtils;

/**
 * Service class for authentication operations
 * Handles user registration and login with JWT token generation
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Registers a new user in the system
     * 
     * @param request registration request containing user details
     * @return AuthResponse with JWT token
     * @throws UserAlreadyExistsException if username already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "a-2");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtils.generateToken(savedUser);
        
        return new AuthResponse(token, savedUser.getUsername(), savedUser.getEmail());
    }
    
    /**
     * Authenticates a user and returns JWT token
     * 
     * @param request login request containing credentials
     * @return AuthResponse with JWT token
     * @throws InvalidCredentialsException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            
            // Get authenticated user
            User user = (User) authentication.getPrincipal();
            
            // Generate JWT token
            String token = jwtUtils.generateToken(user);
            
            return new AuthResponse(token, user.getUsername(), user.getEmail());
            
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "a-1");
        }
    }
}
