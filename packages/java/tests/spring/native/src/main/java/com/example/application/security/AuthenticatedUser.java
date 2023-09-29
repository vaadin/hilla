package com.example.application.security;

import com.example.application.data.Role;
import com.example.application.data.entity.User;
import com.vaadin.flow.spring.security.AuthenticationContext;

import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AuthenticatedUser {

    private final UserDetailsManager userDetailsManager;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserDetailsManager userDetailsManager) {
        this.userDetailsManager = userDetailsManager;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<User> get() {
        return authenticationContext.getAuthenticatedUser(org.springframework.security.core.userdetails.User.class)
                .map(userDetails -> userDetailsManager.loadUserByUsername(userDetails.getUsername()))
                .map(userDetails -> new User(userDetails.getUsername(), Role.USER));
    }

    public void logout() {
        authenticationContext.logout();
    }

}
