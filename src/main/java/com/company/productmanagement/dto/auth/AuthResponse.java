package com.company.productmanagement.dto.auth;

/**
 * DTO for authentication response
 * 
 * @param token JWT token
 * @param username user's username
 * @param email user's email
 */
public record AuthResponse(
    String token,
    String username,
    String email
) {}
