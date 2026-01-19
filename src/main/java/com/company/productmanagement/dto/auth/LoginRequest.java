package com.company.productmanagement.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request
 * 
 * @param username user's username
 * @param password user's password
 */
public record LoginRequest(
    @NotBlank(message = "{v-01}")
    String username,
    
    @NotBlank(message = "{v-03}")
    String password
) {}
