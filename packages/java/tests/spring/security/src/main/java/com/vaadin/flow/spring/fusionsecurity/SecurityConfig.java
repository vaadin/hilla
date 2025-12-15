/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.spring.fusionsecurity;

import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

import java.util.Base64;
import java.util.stream.Collectors;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.vaadin.flow.spring.fusionsecurity.data.UserInfo;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfoRepository;
import com.vaadin.flow.spring.security.RequestUtil;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.stateless.VaadinStatelessSecurityConfigurer;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
@Profile("default")
public class SecurityConfig {

    public static String ROLE_USER = "user";
    public static String ROLE_ADMIN = "admin";

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Value("${springSecurityTestApp.security.stateless:false}")
    private boolean stateless;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            RequestUtil requestUtil) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth
                    // Public access
                    .requestMatchers("/public/**").permitAll()
                    .requestMatchers(requestUtil.applyUrlMapping("/"),
                            requestUtil.applyUrlMapping("/form"),
                            requestUtil.applyUrlMapping("/proxied-service"))
                    .permitAll()
                    // Authenticated access
                    .requestMatchers(requestUtil.applyUrlMapping("/private"),
                            "/all-logged-in/**")
                    .authenticated()
                    // Admin only access
                    .requestMatchers(requestUtil.applyUrlMapping("/admin"),
                            "/admin-only/**")
                    .hasAnyRole(ROLE_ADMIN).requestMatchers("/error/**")
                    .permitAll();
        });

        if (stateless) {
            // Disable creating and using sessions in Spring Security
            http.sessionManagement((sessionManagement) -> {
                sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            });
        }

        http.with(vaadin(), cfg -> cfg.loginView("/login",
                requestUtil.applyUrlMapping("/")));
        http.logout(
                cfg -> cfg.logoutUrl(requestUtil.applyUrlMapping("/logout")));

        if (stateless) {
            VaadinStatelessSecurityConfigurer.apply(http, cfg -> cfg
                    .withSecretKey(keyCfg -> keyCfg.secretKey(new SecretKeySpec(
                            Base64.getUrlDecoder().decode(
                                    "I72kIcB1UrUQVHVUAzgweE-BLc0bF8mLv9SmrgKsQAk"),
                            JwsAlgorithms.HS256)))
                    .issuer("statelessapp"));
        }
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager() {
            @Override
            public UserDetails loadUserByUsername(String username)
                    throws UsernameNotFoundException {
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
            }
        };
    }
}
