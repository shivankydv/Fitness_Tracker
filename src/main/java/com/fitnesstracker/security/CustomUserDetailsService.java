package com.fitnesstracker.security;

import com.fitnesstracker.domain.User;
import com.fitnesstracker.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedEmail = username.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));

        // Assign USER role to all enabled users
        // Design is extensible - ADMIN role can be added later by adding a role field to User entity
        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return CustomUserDetails.create(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                true,
                user
        );
    }
}