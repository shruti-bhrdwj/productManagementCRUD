package com.company.productmanagement.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration request
 * 
 * @param username user's username
 * @param password user's password
 * @param email user's email
 */
public record RegisterRequest(
    @NotBlank(message = "v-1")
    @Size(min = 3, max = 50, message = "v-2")
    String username,
    
    @NotBlank(message = "v-3")
    @Size(min = 6, message = "v-4")
    String password,
    
    @NotBlank(message = "v-5")
    @Email(message = "v-6")
    String email
) {}
