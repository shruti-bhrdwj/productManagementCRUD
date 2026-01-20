package com.company.productmanagement.service.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.company.productmanagement.dto.product.ProductRequest;
import com.company.productmanagement.dto.product.ProductResponse;
import com.company.productmanagement.entity.Product;
import com.company.productmanagement.repository.ProductRepository;
import com.company.productmanagement.service.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 * Tests all CRUD operations
 * 
 * @author Shruti Sharma
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    private Product product;
    private ProductRequest productRequest;
    
    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        productRequest = new ProductRequest(
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10
        );
    }
    
    @Test
    void shouldCreateProductSuccessfully() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        // When
        ProductResponse response = productService.createProduct(productRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(product.getId(), response.id());
        assertEquals(product.getName(), response.name());
        assertEquals(product.getPrice(), response.price());
        
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    void shouldGetAllProductsSuccessfully() {
        // Given
        Product product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .price(new BigDecimal("49.99"))
                .quantity(5)
                .build();
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(product, product2));
        
        // When
        List<ProductResponse> products = productService.getAllProducts();
        
        // Then
        assertNotNull(products);
        assertEquals(2, products.size());
        
        verify(productRepository).findAll();
    }
    
    @Test
    void shouldGetProductByIdSuccessfully() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        // When
        ProductResponse response = productService.getProductById(1L);
        
        // Then
        assertNotNull(response);
        assertEquals(product.getId(), response.id());
        assertEquals(product.getName(), response.name());
        
        verify(productRepository).findById(1L);
    }
    
    @Test
    void shouldFailWhenProductNotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResponseStatusException.class, () -> productService.getProductById(999L));
        
        verify(productRepository).findById(999L);
    }
    
    @Test
    void shouldUpdateProductSuccessfully() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        ProductRequest updateRequest = new ProductRequest(
                "Updated Product",
                "Updated Description",
                new BigDecimal("149.99"),
                20
        );
        
        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("Updated Product", product.getName());
        assertEquals(new BigDecimal("149.99"), product.getPrice());
        
        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }
    
    @Test
    void shouldDeleteProductSuccessfully() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);
        
        // When
        productService.deleteProduct(1L);
        
        // Then
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }
    
    @Test
    void shouldFailWhenDeletingNonExistentProduct() {
        // Given
        when(productRepository.existsById(999L)).thenReturn(false);
        
        // When & Then
        assertThrows(ResponseStatusException.class, () -> productService.deleteProduct(999L));
        
        verify(productRepository).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}
