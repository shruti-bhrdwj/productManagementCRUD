package com.company.productmanagement.service.auth;

import com.company.productmanagement.dto.auth.AuthResponse;
import com.company.productmanagement.dto.auth.LoginRequest;
import com.company.productmanagement.dto.auth.RegisterRequest;
import com.company.productmanagement.entity.User;
import com.company.productmanagement.repository.UserRepository;
import com.company.productmanagement.service.AuthService;
import com.company.productmanagement.utils.JwtUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @InjectMocks
    private AuthService authService;
    
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "password123", "test@example.com");
        loginRequest = new LoginRequest("testuser", "password123");
        
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .build();
    }
    
    // ======= REGISTRATION TESTS =======
    
    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {
        
        @Test
        @DisplayName("Should register user successfully with valid data")
        void shouldRegisterUserSuccessfully() {
            // Given
            when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtils.generateToken(any(User.class))).thenReturn("jwt-token");
            
            // When
            AuthResponse response = authService.register(registerRequest);
            
            // Then
            assertNotNull(response);
            assertEquals("jwt-token", response.token());
            assertEquals("testuser", response.username());
            assertEquals("test@example.com", response.email());
            
            verify(userRepository).existsByUsername(registerRequest.username());
            verify(passwordEncoder).encode(registerRequest.password());
            verify(userRepository).save(any(User.class));
            verify(jwtUtils).generateToken(any(User.class));
        }
        
        @Test
        @DisplayName("Should throw CONFLICT when username already exists")
        void shouldThrowConflictWhenUsernameExists() {
            // Given
            when(userRepository.existsByUsername(registerRequest.username())).thenReturn(true);
            
            // When & Then
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(registerRequest)
            );
            
            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertEquals("a-2", exception.getReason());
            
            verify(userRepository).existsByUsername(registerRequest.username());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
            verify(jwtUtils, never()).generateToken(any(User.class));
        }
        
        @Test
        @DisplayName("Should encode password before saving user")
        void shouldEncodePasswordBeforeSaving() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtils.generateToken(any(User.class))).thenReturn("jwt-token");
            
            // When
            authService.register(registerRequest);
            
            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            verify(passwordEncoder).encode("password123");
            
            User savedUser = userCaptor.getValue();
            assertNotNull(savedUser);
            assertEquals("testuser", savedUser.getUsername());
            assertEquals("test@example.com", savedUser.getEmail());
        }
        
        @Test
        @DisplayName("Should set user as enabled by default")
        void shouldSetUserAsEnabledByDefault() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtils.generateToken(any(User.class))).thenReturn("jwt-token");
            
            // When
            authService.register(registerRequest);
            
            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            
            User savedUser = userCaptor.getValue();
            assertTrue(savedUser.isEnabled());
        }
        
        @Test
        @DisplayName("Should generate JWT token after successful registration")
        void shouldGenerateTokenAfterSuccessfulRegistration() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtils.generateToken(user)).thenReturn("generated-jwt-token");
            
            // When
            AuthResponse response = authService.register(registerRequest);
            
            // Then
            assertEquals("generated-jwt-token", response.token());
            verify(jwtUtils).generateToken(user);
        }
        
        @Test
        @DisplayName("Should register with valid email containing special characters")
        void shouldRegisterWithValidEmailFormat() {
            // Given
            RegisterRequest emailRequest = new RegisterRequest(
                "testuser", "password123", "test.user+tag@example.co.uk"
            );
            User emailUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test.user+tag@example.co.uk")
                .password("encodedPassword")
                .enabled(true)
                .build();
            
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(emailUser);
            when(jwtUtils.generateToken(any(User.class))).thenReturn("jwt-token");
            
            // When
            AuthResponse response = authService.register(emailRequest);
            
            // Then
            assertEquals("test.user+tag@example.co.uk", response.email());
        }
        
        @Test
        @DisplayName("Should register with minimum valid username length (3 chars)")
        void shouldRegisterWithMinimumValidUsername() {
            // Given
            RegisterRequest minRequest = new RegisterRequest("abc", "password123", "test@example.com");
            User minUser = User.builder()
                .id(1L)
                .username("abc")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .build();
            
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(minUser);
            when(jwtUtils.generateToken(any(User.class))).thenReturn("jwt-token");
            
            // When
            AuthResponse response = authService.register(minRequest);
            
            // Then
            assertEquals("abc", response.username());
        }
        
        @Test
        @DisplayName("Should register with minimum valid password length (6 chars)")
        void shouldRegisterWithMinimumValidPassword() {
            // Given
            RegisterRequest minPassRequest = new RegisterRequest("testuser", "pass12", "test@example.com");
            
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode("pass12")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtils.generateToken(any(User.class))).thenReturn("jwt-token");
            
            // When
            AuthResponse response = authService.register(minPassRequest);
            
            // Then
            assertNotNull(response);
            verify(passwordEncoder).encode("pass12");
        }
        
        @Test
        @DisplayName("Should build user correctly with all fields during registration")
        void shouldBuildUserCorrectlyDuringRegistration() {
            // Given
            when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                assertEquals("testuser", savedUser.getUsername());
                assertEquals("test@example.com", savedUser.getEmail());
                assertEquals("encodedPassword", savedUser.getPassword());
                assertTrue(savedUser.isEnabled());
                return savedUser;
            });
            when(jwtUtils.generateToken(any(User.class))).thenReturn("jwt-token");
            
            // When
            authService.register(registerRequest);
            
            // Then
            verify(userRepository).save(any(User.class));
        }
        
        @Test
        @DisplayName("Should propagate RuntimeException when repository save fails")
        void shouldPropagateExceptionWhenRepositorySaveFails() {
            // Given
            when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Database error"));
            
            // When & Then
            assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
            
            verify(userRepository).existsByUsername(registerRequest.username());
            verify(passwordEncoder).encode(registerRequest.password());
            verify(userRepository).save(any(User.class));
            verify(jwtUtils, never()).generateToken(any(User.class));
        }
        
        @Test
        @DisplayName("Should propagate DataAccessException when database fails")
        void shouldPropagateDataAccessException() {
            // Given
            when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                .thenThrow(new DataAccessException("DB connection failed") {});
            
            // When & Then
            assertThrows(DataAccessException.class, () -> authService.register(registerRequest));
            
            verify(jwtUtils, never()).generateToken(any(User.class));
        }
        
        @Test
        @DisplayName("Should check username existence before proceeding with registration")
        void shouldCheckUsernameExistenceFirst() {
            // Given
            when(userRepository.existsByUsername(registerRequest.username())).thenReturn(true);
            
            // When & Then
            assertThrows(ResponseStatusException.class, () -> authService.register(registerRequest));
            
            // Verify that no other operations are performed
            verify(userRepository).existsByUsername(registerRequest.username());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
            verify(jwtUtils, never()).generateToken(any(User.class));
        }
    }
    
    // ======= LOGIN TESTS =======
    
    @Nested
    @DisplayName("Login Tests")
    class LoginTests {
        
        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            // Given
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(jwtUtils.generateToken(user)).thenReturn("jwt-token");
            
            // When
            AuthResponse response = authService.login(loginRequest);
            
            // Then
            assertNotNull(response);
            assertEquals("jwt-token", response.token());
            assertEquals("testuser", response.username());
            assertEquals("test@example.com", response.email());
            
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtils).generateToken(user);
        }
        
        @Test
        @DisplayName("Should throw UNAUTHORIZED for invalid credentials")
        void shouldThrowUnauthorizedForInvalidCredentials() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
            
            // When & Then
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest)
            );
            
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals("a-1", exception.getReason());
            
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtils, never()).generateToken(any(User.class));
        }
        
        @Test
        @DisplayName("Should throw UNAUTHORIZED for non-existent user")
        void shouldThrowUnauthorizedForNonExistentUser() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("User not found"));
            
            // When & Then
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest)
            );
            
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals("a-1", exception.getReason());
        }
        
        @Test
        @DisplayName("Should propagate general AuthenticationException")
        void shouldPropagateGeneralAuthenticationException() {
            // Given - Simulate a general AuthenticationException (not BadCredentialsException)
            AuthenticationException authException = new InternalAuthenticationServiceException("Auth service error");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(authException);
            
            // When & Then - The service only catches BadCredentialsException, so others should propagate
            assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));
            
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtils, never()).generateToken(any(User.class));
        }
        
        @Test
        @DisplayName("Should handle case-sensitive username")
        void shouldHandleCaseSensitiveUsername() {
            // Given
            LoginRequest caseSensitiveRequest = new LoginRequest("TestUser", "password123");
            User caseSensitiveUser = User.builder()
                .id(1L)
                .username("TestUser")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .build();
            
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(caseSensitiveUser);
            when(jwtUtils.generateToken(caseSensitiveUser)).thenReturn("jwt-token");
            
            // When
            AuthResponse response = authService.login(caseSensitiveRequest);
            
            // Then
            assertEquals("TestUser", response.username());
        }
        
        @Test
        @DisplayName("Should handle special characters in password")
        void shouldHandleSpecialCharactersInPassword() {
            // Given
            LoginRequest specialPassRequest = new LoginRequest("testuser", "P@ssw0rd!#$");
            Authentication authentication = mock(Authentication.class);
            
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(jwtUtils.generateToken(user)).thenReturn("jwt-token");
            
            // When
            AuthResponse response = authService.login(specialPassRequest);
            
            // Then
            assertNotNull(response);
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
        
        @Test
        @DisplayName("Should create correct UsernamePasswordAuthenticationToken")
        void shouldCreateCorrectAuthenticationToken() {
            // Given
            Authentication authentication = mock(Authentication.class);
            ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor = 
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            
            when(authenticationManager.authenticate(tokenCaptor.capture()))
                .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(jwtUtils.generateToken(user)).thenReturn("jwt-token");
            
            // When
            authService.login(loginRequest);
            
            // Then
            UsernamePasswordAuthenticationToken capturedToken = tokenCaptor.getValue();
            assertEquals("testuser", capturedToken.getPrincipal());
            assertEquals("password123", capturedToken.getCredentials());
        }
        
        @Test
        @DisplayName("Should extract user from authentication principal")
        void shouldExtractUserFromAuthenticationPrincipal() {
            // Given
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(jwtUtils.generateToken(user)).thenReturn("jwt-token");
            
            // When
            AuthResponse response = authService.login(loginRequest);
            
            // Then
            verify(authentication).getPrincipal();
            assertEquals(user.getUsername(), response.username());
            assertEquals(user.getEmail(), response.email());
        }
        
        @Test
        @DisplayName("Should generate token after successful authentication")
        void shouldGenerateTokenAfterSuccessfulAuthentication() {
            // Given
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(jwtUtils.generateToken(user)).thenReturn("generated-jwt-token");
            
            // When
            AuthResponse response = authService.login(loginRequest);
            
            // Then
            assertEquals("generated-jwt-token", response.token());
            verify(jwtUtils).generateToken(user);
        }
        
        @Test
        @DisplayName("Should not generate token when authentication fails")
        void shouldNotGenerateTokenWhenAuthenticationFails() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid"));
            
            // When & Then
            assertThrows(ResponseStatusException.class, () -> authService.login(loginRequest));
            
            verify(jwtUtils, never()).generateToken(any(User.class));
        }
    }
    
    // ======= EDGE CASE TESTS =======
    
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle null return from userRepository.save")
        void shouldHandleNullReturnFromSave() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(null);
            
            // When & Then
            assertThrows(NullPointerException.class, () -> authService.register(registerRequest));
        }
        
        @Test
        @DisplayName("Should handle null return from authentication.getPrincipal")
        void shouldHandleNullPrincipal() {
            // Given
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(null);
            
            // When & Then
            assertThrows(NullPointerException.class, () -> authService.login(loginRequest));
        }
        
        @Test
        @DisplayName("Should handle empty string password encoding")
        void shouldHandleEmptyPasswordEncoding() {
            // Given
            RegisterRequest emptyPassRequest = new RegisterRequest("testuser", "", "test@example.com");
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode("")).thenReturn("encodedEmptyPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtils.generateToken(any(User.class))).thenReturn("jwt-token");
            
            // When
            AuthResponse response = authService.register(emptyPassRequest);
            
            // Then
            assertNotNull(response);
            verify(passwordEncoder).encode("");
        }
    }
}