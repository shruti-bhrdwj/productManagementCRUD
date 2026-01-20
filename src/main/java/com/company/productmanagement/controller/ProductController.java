package com.company.productmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.company.productmanagement.dto.product.ProductRequest;
import com.company.productmanagement.dto.product.ProductResponse;
import com.company.productmanagement.service.ProductService;
import com.company.productmanagement.utils.ApiEndpointConstants;

import java.util.List;

/**
 * REST controller for product management operations
 * All endpoints are protected by JWT authentication
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management CRUD APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * Create a new product
     * 
     * @param request product creation request
     * @return created product response
     */
    @PostMapping(ApiEndpointConstants.PRODUCT)
    @Operation(summary = "Create a new product", description = "Creates a new product in the system")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all products
     * 
     * @return list of all products
     */
    @GetMapping(ApiEndpointConstants.PRODUCT)
    @Operation(summary = "Get all products", description = "Retrieves all products from the system")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    /**
     * Get product by ID
     * 
     * @param id product ID
     * @return product response
     */
    @GetMapping(ApiEndpointConstants.PRODUCT_BY_ID)
    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Update an existing product
     * 
     * @param id product ID
     * @param request product update request
     * @return updated product response
     */
    @PutMapping(ApiEndpointConstants.PRODUCT_BY_ID)
    @Operation(summary = "Update product", description = "Updates an existing product")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Delete a product
     * 
     * @param id product ID
     * @return no content response
     */
    @DeleteMapping(ApiEndpointConstants.PRODUCT_BY_ID)
    @Operation(summary = "Delete product", description = "Deletes a product from the system")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
}
