package com.company.productmanagement.service;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.company.productmanagement.repository.UserRepository;

/**
 * Implementation of UserDetailsService for loading user-specific data
 * Used by Spring Security for authentication
 * 
 * @author Shruti Sharma
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * Loads user by username for authentication
     * 
     * @param username the username to search for
     * @return UserDetails containing user information
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"a-2"));
    }

}