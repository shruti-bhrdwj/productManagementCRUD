package com.company.productmanagement.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request
 * 
 * @param username user's username
 * @param password user's password
 */
public record LoginRequest(
    @NotBlank(message = "v-1")
    String username,
    
    @NotBlank(message = "v-3")
    String password
) {}
