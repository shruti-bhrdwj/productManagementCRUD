package com.company.productmanagement.dto.error;

import java.time.Instant;

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
        Instant timestamp
) {

    public static ErrorResponse of(String message, String code) {
        return new ErrorResponse(message, code, Instant.now());
    }
}
