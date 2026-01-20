package com.company.productmanagement.service.auth;

import com.company.productmanagement.dto.auth.AuthResponse;
import com.company.productmanagement.dto.auth.LoginRequest;
import com.company.productmanagement.dto.auth.RegisterRequest;
import com.company.productmanagement.entity.User;
import com.company.productmanagement.exception.custom.InvalidCredentialsException;
import com.company.productmanagement.exception.custom.UserAlreadyExistsException;
import com.company.productmanagement.repository.UserRepository;
import com.company.productmanagement.service.AuthService;
import com.company.productmanagement.utils.JwtUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * Tests registration and login functionality
 * 
 * @author Shruti Sharma
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @InjectMocks
    private AuthService authService;
    
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "password123", "test@example.com");
        loginRequest = new LoginRequest("testuser", "password123");
        
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .build();
    }
    
    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtils.generateToken(any(User.class))).thenReturn("jwt-token");
        
        // When
        AuthResponse response = authService.register(registerRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        
        verify(userRepository).existsByUsername(registerRequest.username());
        verify(passwordEncoder).encode(registerRequest.password());
        verify(userRepository).save(any(User.class));
        verify(jwtUtils).generateToken(any(User.class));
    }
    
    @Test
    void shouldFailWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(true);
        
        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
        
        verify(userRepository).existsByUsername(registerRequest.username());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void shouldLoginSuccessfully() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtUtils.generateToken(user)).thenReturn("jwt-token");
        
        // When
        AuthResponse response = authService.login(loginRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("testuser", response.username());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateToken(user);
    }
    
    @Test
    void shouldFailWhenCredentialsAreInvalid() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        
        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).generateToken(any(User.class));
    }
}
