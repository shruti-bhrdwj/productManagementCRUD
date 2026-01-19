package com.company.productmanagement.repository;

import com.company.productmanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for Product entity
 * Provides database operations for product management
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find products by category
     * 
     * @param category the category to search for
     * @return list of products in the category
     */
    List<Product> findByCategory(String category);
    
    /**
     * Find products by name containing (case-insensitive)
     * 
     * @param name the name pattern to search for
     * @return list of matching products
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products by price range
     * 
     * @param minPrice the minimum price
     * @param maxPrice the maximum price
     * @return list of products in the price range
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
}
