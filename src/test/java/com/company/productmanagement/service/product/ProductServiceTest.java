package com.company.productmanagement.service.product;

import com.company.productmanagement.dto.product.ProductRequest;
import com.company.productmanagement.dto.product.ProductResponse;
import com.company.productmanagement.entity.Product;
import com.company.productmanagement.repository.ProductRepository;
import com.company.productmanagement.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Desc")
                .price(new BigDecimal("10.0"))
                .quantity(5)
                .build();
    }

    // ---------------- CREATE ----------------
    @Test
    void shouldCreateProductSuccessfully() {
        ProductRequest request = new ProductRequest("Test Product", "Desc", new BigDecimal("10.0"), 5);

        when(productRepository.existsByName("Test Product")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertEquals("Test Product", response.name());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldFailCreateWhenProductExists() {
        when(productRepository.existsByName("Test Product")).thenReturn(true);

        ProductRequest request = new ProductRequest("Test Product", "Desc", new BigDecimal("10.0"), 5);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.createProduct(request));

        assertEquals("pdm-2", exception.getReason());
    }

    // ---------------- GET ----------------
    @Test
    void shouldGetProductByIdSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertEquals("Test Product", response.name());
    }

    @Test
    void shouldFailGetProductByInvalidId() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.getProductById(1L));

        assertEquals("pdm-1", exception.getReason());
    }

    // ---------------- UPDATE ----------------
    @Test
    void shouldUpdateProductSuccessfully() {
        ProductRequest request = new ProductRequest("Updated", "New Desc", new BigDecimal("20.0"), 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.updateProduct(1L, request);

        assertEquals("Updated", response.name());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldFailUpdateInvalidId() {
        ProductRequest request = new ProductRequest("Updated", "New Desc", new BigDecimal("20.0"), 10);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.updateProduct(1L, request));

        assertEquals("pdm-1", exception.getReason());
    }

    // ---------------- DELETE ----------------
    @Test
    void shouldDeleteProductSuccessfully() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldFailDeleteInvalidId() {
        when(productRepository.existsById(1L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.deleteProduct(1L));

        assertEquals("pdm-1", exception.getReason());
    }

    // ---------------- GET ALL ----------------
    @Test
    void shouldGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> list = productService.getAllProducts();

        assertEquals(1, list.size());
        assertEquals("Test Product", list.get(0).name());
    }
}