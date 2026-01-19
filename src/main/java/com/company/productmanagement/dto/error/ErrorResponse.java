package com.company.productmanagement.dto.error;

import java.time.LocalDateTime;

/**
 * Standard error response DTO for consistent error handling
 * 
 * @param message error message
 * @param code error code
 * @param timestamp timestamp when error occurred
 */
public record ErrorResponse(
    String message,
    String code,
    LocalDateTime timestamp
) {
    /**
     * Creates an error response with current timestamp
     * 
     * @param message error message
     * @param code error code
     * @return ErrorResponse instance
     */
    public static ErrorResponse of(String message, String code) {
        return new ErrorResponse(message, code, LocalDateTime.now());
    }
}