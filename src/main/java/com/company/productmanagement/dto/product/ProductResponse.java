package com.company.productmanagement.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for product response
 * 
 * @param id product ID
 * @param name product name
 * @param description product description
 * @param price product price
 * @param quantity available quantity
 * @param category product category
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record ProductResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer quantity,
    String category,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
