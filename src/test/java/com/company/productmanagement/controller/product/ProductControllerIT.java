package com.company.productmanagement.controller.product;

import com.company.productmanagement.dto.product.ProductRequest;
import com.company.productmanagement.entity.Product;
import com.company.productmanagement.repository.ProductRepository;
import com.company.productmanagement.utils.ApiEndpointConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController
 * Tests the real ProductService and repository with an in-memory database
 * Includes role-based security (ADMIN vs USER)
 */
@SpringBootTest
@Transactional
class ProductControllerIT {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity()) // Enable Spring Security
                .build();
        productRepository.deleteAll();
    }

    // ---------------- CREATE ----------------
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateProductAsAdmin() throws Exception {
        ProductRequest request = new ProductRequest("Admin Product", "Desc", new BigDecimal("50.0"), 5);

        mockMvc.perform(post(ApiEndpointConstants.PRODUCT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Admin Product"))
                .andExpect(jsonPath("$.price").value(50.0));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldFailCreateProductAsUser() throws Exception {
        ProductRequest request = new ProductRequest("User Product", "Desc", new BigDecimal("50.0"), 5);

        mockMvc.perform(post(ApiEndpointConstants.PRODUCT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldFailValidationWhenNameBlank() throws Exception {
        ProductRequest request = new ProductRequest("", "Desc", new BigDecimal("10.0"), 1);

        mockMvc.perform(post(ApiEndpointConstants.PRODUCT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("v-7"));
    }

    // ---------------- GET ----------------
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetProductByIdSuccessfully() throws Exception {
        Product saved = productRepository.save(new Product(null, "IT Product", "Desc", new BigDecimal("30.0"), 3, null, null));

        mockMvc.perform(get(ApiEndpointConstants.PRODUCT + "/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("IT Product"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(get(ApiEndpointConstants.PRODUCT + "/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("pdm-1"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetAllProductsPaged() throws Exception {
        productRepository.save(new Product(null, "Product 1", "Desc 1", new BigDecimal("10.0"), 1, null, null));
        productRepository.save(new Product(null, "Product 2", "Desc 2", new BigDecimal("20.0"), 2, null, null));

        mockMvc.perform(get(ApiEndpointConstants.PRODUCT)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Product 1"))
                .andExpect(jsonPath("$.content[1].name").value("Product 2"));
    }

    // ---------------- UPDATE ----------------
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateProductAsAdmin() throws Exception {
        Product saved = productRepository.save(new Product(null, "Old", "Desc", new BigDecimal("15.0"), 1, null, null));

        ProductRequest request = new ProductRequest("Updated Name", "Updated Desc", new BigDecimal("25.0"), 5);

        mockMvc.perform(put(ApiEndpointConstants.PRODUCT + "/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.price").value(25.0));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldFailUpdateProductAsUser() throws Exception {
        Product saved = productRepository.save(new Product(null, "Old", "Desc", new BigDecimal("15.0"), 1, null, null));

        ProductRequest request = new ProductRequest("Updated Name", "Updated Desc", new BigDecimal("25.0"), 5);

        mockMvc.perform(put(ApiEndpointConstants.PRODUCT + "/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ---------------- DELETE ----------------
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteProductAsAdmin() throws Exception {
        Product saved = productRepository.save(new Product(null, "Delete Me", "Desc", new BigDecimal("5.0"), 1, null, null));

        mockMvc.perform(delete(ApiEndpointConstants.PRODUCT + "/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldFailDeleteProductAsUser() throws Exception {
        Product saved = productRepository.save(new Product(null, "Delete Me", "Desc", new BigDecimal("5.0"), 1, null, null));

        mockMvc.perform(delete(ApiEndpointConstants.PRODUCT + "/" + saved.getId()))
                .andExpect(status().isForbidden());
    }
}