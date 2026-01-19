package com.company.productmanagement.exception.custom;

/**
 * Exception thrown when JWT token is invalid or expired
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
}
