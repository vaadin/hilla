package com.example.application.security;

import com.vaadin.flow.spring.security.RequestUtil;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.stream.Stream;

import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
class SecurityConfiguration {

    @Bean
    SecurityFilterChain vaadinSecurityFilterChain(HttpSecurity http,
            RequestUtil requestUtil) throws Exception {

        http.authorizeHttpRequests(
                authorize -> authorize
                        .requestMatchers(new AntPathRequestMatcher(
                                requestUtil.applyUrlMapping("/grid")))
                        .permitAll());

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(new AntPathRequestMatcher("/images/*.png"))
                .permitAll());
        // Icons from the line-awesome addon
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        new AntPathRequestMatcher("/line-awesome/**/*.svg"))
                .permitAll());

        http.with(vaadin(), cfg -> cfg.loginView("login"));
        return http.build();
    }

    @Bean
    UserDetailsManager userDetailsService() {
        var list = Stream.of("user1", "user2").map(u -> User.withUsername(u)
                .password("{noop}" + u).roles("USER").build()).toList();
        return new InMemoryUserDetailsManager(list);
    }

}
