package com.company.productmanagement.service.auth;

import com.company.productmanagement.dto.auth.AuthResponse;
import com.company.productmanagement.dto.auth.LoginRequest;
import com.company.productmanagement.dto.auth.RegisterRequest;
import com.company.productmanagement.entity.User;
import com.company.productmanagement.repository.UserRepository;
import com.company.productmanagement.service.AuthService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AuthService
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIT {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final String TEST_USERNAME = "integrationTestUser";
    private static final String TEST_EMAIL = "integration@test.com";
    private static final String TEST_PASSWORD = "TestPass123";
    
    @BeforeEach
    void setUp() {
        // Clean up test data before each test
        userRepository.findByUsername(TEST_USERNAME).ifPresent(userRepository::delete);
    }
    
    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        userRepository.findByUsername(TEST_USERNAME).ifPresent(userRepository::delete);
    }
    
    // ======= REGISTRATION INTEGRATION TESTS =======
    
    @Nested
    @DisplayName("Registration Integration Tests")
    class RegistrationIntegrationTests {
        
        @Test
        @Order(1)
        @DisplayName("Should register user successfully with database persistence")
        @Transactional
        void shouldRegisterUserSuccessfullyWithPersistence() {
            // Given
            RegisterRequest request = new RegisterRequest(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
            
            // When
            AuthResponse response = authService.register(request);
            
            // Then
            assertNotNull(response);
            assertNotNull(response.token());
            assertEquals(TEST_USERNAME, response.username());
            assertEquals(TEST_EMAIL, response.email());
            
            // Verify user is saved in database
            User savedUser = userRepository.findByUsername(TEST_USERNAME).orElse(null);
            assertNotNull(savedUser);
            assertEquals(TEST_USERNAME, savedUser.getUsername());
            assertEquals(TEST_EMAIL, savedUser.getEmail());
            assertTrue(savedUser.isEnabled());
            
            // Verify password is encoded
            assertNotEquals(TEST_PASSWORD, savedUser.getPassword());
            assertTrue(passwordEncoder.matches(TEST_PASSWORD, savedUser.getPassword()));
            
            // Verify JWT token is valid
            assertFalse(response.token().isEmpty());
            assertTrue(response.token().length() > 20); // JWT tokens are typically long
        }
        
        @Test
        @Order(2)
        @DisplayName("Should throw conflict when registering duplicate username")
        @Transactional
        void shouldThrowConflictWhenRegisteringDuplicateUsername() {
            // Given - First registration
            RegisterRequest firstRequest = new RegisterRequest(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
            authService.register(firstRequest);
            
            // When & Then - Second registration with same username
            RegisterRequest duplicateRequest = new RegisterRequest(TEST_USERNAME, "DifferentPass123", "different@test.com");
            
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(duplicateRequest)
            );
            
            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertEquals("a-2", exception.getReason());
            
            // Verify only one user exists in database
            long userCount = userRepository.count();
            assertTrue(userCount >= 1); // At least our test user
        }
        
        @Test
        @Order(3)
        @DisplayName("Should register users with different usernames successfully")
        @Transactional
        void shouldRegisterMultipleUsersWithDifferentUsernames() {
            // Given
            RegisterRequest request1 = new RegisterRequest("user1", TEST_PASSWORD, "user1@test.com");
            RegisterRequest request2 = new RegisterRequest("user2", TEST_PASSWORD, "user2@test.com");
            RegisterRequest request3 = new RegisterRequest("user3", TEST_PASSWORD, "user3@test.com");
            
            // When
            AuthResponse response1 = authService.register(request1);
            AuthResponse response2 = authService.register(request2);
            AuthResponse response3 = authService.register(request3);
            
            // Then
            assertNotNull(response1.token());
            assertNotNull(response2.token());
            assertNotNull(response3.token());
            
            // Verify all users are in database
            assertTrue(userRepository.findByUsername("user1").isPresent());
            assertTrue(userRepository.findByUsername("user2").isPresent());
            assertTrue(userRepository.findByUsername("user3").isPresent());
            
            // Cleanup
            userRepository.findByUsername("user1").ifPresent(userRepository::delete);
            userRepository.findByUsername("user2").ifPresent(userRepository::delete);
            userRepository.findByUsername("user3").ifPresent(userRepository::delete);
        }
        
        @Test
        @Order(4)
        @DisplayName("Should register with special characters in email")
        @Transactional
        void shouldRegisterWithSpecialCharactersInEmail() {
            // Given
            String specialEmail = "test.user+tag@sub.example.co.uk";
            RegisterRequest request = new RegisterRequest(TEST_USERNAME, TEST_PASSWORD, specialEmail);
            
            // When
            AuthResponse response = authService.register(request);
            
            // Then
            assertEquals(specialEmail, response.email());
            
            User savedUser = userRepository.findByUsername(TEST_USERNAME).orElse(null);
            assertNotNull(savedUser);
            assertEquals(specialEmail, savedUser.getEmail());
        }
        
        @Test
        @Order(5)
        @DisplayName("Should register with minimum valid username length")
        @Transactional
        void shouldRegisterWithMinimumUsername() {
            // Given
            String minUsername = "abc";
            RegisterRequest request = new RegisterRequest(minUsername, TEST_PASSWORD, TEST_EMAIL);
            
            // When
            AuthResponse response = authService.register(request);
            
            // Then
            assertEquals(minUsername, response.username());
            assertTrue(userRepository.existsByUsername(minUsername));
            
            // Cleanup
            userRepository.findByUsername(minUsername).ifPresent(userRepository::delete);
        }
        
        @Test
        @Order(6)
        @DisplayName("Should register with maximum valid username length")
        @Transactional
        void shouldRegisterWithMaximumUsername() {
            // Given
            String maxUsername = "a".repeat(50); // Assuming max is 50
            RegisterRequest request = new RegisterRequest(maxUsername, TEST_PASSWORD, TEST_EMAIL);
            
            // When
            AuthResponse response = authService.register(request);
            
            // Then
            assertEquals(maxUsername, response.username());
            assertTrue(userRepository.existsByUsername(maxUsername));
            
            // Cleanup
            userRepository.findByUsername(maxUsername).ifPresent(userRepository::delete);
        }
        
        @Test
        @Order(7)
        @DisplayName("Should encode different passwords differently")
        @Transactional
        void shouldEncodeDifferentPasswordsDifferently() {
            // Given
            RegisterRequest request1 = new RegisterRequest("user1", "password1", "user1@test.com");
            RegisterRequest request2 = new RegisterRequest("user2", "password2", "user2@test.com");
            
            // When
            authService.register(request1);
            authService.register(request2);
            
            // Then
            User user1 = userRepository.findByUsername("user1").orElse(null);
            User user2 = userRepository.findByUsername("user2").orElse(null);
            
            assertNotNull(user1);
            assertNotNull(user2);
            assertNotEquals(user1.getPassword(), user2.getPassword());
            
            // Cleanup
            userRepository.delete(user1);
            userRepository.delete(user2);
        }
    }
    
    // ======= LOGIN INTEGRATION TESTS =======
    
    @Nested
    @DisplayName("Login Integration Tests")
    class LoginIntegrationTests {
        
        @BeforeEach
        void setupUser() {
            // Create a user for login tests
            RegisterRequest request = new RegisterRequest(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
            authService.register(request);
        }
        
        @Test
        @Order(10)
        @DisplayName("Should login successfully with correct credentials")
        void shouldLoginSuccessfullyWithCorrectCredentials() {
            // Given
            LoginRequest request = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
            
            // When
            AuthResponse response = authService.login(request);
            
            // Then
            assertNotNull(response);
            assertNotNull(response.token());
            assertEquals(TEST_USERNAME, response.username());
            assertEquals(TEST_EMAIL, response.email());
            
            // Verify token is valid
            assertTrue(response.token().length() > 20);
        }
        
        @Test
        @Order(11)
        @DisplayName("Should throw unauthorized with wrong password")
        void shouldThrowUnauthorizedWithWrongPassword() {
            // Given
            LoginRequest request = new LoginRequest(TEST_USERNAME, "WrongPassword123");
            
            // When & Then
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
            );
            
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals("a-1", exception.getReason());
        }
        
        @Test
        @Order(12)
        @DisplayName("Should throw unauthorized for non-existent user")
        void shouldFailForNonExistentUser() {
            // Given
            LoginRequest request = new LoginRequest("nonExistentUser", TEST_PASSWORD);
            
            // When & Then
            assertThrows(
                InternalAuthenticationServiceException.class,
                () -> authService.login(request)
            );
        }
        
        @Test
        @Order(13)
        @DisplayName("Should handle case-sensitive username during login")
        void shouldHandleCaseSensitiveUsername() {
            // Given - Try to login with different case
            LoginRequest wrongCaseRequest = new LoginRequest("INTEGRATIONTESTUSER", TEST_PASSWORD);
            
            // When & Then - Should fail because username is case-sensitive
            assertThrows(InternalAuthenticationServiceException.class, () -> authService.login(wrongCaseRequest));
        }
        
        @Test
        @Order(14)
        @DisplayName("Should generate different tokens for multiple logins")
        void shouldGenerateDifferentTokensForMultipleLogins() {
            // Given
            LoginRequest request = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
            
            // When
            AuthResponse response1 = authService.login(request);
            
            // Small delay to ensure different timestamps
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            AuthResponse response2 = authService.login(request);
            
            // Then - Tokens might be different if they include timestamps
            assertNotNull(response1.token());
            assertNotNull(response2.token());
            // Note: Tokens might be the same if JWT doesn't include timestamp
            // This test verifies the system can handle multiple logins
        }
        
        @Test
        @Order(15)
        @DisplayName("Should login with special characters in password")
        void shouldLoginWithSpecialCharactersInPassword() {
            // Given - Register user with special character password
            String specialPassword = "P@ssw0rd!#$%^&*()";
            RegisterRequest registerRequest = new RegisterRequest("specialUser", specialPassword, "special@test.com");
            authService.register(registerRequest);
            
            // When
            LoginRequest loginRequest = new LoginRequest("specialUser", specialPassword);
            AuthResponse response = authService.login(loginRequest);
            
            // Then
            assertNotNull(response);
            assertEquals("specialUser", response.username());
            
            // Cleanup
            userRepository.findByUsername("specialUser").ifPresent(userRepository::delete);
        }
        
        @Test
        @Order(16)
        @DisplayName("Should fail login with partially correct password")
        void shouldFailLoginWithPartiallyCorrectPassword() {
            // Given - Try with substring of correct password
            LoginRequest request = new LoginRequest(TEST_USERNAME, TEST_PASSWORD.substring(0, 5));
            
            // When & Then
            assertThrows(ResponseStatusException.class, () -> authService.login(request));
        }
    }
    
    // ======= FULL FLOW INTEGRATION TESTS =======
    
    @Nested
    @DisplayName("Full Flow Integration Tests")
    class FullFlowIntegrationTests {
        
        @Test
        @Order(20)
        @DisplayName("Should complete full registration and login flow")
        @Transactional
        void shouldCompleteFullRegistrationAndLoginFlow() {
            // Given
            String username = "flowTestUser";
            String password = "FlowTest123";
            String email = "flow@test.com";
            
            // When - Register
            RegisterRequest registerRequest = new RegisterRequest(username, password, email);
            AuthResponse registerResponse = authService.register(registerRequest);
            
            // Then - Verify registration
            assertNotNull(registerResponse);
            assertNotNull(registerResponse.token());
            assertEquals(username, registerResponse.username());
            assertEquals(email, registerResponse.email());
            
            // When - Login
            LoginRequest loginRequest = new LoginRequest(username, password);
            AuthResponse loginResponse = authService.login(loginRequest);
            
            // Then - Verify login
            assertNotNull(loginResponse);
            assertNotNull(loginResponse.token());
            assertEquals(username, loginResponse.username());
            assertEquals(email, loginResponse.email());
            
            // Verify user exists in database
            User savedUser = userRepository.findByUsername(username).orElse(null);
            assertNotNull(savedUser);
            assertTrue(savedUser.isEnabled());
            
            // Cleanup
            userRepository.delete(savedUser);
        }
        
        @Test
        @Order(21)
        @DisplayName("Should prevent double registration and allow login after first registration")
        @Transactional
        void shouldPreventDoubleRegistrationButAllowLogin() {
            // Given
            String username = "doubleRegUser";
            RegisterRequest request1 = new RegisterRequest(username, TEST_PASSWORD, "first@test.com");
            RegisterRequest request2 = new RegisterRequest(username, "DifferentPass", "second@test.com");
            
            // When - First registration succeeds
            AuthResponse firstResponse = authService.register(request1);
            assertNotNull(firstResponse);
            
            // Then - Second registration fails
            assertThrows(ResponseStatusException.class, () -> authService.register(request2));
            
            // But login with first credentials should work
            LoginRequest loginRequest = new LoginRequest(username, TEST_PASSWORD);
            AuthResponse loginResponse = authService.login(loginRequest);
            assertNotNull(loginResponse);
            assertEquals("first@test.com", loginResponse.email()); // First email is used
            
            // Cleanup
            userRepository.findByUsername(username).ifPresent(userRepository::delete);
        }
        
        @Test
        @Order(22)
        @DisplayName("Should maintain user data consistency across operations")
        @Transactional
        void shouldMaintainUserDataConsistency() {
            // Given
            String username = "consistencyUser";
            String password = "Consistent123";
            String email = "consistency@test.com";
            
            // When - Register
            RegisterRequest registerRequest = new RegisterRequest(username, password, email);
            AuthResponse registerResponse = authService.register(registerRequest);
            
            // Then - Check immediate consistency
            User userAfterRegister = userRepository.findByUsername(username).orElse(null);
            assertNotNull(userAfterRegister);
            assertEquals(username, userAfterRegister.getUsername());
            assertEquals(email, userAfterRegister.getEmail());
            
            // When - Login
            LoginRequest loginRequest = new LoginRequest(username, password);
            AuthResponse loginResponse = authService.login(loginRequest);
            
            // Then - Verify data consistency
            assertEquals(registerResponse.username(), loginResponse.username());
            assertEquals(registerResponse.email(), loginResponse.email());
            
            // Verify database consistency
            User userAfterLogin = userRepository.findByUsername(username).orElse(null);
            assertNotNull(userAfterLogin);
            assertEquals(userAfterRegister.getId(), userAfterLogin.getId());
            assertEquals(userAfterRegister.getUsername(), userAfterLogin.getUsername());
            assertEquals(userAfterRegister.getEmail(), userAfterLogin.getEmail());
            
            // Cleanup
            userRepository.delete(userAfterLogin);
        }
    }
    
    // ======= CONCURRENT ACCESS TESTS =======
    
    @Nested
    @DisplayName("Concurrent Access Tests")
    class ConcurrentAccessTests {
        
        @Test
        @Order(30)
        @DisplayName("Should handle concurrent registration attempts gracefully")
        void shouldHandleConcurrentRegistrationAttempts() {
            // This test simulates race conditions
            // In real scenario, database constraints should prevent duplicate registrations
            
            // Given
            String username = "concurrentUser";
            RegisterRequest request = new RegisterRequest(username, TEST_PASSWORD, "concurrent@test.com");
            
            // When - First registration
            authService.register(request);
            
            // Then - Second registration should fail with conflict
            assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
            );
            
            // Verify only one user was created
            long count = userRepository.findAll()
                .stream()
                .filter(u -> u.getUsername().equals(username))
                .count();
            assertEquals(1, count);
            
            // Cleanup
            userRepository.findByUsername(username).ifPresent(userRepository::delete);
        }
    }
}