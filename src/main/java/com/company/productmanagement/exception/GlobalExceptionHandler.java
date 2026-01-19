package com.company.productmanagement.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.company.productmanagement.dto.error.ErrorResponse;
import com.company.productmanagement.exception.custom.InvalidCredentialsException;
import com.company.productmanagement.exception.custom.InvalidTokenException;
import com.company.productmanagement.exception.custom.ProductNotFoundException;
import com.company.productmanagement.exception.custom.UserAlreadyExistsException;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Global exception handler for centralized error handling
 * Uses @ControllerAdvice to handle exceptions across all controllers
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @Autowired
    private MessageSource messageSource;
    
    /**
     * Handles product not found exception
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage(
                "pdm-1", null, ex.getMessage(), locale);
        String code = "pdm-1";
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(message, code));
    }
    
    /**
     * Handles user already exists exception
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, Locale locale) {
        String message = messageSource.getMessage(
                "a-2", null, ex.getMessage(), locale);
        String code = "a-2";
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(message, code));
    }
    
    /**
     * Handles invalid credentials exception
     */
    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            Exception ex, Locale locale) {
        String message = messageSource.getMessage(
                "a-1", null, "Invalid credentials", locale);
        String code = "a-1";
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(message, code));
    }
    
    /**
     * Handles invalid token exception
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException ex, Locale locale) {
        String message = messageSource.getMessage(
                "a-3", null, ex.getMessage(), locale);
        String code = "a-3";
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(message, code));
    }
    
    /**
     * Handles authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, Locale locale) {
        String message = messageSource.getMessage(
                "a-5", null, "Unauthorized", locale);
        String code = "a-5";
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(message, code));
    }
    
    /**
     * Handles validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, Locale locale) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + ": " + errorMessage;
                })
                .collect(Collectors.joining(", "));
        
        String code = "v-1";
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(message, code));
    }
    
    /**
     * Handles all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, Locale locale) {
        String message = messageSource.getMessage(
                "g-1", null, "An error occurred", locale);
        String code = "g-1";
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(message, code));
    }
}