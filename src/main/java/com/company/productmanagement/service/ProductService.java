package com.company.productmanagement.service;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.company.productmanagement.dto.product.ProductRequest;
import com.company.productmanagement.dto.product.ProductResponse;
import com.company.productmanagement.entity.Product;
import com.company.productmanagement.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for product management operations
 * Handles CRUD operations for products
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * Creates a new product
     * 
     * @param request product creation request
     * @return created product response
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByName(request.name())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "pdm-2"
            );
        }
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .quantity(request.quantity() != null ? request.quantity() : 0)
                .build();
        
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }
    
    /**
     * Retrieves all products
     * 
     * @return list of all products
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves a product by ID
     * 
     * @param id product ID
     * @return product response
     * @throws ResponseStatusException if product not found
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "pdm-1" 
            ));
        return mapToResponse(product);
    }
    
    /**
     * Updates an existing product
     * 
     * @param id product ID
     * @param request product update request
     * @return updated product response
     * @throws ProductNotFoundException if product not found
     */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "pdm-1"));
        
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        
        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }
    
    /**
     * Deletes a product by ID
     * 
     * @param id product ID
     * @throws ResponseStatusException if product not found
     */
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "pdm-1");
        }
        productRepository.deleteById(id);
    }
    
    /**
     * Maps Product entity to ProductResponse DTO
     * 
     * @param product product entity
     * @return product response DTO
     */
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
