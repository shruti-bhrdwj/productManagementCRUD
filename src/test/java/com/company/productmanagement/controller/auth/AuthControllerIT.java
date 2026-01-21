package com.company.productmanagement.controller.auth;

import com.company.productmanagement.dto.auth.LoginRequest;
import com.company.productmanagement.dto.auth.RegisterRequest;
import com.company.productmanagement.utils.ApiEndpointConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ================= REGISTER =================

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "ituser1",
                "Password123",
                "ituser1@example.com"
        );

        mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("ituser1"))
                .andExpect(jsonPath("$.email").value("ituser1@example.com"));
    }

    @Test
    void shouldReturnConflictWhenRegisteringSameUserTwice() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "ituser2",
                "Password123",
                "ituser2@example.com"
        );

        // first time OK
        mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // second time -> conflict
        mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("a-2"));
    }

    @Test
    void shouldFailRegisterValidation() throws Exception {

        RegisterRequest request = new RegisterRequest("", "", "");

        mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists());
    }

    // ================= LOGIN =================

    @Test
    void shouldLoginSuccessfullyAfterRegister() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest(
                "loginuser1",
                "Password123",
                "loginuser1@example.com"
        );

        mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(
                "loginuser1",
                "Password123"
        );

        mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("loginuser1"));
    }

    @Test
    void shouldFailLoginWithWrongPassword() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest(
                "loginuser2",
                "Password123",
                "loginuser2@example.com"
        );

        mockMvc.perform(post(ApiEndpointConstants.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(
                "loginuser2",
                "WrongPassword"
        );

        mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("a-1"));
    }

    @Test
    void shouldFailLoginForNonExistingUser() throws Exception {

        LoginRequest loginRequest = new LoginRequest(
                "nouser",
                "Password123"
        );

        mockMvc.perform(post(ApiEndpointConstants.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("a-5"));
    }
}

