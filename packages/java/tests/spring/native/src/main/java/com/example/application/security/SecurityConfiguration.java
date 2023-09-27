package com.example.application.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.stream.Stream;

@EnableWebSecurity
@Configuration
class SecurityConfiguration extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests()
            .requestMatchers(
                new AntPathRequestMatcher(applyUrlMapping("/grid")))
            .permitAll();

        http.authorizeHttpRequests(
            authorize -> authorize.requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll());
        // Icons from the line-awesome addon
        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers(new AntPathRequestMatcher("/line-awesome/**/*.svg")).permitAll());

        super.configure(http);
        setLoginView(http, "login");
    }

    @Bean
    UserDetailsManager userDetailsService() {
        var list = Stream.of("user1", "user2").map(u -> User.withUsername(u)
                .password("{noop}" + u).roles("USER").build()).toList();
        return new InMemoryUserDetailsManager(list);
    }

}
