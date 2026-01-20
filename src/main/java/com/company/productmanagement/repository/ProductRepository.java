package com.company.productmanagement.repository;

import com.company.productmanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * Find if product exists by the name
     * 
     * @param name 
     * @return boolean
     */
    Boolean existsByName(String name);
    
}
