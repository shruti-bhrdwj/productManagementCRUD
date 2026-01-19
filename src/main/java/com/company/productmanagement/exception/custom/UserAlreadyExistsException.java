package com.company.productmanagement.exception.custom;

/**
 * Exception thrown when user already exists during registration
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String username, String field) {
        super("User already exists with " + field + ": " + username);
    }
}
