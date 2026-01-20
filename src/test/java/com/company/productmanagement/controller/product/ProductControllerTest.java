package com.company.productmanagement.controller.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.company.productmanagement.controller.ProductController;
import com.company.productmanagement.dto.product.ProductRequest;
import com.company.productmanagement.dto.product.ProductResponse;
import com.company.productmanagement.exception.custom.ProductNotFoundException;
import com.company.productmanagement.security.JwtAuthenticationFilter;
import com.company.productmanagement.service.ProductService;
import com.company.productmanagement.utils.ApiEndpointConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Unit tests for ProductController
 * Uses MockMvc to test REST endpoints
 * 
 * @author Shruti Sharma
 */
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ProductService productService;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockBean
    private UserDetailsService userDetailsService;
    
    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        // Given
        ProductRequest request = new ProductRequest(
                "Test Product", "Description", new BigDecimal("99.99"), 10, "Electronics");
        ProductResponse response = new ProductResponse(
                1L, "Test Product", "Description", new BigDecimal("99.99"), 
                10, "Electronics", LocalDateTime.now(), LocalDateTime.now());
        
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post(ApiEndpointConstants.PRODUCT_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));
    }
    
    @Test
    void shouldReturnBadRequestWhenProductDataIsInvalid() throws Exception {
        // Given - Missing required fields
        ProductRequest request = new ProductRequest("", "", null, null, null);
        
        // When & Then
        mockMvc.perform(post(ApiEndpointConstants.PRODUCT_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldGetAllProductsSuccessfully() throws Exception {
        // Given
        List<ProductResponse> products = Arrays.asList(
                new ProductResponse(1L, "Product 1", "Desc 1", new BigDecimal("99.99"), 
                        10, "Electronics", LocalDateTime.now(), LocalDateTime.now()),
                new ProductResponse(2L, "Product 2", "Desc 2", new BigDecimal("149.99"), 
                        5, "Books", LocalDateTime.now(), LocalDateTime.now())
        );
        
        when(productService.getAllProducts()).thenReturn(products);
        
        // When & Then
        mockMvc.perform(get(ApiEndpointConstants.PRODUCT_BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].name").value("Product 2"));
    }
    
    @Test
    void shouldGetProductByIdSuccessfully() throws Exception {
        // Given
        ProductResponse response = new ProductResponse(
                1L, "Test Product", "Description", new BigDecimal("99.99"), 
                10, "Electronics", LocalDateTime.now(), LocalDateTime.now());
        
        when(productService.getProductById(1L)).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get(ApiEndpointConstants.PRODUCT_BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }
    
    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        // Given
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException(999L));
        
        // When & Then
        mockMvc.perform(get(ApiEndpointConstants.PRODUCT_BASE + "/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void shouldUpdateProductSuccessfully() throws Exception {
        // Given
        ProductRequest request = new ProductRequest(
                "Updated Product", "Updated Desc", new BigDecimal("149.99"), 20, "Electronics");
        ProductResponse response = new ProductResponse(
                1L, "Updated Product", "Updated Desc", new BigDecimal("149.99"), 
                20, "Electronics", LocalDateTime.now(), LocalDateTime.now());
        
        when(productService.updateProduct(eq(1L), any(ProductRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(put(ApiEndpointConstants.PRODUCT_BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(149.99));
    }
    
    @Test
    void shouldDeleteProductSuccessfully() throws Exception {
        // Given
        doNothing().when(productService).deleteProduct(1L);
        
        // When & Then
        mockMvc.perform(delete(ApiEndpointConstants.PRODUCT_BASE + "/1"))
                .andExpect(status().isNoContent());
        
        verify(productService, times(1)).deleteProduct(1L);
    }
    
    @Test
    void shouldSearchProductsByName() throws Exception {
        // Given
        List<ProductResponse> products = Arrays.asList(
                new ProductResponse(1L, "Test Product", "Desc", new BigDecimal("99.99"), 
                        10, "Electronics", LocalDateTime.now(), LocalDateTime.now())
        );
        
        when(productService.searchProductsByName("Test")).thenReturn(products);
        
        // When & Then
        mockMvc.perform(get(ApiEndpointConstants.PRODUCT_BASE + "/search")
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }
    
    @Test
    void shouldGetProductsByCategory() throws Exception {
        // Given
        List<ProductResponse> products = Arrays.asList(
                new ProductResponse(1L, "Product 1", "Desc", new BigDecimal("99.99"), 
                        10, "Electronics", LocalDateTime.now(), LocalDateTime.now())
        );
        
        when(productService.getProductsByCategory("Electronics")).thenReturn(products);
        
        // When & Then
        mockMvc.perform(get(ApiEndpointConstants.PRODUCT_BASE + "/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Electronics"));
    }
}