package com.company.productmanagement.exception;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.company.productmanagement.dto.error.ErrorResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import java.util.Locale;

/**
 * Global exception handler for centralized error handling
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, Locale locale) {

        String code;
        HttpStatus status;

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            code = rse.getReason();
        } 
        else if(ex instanceof AccessDeniedException){
            status = HttpStatus.FORBIDDEN;
            code = "a-6";
        }
        else if (ex instanceof MethodArgumentNotValidException manv) {
            status = HttpStatus.BAD_REQUEST;

            String msgKey = manv.getBindingResult()
                    .getFieldErrors()
                    .get(0)
                    .getDefaultMessage();

            code = msgKey;
        }
        else if (ex instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
            code = "a-1";
        }
        else if (ex instanceof AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            code = "a-5";
        }
        else if (ex instanceof DataIntegrityViolationException) {
            status = HttpStatus.CONFLICT;
            code = "a-2";
        }
        else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = "g-1";
        }

        String message = messageSource.getMessage(code, null, locale);

        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(message, code));
    }
}