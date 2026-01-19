package com.company.productmanagement.exception.custom;

/**
 * Exception thrown when a requested product is not found
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(String message) {
        super(message);
    }
    
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
