package com.vaadin.flow.connect;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Use default spring login form
        http.formLogin();
        // Vaadin already handles csrf.
        http.csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) {
        // Access to static resources, bypassing Spring security.
        web.ignoring().antMatchers("/VAADIN/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        // Configure users and roles in memory
        auth.inMemoryAuthentication().withUser("user").password("{noop}user")
                .roles("USER").and().withUser("admin").password("{noop}admin")
                .roles("ADMIN", "USER");
    }
}
