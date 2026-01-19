package com.company.productmanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * OpenAPI/Swagger configuration for API documentation
 * Accessible at /swagger-ui.html
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Product Management API",
                description = "REST API for Product Management with JWT Authentication",
                version = "1.0.0",
                contact = @Contact(
                        name = "Shruti Sharma",
                        email = "shruti.bhrwj@gmail.com"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development Server",
                        url = "http://localhost:8080"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        description = "JWT authentication. Format: Bearer {token}",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
