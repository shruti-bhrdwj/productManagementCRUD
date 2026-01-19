package com.company.productmanagement.utils;

/**
 * Central repository for all API endpoint paths.
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
public final class ApiEndpointConstants {
    
    private ApiEndpointConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // Base API path
    public static final String API_BASE = "/api";
    
    // Authentication Endpoints
    public static final String AUTH_BASE = API_BASE + "/auth";
    public static final String AUTH_REGISTER = "/register";
    public static final String AUTH_LOGIN = "/login";
    
    // Product Endpoints
    public static final String PRODUCT_BASE = API_BASE + "/products";
    public static final String PRODUCT_BY_ID = "/{id}";
    
    // Public endpoints (no authentication required)
    public static final String[] PUBLIC_ENDPOINTS = {
        AUTH_BASE + AUTH_REGISTER,
        AUTH_BASE + AUTH_LOGIN,
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/swagger-ui/index.html",
        "/api-docs/**",
        "/v3/api-docs/**"
    };
}
