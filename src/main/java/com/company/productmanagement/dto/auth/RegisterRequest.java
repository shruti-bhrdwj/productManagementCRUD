package com.company.productmanagement.dto.auth;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

/**
 * DTO for user registration request
 * 
 * @param username user's username
 * @param password user's password
 * @param email user's email
 */
public record RegisterRequest(

    @NotBlank(message = "v-1",groups = First.class)
    @Size(min = 3, max = 50, message = "v-2",groups = Second.class)
    String username,
    
    @NotBlank(message = "v-3", groups = First.class)
    @Size(min = 6, message = "v-4", groups = Second.class)
    String password,
    
    @NotBlank(message = "v-5", groups = First.class)
    @Email(message = "v-6", groups = Second.class)
    String email
) {
    public interface First {}
    public interface Second {}
    
    @GroupSequence({ Default.class, First.class, Second.class })
    public interface ValidationOrder {}
}


