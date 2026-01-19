package com.company.productmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Product Management System
 * Secure REST API with JWT authentication
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
@SpringBootApplication
public class ProductManagementApplication {

     /**
     * Application entry point
     */
	public static void main(String[] args) {
		SpringApplication.run(ProductManagementApplication.class, args);
	}

}
