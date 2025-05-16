package com.vaadin.flow.connect;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
@Profile("legacy-vaadin-web-security")
public class LegacySecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf((h) -> h
                .ignoringRequestMatchers(new AntPathRequestMatcher("/login")));

        http.authorizeHttpRequests(
                (h) -> h.requestMatchers(new AntPathRequestMatcher("/flux"))
                        .permitAll());
        http.authorizeHttpRequests((h) -> h
                .requestMatchers(new AntPathRequestMatcher("/type-script"))
                .permitAll());
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
