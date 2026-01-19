package com.company.productmanagement.controller.auth;

import com.company.productmanagement.controller.AuthController;
import com.company.productmanagement.dto.auth.AuthResponse;
import com.company.productmanagement.dto.auth.LoginRequest;
import com.company.productmanagement.dto.auth.RegisterRequest;
import com.company.productmanagement.exception.custom.InvalidCredentialsException;
import com.company.productmanagement.exception.custom.UserAlreadyExistsException;
import com.company.productmanagement.service.AuthService;
import com.company.productmanagement.security.JwtAuthenticationFilter;
import com.company.productmanagement.utils.ApiEndpointConstants;
import com.company.productmanagement.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController
 * Uses MockMvc to test REST endpoints
 * 
 * @author Shruti Sharma
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private UserDetailsService userDetailsService;
    
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "testuser", "password123", "test@example.com");
        AuthResponse response = new AuthResponse(
                "jwt-token", "testuser", "test@example.com");
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post(ApiEndpointConstants.AUTH_BASE + ApiEndpointConstants.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
    
    @Test
    void shouldReturnBadRequestWhenRegistrationDataIsInvalid() throws Exception {
        // Given - Missing required fields
        RegisterRequest request = new RegisterRequest("", "", "");
        
        // When & Then
        mockMvc.perform(post(ApiEndpointConstants.AUTH_BASE + ApiEndpointConstants.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "existinguser", "password123", "test@example.com");
        
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));
        
        // When & Then
        mockMvc.perform(post(ApiEndpointConstants.AUTH_BASE + ApiEndpointConstants.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
    
    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        AuthResponse response = new AuthResponse(
                "jwt-token", "testuser", "test@example.com");
        
        when(authService.login(any(LoginRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post(ApiEndpointConstants.AUTH_BASE + ApiEndpointConstants.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));
        
        // When & Then
        mockMvc.perform(post(ApiEndpointConstants.AUTH_BASE + ApiEndpointConstants.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
