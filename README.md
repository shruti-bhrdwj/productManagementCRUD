# Product Management System

A secure REST API for product management with JWT authentication built using Spring Boot 3.2.

## Features

- **JWT Authentication**: Stateless authentication using JSON Web Tokens
- **Product CRUD Operations**: Complete product management functionality
- **Global Exception Handling**: Centralized error handling with custom error responses
- **Record-based DTOs**: Immutable data transfer objects using Java Records
- **Swagger/OpenAPI Documentation**: Interactive API documentation
- **Comprehensive Unit Tests**: High test coverage with JUnit 5 and Mockito
- **Security**: Spring Security with JWT token validation

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Security**
- **Spring Data JPA**
- **MySQL 8.0**
- **JWT (jjwt 0.12.3)**
- **Lombok**
- **SpringDoc OpenAPI**
- **JUnit 5**
- **Mockito**

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd productmanagement
```

### 2. Configure MySQL Database

Create a MySQL database:

```sql
CREATE DATABASE product_management_db;
```

Update `src/main/resources/application.yml` with your MySQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/product_management_db?createDatabaseIfNotExist=true
    username: your_username
    password: your_password
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

### Swagger UI

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### API Endpoints

#### Authentication Endpoints

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Login and get JWT token | No |

#### Product Endpoints (Protected)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| POST | `/api/products` | Create a new product | Required |
| GET | `/api/products` | Get all products | Required |
| GET | `/api/products/{id}` | Get product by ID | Required |
| PUT | `/api/products/{id}` | Update a product | Required |
| DELETE | `/api/products/{id}` | Delete a product | Required |


## Error Response Format

All errors follow a consistent format:

```json
{
  "message": "Product not found",
  "code": "pdm-prod-001",
  "timestamp": "2025-01-19T10:00:00"
}
```

## Running Tests

Execute all tests:

```bash
mvn test
```

Run specific test class:

```bash
mvn test -Dtest=ProductServiceTest
```

## Security

- Passwords are encrypted using BCrypt
- JWT tokens expire after 24 hours (configurable in `application.yml`)
- All product endpoints require valid JWT authentication
- CSRF protection is disabled (stateless REST API)
- Session management is stateless

## Best Practices Implemented

1. **Separation of Concerns**: Clear separation between layers (Controller, Service, Repository)
2. **DTOs**: Using Records for immutable data transfer objects
3. **Exception Handling**: Centralized error handling with `@ControllerAdvice`
4. **Constants**: Centralized API endpoint constants
5. **Validation**: Bean validation on request DTOs
6. **Documentation**: Comprehensive JavaDoc and OpenAPI documentation
7. **Testing**: Unit tests for services and controllers
8. **Security**: JWT-based stateless authentication
9. **Clean Code**: Lombok for reducing boilerplate code

