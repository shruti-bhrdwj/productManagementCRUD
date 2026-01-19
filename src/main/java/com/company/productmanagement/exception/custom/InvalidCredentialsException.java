package com.company.productmanagement.exception.custom;

/**
 * Exception thrown when authentication fails
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
