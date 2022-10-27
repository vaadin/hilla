package com.vaadin.flow.connect;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .ignoringRequestMatchers(new AntPathRequestMatcher("/login"));
        super.configure(http);
        setLoginView(http, "/login");
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
