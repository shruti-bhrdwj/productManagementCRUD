package com.company.productmanagement.service.product;

import com.company.productmanagement.dto.product.ProductRequest;
import com.company.productmanagement.dto.product.ProductResponse;
import com.company.productmanagement.entity.Product;
import com.company.productmanagement.repository.ProductRepository;
import com.company.productmanagement.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceIT {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDb() {
        productRepository.deleteAll();
    }

    @Test
    void shouldCreateAndGetProduct() {
        ProductRequest request = new ProductRequest("IT Product", "Desc", new BigDecimal("50.0"), 5);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response.id());
        assertEquals("IT Product", response.name());

        ProductResponse fetched = productService.getProductById(response.id());
        assertEquals("IT Product", fetched.name());
    }

    @Test
    void shouldUpdateProduct() {
        Product saved = productRepository.save(new Product(null, "Old", "Desc", new BigDecimal("20.0"), 2, null, null));

        ProductRequest request = new ProductRequest("Updated", "New Desc", new BigDecimal("30.0"), 5);
        ProductResponse response = productService.updateProduct(saved.getId(), request);

        assertEquals("Updated", response.name());
        assertEquals(new BigDecimal("30.0"), response.price());
    }

    @Test
    void shouldDeleteProduct() {
        Product saved = productRepository.save(new Product(null, "To Delete", "Desc", new BigDecimal("10.0"), 1, null, null));

        productService.deleteProduct(saved.getId());
        assertFalse(productRepository.existsById(saved.getId()));
    }

    @Test
    void shouldGetAllProducts() {
        productRepository.save(new Product(null, "P1", "Desc1", new BigDecimal("10.0"), 1, null, null));
        productRepository.save(new Product(null, "P2", "Desc2", new BigDecimal("20.0"), 2, null, null));

        List<ProductResponse> products = productService.getAllProducts();

        assertEquals(2, products.size());
    }

    @Test
    void shouldThrowExceptionForInvalidId() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.getProductById(999L));
        assertEquals("pdm-1", ex.getReason());
    }
}

