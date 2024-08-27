package com.vaadin.hilla.test.signals.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Public access
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(new AntPathRequestMatcher("/")).permitAll());

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(new AntPathRequestMatcher("/images/*.png"))
                .permitAll());

        // Icons from the line-awesome addon
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        new AntPathRequestMatcher("/line-awesome/**/*.svg"))
                .permitAll());

        super.configure(http);
        setLoginView(http, "/login");
    }

}
