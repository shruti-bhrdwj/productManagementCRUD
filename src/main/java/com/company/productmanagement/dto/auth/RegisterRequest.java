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
    @NotBlank(message = "{v-01}")
    @Size(min = 3, max = 50, message = "{v-02}")
    String username,
    
    @NotBlank(message = "{v-03}")
    @Size(min = 6, message = "{v-04}")
    String password,
    
    @NotBlank(message = "{v-05}")
    @Email(message = "{v-06}")
    String email
) {}
