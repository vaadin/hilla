package com.vaadin.flow.spring.fusionsecurityjwt.config;

import java.util.stream.Collectors;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.Base64URL;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfo;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@EnableWebSecurity
@Order(10)
public class SecurityConfiguration extends VaadinStatelessWebSecurityConfig {

    public static String ROLE_USER = "user";
    public static String ROLE_ADMIN = "admin";

    @Autowired
    private UserInfoRepository userInfoRepository;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        // Public access
        http.authorizeRequests().antMatchers("/").permitAll();
        // Admin only access
        http.authorizeRequests().antMatchers("/admin-only/**")
                .hasAnyRole(ROLE_ADMIN);
        super.configure(http);

        http
            .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        setJwtSplitCookieAuthentication(http, "statelessapp", 3600,
                JWSAlgorithm.HS256);
        setLoginView(http, "/login");
        // @formatter:on
    }

    @Bean
    JWKSource<SecurityContext> jwkSource() {
        OctetSequenceKey key = new OctetSequenceKey.Builder(
                Base64URL.from("I72kIcB1UrUQVHVUAzgweE+BLc0bF8mLv9SmrgKsQAk="))
                .algorithm(JWSAlgorithm.HS256).build();
        JWKSet jwkSet = new JWKSet(key);
        return (jwkSelector, context) -> jwkSelector.select(jwkSet);
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
