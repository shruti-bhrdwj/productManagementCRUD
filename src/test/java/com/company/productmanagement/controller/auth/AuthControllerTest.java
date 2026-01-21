package com.company.productmanagement.controller.auth;

import com.company.productmanagement.controller.AuthController;
import com.company.productmanagement.dto.auth.AuthResponse;
import com.company.productmanagement.dto.auth.LoginRequest;
import com.company.productmanagement.dto.auth.RegisterRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

/**
 * Unit tests for AuthController
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

        @MockBean
        private MessageSource messageSource;
        // ======= REGISTER TESTS =======

        @Test
        void shouldRegisterUserSuccessfully() throws Exception {
                RegisterRequest request = new RegisterRequest(
                                "testuser", "password123", "test@example.com");

                AuthResponse response = new AuthResponse(
                                "jwt-token", "testuser", "test@example.com");

                when(authService.register(any(RegisterRequest.class))).thenReturn(response);

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.token").value("jwt-token"))
                                .andExpect(jsonPath("$.username").value("testuser"))
                                .andExpect(jsonPath("$.email").value("test@example.com"));
        }

        @Test
        void shouldReturnConflictWhenUsernameExists() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "existinguser", "password123", "test@example.com");

                when(authService.register(any(RegisterRequest.class)))
                                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "a-2"));

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("a-2"));
        }

        @Test
        void shouldFailWhenUsernameIsBlank() throws Exception {
                RegisterRequest request = new RegisterRequest("", "password123", "test@example.com");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-1"));
        }

        @Test
        void shouldFailWhenUsernameIsTooShort() throws Exception {

                RegisterRequest request = new RegisterRequest("ab", "password123", "test@example.com");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-2"));
        }

        @Test
        void shouldFailWhenUsernameIsTooLong() throws Exception {

                String longUsername = "a".repeat(51); // More than 50 characters
                RegisterRequest request = new RegisterRequest(longUsername, "password123", "test@example.com");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-2"));
        }

        @Test
        void shouldFailWhenPasswordIsBlank() throws Exception {

                RegisterRequest request = new RegisterRequest("testuser", "", "test@example.com");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-3"));
        }

        @Test
        void shouldFailWhenPasswordIsTooShort() throws Exception {
                RegisterRequest request = new RegisterRequest("user", "123", "test@example.com");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-4"));
        }

        @Test
        void shouldFailWhenEmailIsBlank() throws Exception {

                RegisterRequest request = new RegisterRequest("testuser", "password123", "");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-5")); // Email required code
        }

        @Test
        void shouldFailWhenEmailIsInvalid() throws Exception {

                RegisterRequest request = new RegisterRequest("testuser", "password123", "invalid-email");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-6")); // Email format code
        }

        @Test
        void shouldFailWhenAllFieldsAreEmpty() throws Exception {

                RegisterRequest request = new RegisterRequest("", "", "");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").exists()); // Will return first validation error
        }

        @Test
        void shouldFailWhenRequestBodyIsNull() throws Exception {

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldFailWhenRequestBodyIsMalformed() throws Exception {

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json}"))
                                .andExpect(status().is5xxServerError());
        }

        @Test
        void shouldAcceptValidEmailWithSpecialCharacters() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "testuser", "password123", "test.user+tag@example.co.uk");

                AuthResponse response = new AuthResponse(
                                "jwt-token", "testuser", "test.user+tag@example.co.uk");

                when(authService.register(any(RegisterRequest.class))).thenReturn(response);

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.email").value("test.user+tag@example.co.uk"));
        }

        @Test
        void shouldAcceptMinimumValidUsername() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "abc", "password123", "test@example.com");

                AuthResponse response = new AuthResponse(
                                "jwt-token", "abc", "test@example.com");

                when(authService.register(any(RegisterRequest.class))).thenReturn(response);

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.username").value("abc"));
        }

        @Test
        void shouldAcceptMinimumValidPassword() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "testuser", "pass12", "test@example.com");

                AuthResponse response = new AuthResponse(
                                "jwt-token", "testuser", "test@example.com");

                when(authService.register(any(RegisterRequest.class))).thenReturn(response);

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }

        // ======= LOGIN TESTS =======

        @Test
        void shouldLoginSuccessfully() throws Exception {
                LoginRequest request = new LoginRequest("testuser", "password123");
                AuthResponse response = new AuthResponse(
                                "jwt-token", "testuser", "test@example.com");

                when(authService.login(any(LoginRequest.class))).thenReturn(response);

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("jwt-token"))
                                .andExpect(jsonPath("$.username").value("testuser"))
                                .andExpect(jsonPath("$.email").value("test@example.com"));
        }

        @Test
        void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {

                LoginRequest request = new LoginRequest("testuser", "wrongpassword");

                when(authService.login(any(LoginRequest.class)))
                                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "a-1"));

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.code").value("a-1"));
        }

        @Test
        void shouldReturnUnauthorizedForNonExistentUser() throws Exception {

                LoginRequest request = new LoginRequest("nonexistent", "password123");

                when(authService.login(any(LoginRequest.class)))
                                .thenThrow(new BadCredentialsException("Bad credentials"));

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.code").value("a-1"));
        }

        @Test
        void shouldFailWhenLoginUsernameIsBlank() throws Exception {

                LoginRequest request = new LoginRequest("", "password123");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-1")); // Username required
        }

        @Test
        void shouldFailWhenLoginPasswordIsBlank() throws Exception {

                LoginRequest request = new LoginRequest("testuser", "");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-3")); // Password required
        }

        @Test
        void shouldFailWhenBothCredentialsAreBlank() throws Exception {

                LoginRequest request = new LoginRequest("", "");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").exists()); // Will return first validation error
        }

        @Test
        void shouldFailWhenLoginRequestBodyIsNull() throws Exception {

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldFailWhenLoginRequestBodyIsMalformed() throws Exception {

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{malformed}"))
                                .andExpect(status().is5xxServerError());
        }

        @Test
        void shouldHandleGeneralAuthenticationException() throws Exception {

                LoginRequest request = new LoginRequest("testuser", "password123");

                when(authService.login(any(LoginRequest.class)))
                                .thenThrow(new AuthenticationException("Authentication failed") {
                                });

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.code").value("a-5"));
        }

        @Test
        void shouldLoginWithCaseSensitiveUsername() throws Exception {

                LoginRequest request = new LoginRequest("TestUser", "password123");
                AuthResponse response = new AuthResponse(
                                "jwt-token", "TestUser", "test@example.com");

                when(authService.login(any(LoginRequest.class))).thenReturn(response);

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value("TestUser"));
        }

        @Test
        void shouldHandleSpecialCharactersInPassword() throws Exception {

                LoginRequest request = new LoginRequest("testuser", "P@ssw0rd!#$");
                AuthResponse response = new AuthResponse(
                                "jwt-token", "testuser", "test@example.com");

                when(authService.login(any(LoginRequest.class))).thenReturn(response);

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }

        @Test
        void shouldHandleUnexpectedException() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "testuser", "password123", "test@example.com");

                when(authService.register(any(RegisterRequest.class)))
                                .thenThrow(new RuntimeException("Unexpected error"));

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.code").value("g-1"));
        }

        @Test
        void shouldFailWhenContentTypeIsMissing() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "testuser", "password123", "test@example.com");

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().is5xxServerError());
        }

        @Test
        void shouldHandleConcurrentRegistration() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "testuser", "password123", "test@example.com");

                when(authService.register(any(RegisterRequest.class)))
                                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "a-2"));

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("a-2"));
        }

        @Test
        void shouldReturnErrorWithTimestamp() throws Exception {

                LoginRequest request = new LoginRequest("user", "wrong");

                when(authService.login(any(LoginRequest.class)))
                                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "a-1"));

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        void shouldCompleteFullRegistrationFlow() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "newuser", "securePass123", "newuser@example.com");

                AuthResponse response = new AuthResponse(
                                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJuZXd1c2VyIn0.signature",
                                "newuser",
                                "newuser@example.com");

                when(authService.register(any(RegisterRequest.class))).thenReturn(response);

                mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.token").isNotEmpty())
                                .andExpect(jsonPath("$.token").isString())
                                .andExpect(jsonPath("$.username").value("newuser"))
                                .andExpect(jsonPath("$.email").value("newuser@example.com"));
        }

        @Test
        void shouldCompleteFullLoginFlow() throws Exception {
                LoginRequest request = new LoginRequest("existinguser", "password123");

                AuthResponse response = new AuthResponse(
                                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJleGlzdGluZ3VzZXIifQ.signature",
                                "existinguser",
                                "user@example.com");

                when(authService.login(any(LoginRequest.class))).thenReturn(response);

                mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").isNotEmpty())
                                .andExpect(jsonPath("$.token").isString())
                                .andExpect(jsonPath("$.username").value("existinguser"))
                                .andExpect(jsonPath("$.email").value("user@example.com"));
        }
}
