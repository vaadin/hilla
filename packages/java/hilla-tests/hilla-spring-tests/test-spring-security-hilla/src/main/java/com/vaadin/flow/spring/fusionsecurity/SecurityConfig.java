package com.vaadin.flow.spring.fusionsecurity;

import java.util.stream.Collectors;

import com.vaadin.flow.spring.fusionsecurity.data.UserInfo;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfoRepository;
import com.vaadin.flow.spring.security.VaadinWebSecurityConfigurerAdapter;
import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurityConfigurerAdapter {

    public static String ROLE_USER = "user";
    public static String ROLE_ADMIN = "admin";

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Public access
        http.authorizeRequests().antMatchers("/").permitAll();
        http.authorizeRequests().antMatchers("/form").permitAll();
        // Admin only access
        http.authorizeRequests().antMatchers("/admin-only/**")
                .hasAnyRole(ROLE_ADMIN);

        super.configure(http);

        setLoginView(http, "/login");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.ignoring().antMatchers("/public/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.userDetailsService(username -> {
            UserInfo userInfo = userInfoRepository.findByUsername(username);
            if (userInfo == null) {
                throw new UsernameNotFoundException(
                        "No user present with username: " + username);
            } else {
                return new User(userInfo.getUsername(),
                        userInfo.getEncodedPassword(),
                        userInfo.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority(
                                        "ROLE_" + role))
                                .collect(Collectors.toList()));
            }
        });
    }
}
