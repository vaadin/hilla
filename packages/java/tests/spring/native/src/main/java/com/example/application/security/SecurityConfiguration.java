/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.example.application.security;

import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.vaadin.flow.spring.security.RequestUtil;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
class SecurityConfiguration {

    @Bean
    SecurityFilterChain vaadinSecurityFilterChain(HttpSecurity http,
            RequestUtil requestUtil) throws Exception {
        // The chat view is the one that needs a logged in user. The other
        // views are reachable anonymously and the endpoints they call decide
        // for themselves who may call them.
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(requestUtil.applyUrlMapping("/chat"))
                .authenticated());

        http.with(vaadin(), cfg -> cfg.loginView("/login"));
        return http.build();
    }

    @Bean
    UserDetailsManager userDetailsService() {
        var users = List.of(
                User.withUsername("user1").password("{noop}user1").roles("USER")
                        .build(),
                User.withUsername("user2").password("{noop}user2").roles("USER")
                        .build(),
                User.withUsername("admin").password("{noop}admin")
                        .roles("USER", "ADMIN").build());
        return new InMemoryUserDetailsManager(users);
    }

}
