package com.company.productmanagement.controller.product;

import com.company.productmanagement.controller.ProductController;
import com.company.productmanagement.dto.product.ProductRequest;
import com.company.productmanagement.dto.product.ProductResponse;
import com.company.productmanagement.exception.GlobalExceptionHandler;
import com.company.productmanagement.service.ProductService;
import com.company.productmanagement.utils.ApiEndpointConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters
@ContextConfiguration(classes = { ProductController.class, GlobalExceptionHandler.class})
class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ProductService productService;

        // ----- CREATE TESTS -----
        @Test
        void shouldCreateProductSuccessfully() throws Exception {
                ProductRequest request = new ProductRequest(
                                "Test Product", "Description", new BigDecimal("99.99"), 10);

                ProductResponse response = new ProductResponse(
                                1L, "Test Product", "Description", new BigDecimal("99.99"),
                                10, LocalDateTime.now(), LocalDateTime.now());

                when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);

                mockMvc.perform(post(ApiEndpointConstants.PRODUCT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Test Product"))
                                .andExpect(jsonPath("$.price").value(99.99));
        }

        @Test
        void shouldFailWhenNameBlank() throws Exception {
                ProductRequest request = new ProductRequest(
                                "", "Description", new BigDecimal("10.0"), 5);

                mockMvc.perform(post(ApiEndpointConstants.PRODUCT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-7")); // Product name is required
        }

        @Test
        void shouldFailWhenPriceNullOrInvalid() throws Exception {
                // Price null
                ProductRequest requestNull = new ProductRequest("Product", "Desc", null, 5);
                mockMvc.perform(post(ApiEndpointConstants.PRODUCT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestNull)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-10")); // Price required

                // Price zero
                ProductRequest requestZero = new ProductRequest("Product", "Desc", BigDecimal.ZERO, 5);
                mockMvc.perform(post(ApiEndpointConstants.PRODUCT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestZero)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-11")); // Price must be > 0
        }

        @Test
        void shouldFailWhenQuantityNegative() throws Exception {
                ProductRequest request = new ProductRequest(
                                "Product", "Desc", new BigDecimal("10.0"), -1);

                mockMvc.perform(post(ApiEndpointConstants.PRODUCT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("v-13")); // Quantity cannot be negative
        }

        // ----- GET TESTS -----

        @Test
        void shouldGetAllProductsSuccessfully() throws Exception {
                List<ProductResponse> products = List.of(
                                new ProductResponse(1L, "Product 1", "Desc 1",
                                                new BigDecimal("99.99"), 10,
                                                LocalDateTime.now(), LocalDateTime.now()),
                                new ProductResponse(2L, "Product 2", "Desc 2",
                                                new BigDecimal("149.99"), 5,
                                                LocalDateTime.now(), LocalDateTime.now()));

                // Create Pageable and Page with it
                Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
                Page<ProductResponse> page = new PageImpl<>(products, pageable, products.size());

                when(productService.getAllProducts(any(Pageable.class)))
                                .thenReturn(page);

                mockMvc.perform(get(ApiEndpointConstants.PRODUCT)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "id"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(2))
                                .andExpect(jsonPath("$.content[0].name").value("Product 1"))
                                .andExpect(jsonPath("$.content[1].name").value("Product 2"));

                verify(productService).getAllProducts(any(Pageable.class));
        }

        @Test
        void shouldGetProductByIdSuccessfully() throws Exception {
                ProductResponse response = new ProductResponse(
                                1L, "Test Product", "Description", new BigDecimal("99.99"),
                                10, LocalDateTime.now(), LocalDateTime.now());

                when(productService.getProductById(1L)).thenReturn(response);

                mockMvc.perform(get(ApiEndpointConstants.PRODUCT + "/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Test Product"));
        }

        @Test
        void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
                when(productService.getProductById(999L))
                                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "pdm-1"));

                mockMvc.perform(get(ApiEndpointConstants.PRODUCT + "/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("pdm-1"));
        }

        // ----- UPDATE TESTS -----
        @Test
        void shouldUpdateProductSuccessfully() throws Exception {
                ProductRequest request = new ProductRequest(
                                "Updated Product", "Updated Desc", new BigDecimal("149.99"), 20);

                ProductResponse response = new ProductResponse(
                                1L, "Updated Product", "Updated Desc", new BigDecimal("149.99"),
                                20, LocalDateTime.now(), LocalDateTime.now());

                when(productService.updateProduct(eq(1L), any(ProductRequest.class))).thenReturn(response);

                mockMvc.perform(put(ApiEndpointConstants.PRODUCT + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Product"))
                                .andExpect(jsonPath("$.price").value(149.99));
        }

        // ----- DELETE TEST -----
        @Test
        void shouldDeleteProductSuccessfully() throws Exception {
                doNothing().when(productService).deleteProduct(1L);

                mockMvc.perform(delete(ApiEndpointConstants.PRODUCT + "/1"))
                                .andExpect(status().isNoContent());

                verify(productService, times(1)).deleteProduct(1L);
        }
}