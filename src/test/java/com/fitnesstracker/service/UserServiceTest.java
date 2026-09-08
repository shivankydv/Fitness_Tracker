package com.fitnesstracker.service;

import com.fitnesstracker.domain.User;
import com.fitnesstracker.dto.RegisterRequest;
import com.fitnesstracker.dto.UserResponse;
import com.fitnesstracker.exception.UserAlreadyExistsException;
import com.fitnesstracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_success_returnsUserResponse() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.registerUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test User");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getId()).isEqualTo(1L);

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword");
    }

    @Test
    void registerUser_normalizesEmail_caseInsensitive() {
        RegisterRequest request = new RegisterRequest("Test User", "TEST@EXAMPLE.COM", "password123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        userService.registerUser(request);

        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void registerUser_normalizesEmail_trimsWhitespace() {
        RegisterRequest request = new RegisterRequest("Test User", "  test@example.com  ", "password123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        userService.registerUser(request);

        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void registerUser_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registerUser_passwordIsHashed_notStoredInPlaintext() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "plainPassword");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("bcryptHashedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        userService.registerUser(request);

        verify(passwordEncoder).encode("plainPassword");
    }

    @Test
    void findByEmail_existingEmail_returnsUser() {
        User user = new User("Test User", "test@example.com", "hashedPassword");
        user.setId(1L);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByEmail("TEST@EXAMPLE.COM");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_nonexistentEmail_returnsEmpty() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<User> found = userService.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void emailExists_existingEmail_returnsTrue() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean exists = userService.emailExists("TEST@EXAMPLE.COM");

        assertThat(exists).isTrue();
    }

    @Test
    void emailExists_nonexistentEmail_returnsFalse() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        boolean exists = userService.emailExists("nonexistent@example.com");

        assertThat(exists).isFalse();
    }
}