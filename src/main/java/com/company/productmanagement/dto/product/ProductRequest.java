package com.company.productmanagement.dto.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO for creating a new product
 * 
 * @param name product name
 * @param description product description
 * @param price product price
 * @param quantity available quantity
 * @param category product category
 */
public record ProductRequest(
    @NotBlank(message = "{v-07}")
    @Size(max = 100, message = "{v-08}")
    String name,
    
    @Size(max = 500, message = "{v-09}")
    String description,
    
    @NotNull(message = "{v-10}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{v-11}")
    @Digits(integer = 8, fraction = 2, message = "{v-12}")
    BigDecimal price,
    
    @Min(value = 0, message = "{v-13}")
    Integer quantity,
    
    @Size(max = 50, message = "{v-14}")
    String category
) {}
