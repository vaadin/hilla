package com.vaadin.flow.connect;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean(name = "MySecurityFilterChainBean")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Use default spring login form
        http.formLogin();
        // Vaadin already handles csrf.
        http.csrf().disable();
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // Configure users and roles in memory
        UserDetails user = User.withUsername("user").password("{noop}user")
                .roles("USER").build();
        UserDetails admin = User.withUsername("admin").password("{noop}admin")
                .roles("ADMIN", "USER").build();
        return new InMemoryUserDetailsManager(user, admin);
    }
}
