package com.fitnesstracker.security;

import com.fitnesstracker.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final User user;

    public CustomUserDetails(Long id, String username, String password,
                             Collection<? extends GrantedAuthority> authorities, boolean enabled, User user) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities != null ? authorities : Collections.emptyList();
        this.enabled = enabled;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public static CustomUserDetails create(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, boolean enabled, User user) {
        return new CustomUserDetails(id, username, password, authorities, enabled, user);
    }
}