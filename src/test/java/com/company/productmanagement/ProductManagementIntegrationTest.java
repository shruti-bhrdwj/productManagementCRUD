package com.company.productmanagement;

import com.company.productmanagement.dto.auth.LoginRequest;
import com.company.productmanagement.dto.auth.RegisterRequest;
import com.company.productmanagement.dto.product.ProductRequest;
import com.company.productmanagement.entity.Product;
import com.company.productmanagement.entity.User;
import com.company.productmanagement.repository.ProductRepository;
import com.company.productmanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for the entire Product Management System
 * Tests the full stack: Controller → Service → Repository → Database
 * 
 * @author shruti Sharma
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductManagementIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private String jwtToken;
    
    @BeforeEach
    void setUp() throws Exception {
        // Clean database
        productRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create and authenticate a test user
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .enabled(true)
                .build();
        userRepository.save(user);
        
        // Get JWT token
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(response).get("token").asText();
    }
    
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "newuser", "password123", "newuser@example.com");
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
        
        // Verify user exists in database
        assert userRepository.findByUsername("newuser").isPresent();
    }
    
    @Test
    void shouldLoginAndReturnJwtToken() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    @Test
    void shouldCreateProductWithAuthentication() throws Exception {
        // Given
        ProductRequest request = new ProductRequest(
                "Laptop", 
                "High-performance laptop", 
                new BigDecimal("999.99"), 
                10
        );
        
        // When & Then
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.quantity").value(10));
        
        // Verify product exists in database
        assert productRepository.existsByName("Laptop");
    }
    
    @Test
    void shouldFailToCreateProductWithoutAuthentication() throws Exception {
        // Given
        ProductRequest request = new ProductRequest(
                "Laptop", "Description", new BigDecimal("999.99"), 10);
        
        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void shouldGetAllProducts() throws Exception {
        // Given - Create test products
        Product product1 = Product.builder()
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                
                .build();
        
        Product product2 = Product.builder()
                .name("Product 2")
                .description("Description 2")
                .price(new BigDecimal("149.99"))
                .quantity(5)
                .build();
        
        productRepository.save(product1);
        productRepository.save(product2);
        
        // When & Then
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].name").value("Product 2"));
    }
    
    @Test
    void shouldGetProductById() throws Exception {
        // Given
        Product product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                
                .build();
        Product savedProduct = productRepository.save(product);
        
        // When & Then
        mockMvc.perform(get("/api/products/" + savedProduct.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));
    }
    
    @Test
    void shouldReturnNotFoundForNonExistentProduct() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void shouldUpdateProduct() throws Exception {
        // Given - Create initial product
        Product product = Product.builder()
                .name("Original Product")
                .description("Original Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                
                .build();
        Product savedProduct = productRepository.save(product);
        
        ProductRequest updateRequest = new ProductRequest(
                "Updated Product",
                "Updated Description",
                new BigDecimal("149.99"),
                20
        );
        
        // When & Then
        mockMvc.perform(put("/api/products/" + savedProduct.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.quantity").value(20));
        
        // Verify database was updated
        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assert updatedProduct.getName().equals("Updated Product");
        assert updatedProduct.getPrice().compareTo(new BigDecimal("149.99")) == 0;
    }
    
    @Test
    void shouldDeleteProduct() throws Exception {
        // Given
        Product product = Product.builder()
                .name("Product to Delete")
                .description("Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();
        
        // When & Then
        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
        
        // Verify product was deleted from database
        assert productRepository.findById(productId).isEmpty();
    }
    
    @Test
    void shouldValidateProductRequestFields() throws Exception {
        ProductRequest invalidName = new ProductRequest(
            "",
            "Valid Description",
            new BigDecimal("10.00"),
            5
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("v-7"));

        // Price <= 0
        ProductRequest invalidPrice = new ProductRequest(
                "Valid Name",
                "Valid Description",
                new BigDecimal("0.00"),
                5
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPrice)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("v-11"));

        //Quantity negative
        ProductRequest invalidQuantity = new ProductRequest(
                "Valid Name",
                "Valid Description",
                new BigDecimal("10.00"),
                -5
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidQuantity)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("v-13"));
    }
    
    @Test
    void shouldHandleInvalidJwtToken() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer invalid-token-here"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void fullWorkflowTest_RegisterLoginCreateUpdateDelete() throws Exception {
        // 1. Register new user
        RegisterRequest registerRequest = new RegisterRequest(
                "workflowuser", "password123", "workflow@example.com");
        
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String registerResponse = registerResult.getResponse().getContentAsString();
        String newUserToken = objectMapper.readTree(registerResponse).get("token").asText();
        
        // 2. Create a product
        ProductRequest createRequest = new ProductRequest(
                "Workflow Product", "Description", new BigDecimal("199.99"), 5);
        
        MvcResult createResult = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + newUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String createResponse = createResult.getResponse().getContentAsString();
        Long productId = objectMapper.readTree(createResponse).get("id").asLong();
        
        // 3. Update the product
        ProductRequest updateRequest = new ProductRequest(
                "Updated Workflow Product", "Updated", new BigDecimal("299.99"), 10);
        
        mockMvc.perform(put("/api/products/" + productId)
                        .header("Authorization", "Bearer " + newUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Workflow Product"));
        
        // 4. Get the product
        mockMvc.perform(get("/api/products/" + productId)
                        .header("Authorization", "Bearer " + newUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId));
        
        // 5. Delete the product
        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", "Bearer " + newUserToken))
                .andExpect(status().isNoContent());
        
        // 6. Verify product is deleted
        mockMvc.perform(get("/api/products/" + productId)
                        .header("Authorization", "Bearer " + newUserToken))
                .andExpect(status().isNotFound());
    }
}
