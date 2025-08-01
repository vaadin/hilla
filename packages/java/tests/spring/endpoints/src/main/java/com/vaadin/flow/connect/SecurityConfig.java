package com.vaadin.flow.connect;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

@EnableWebSecurity
@Configuration
@Profile("default")
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain vaadinSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http.csrf((h) -> h.ignoringRequestMatchers("/login"));

        http.authorizeHttpRequests(
                (h) -> h.requestMatchers("/flux").permitAll());
        http.authorizeHttpRequests(
                (h) -> h.requestMatchers("/type-script").permitAll());
        http.with(vaadin(), cfg -> cfg.loginView("/login"));
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
