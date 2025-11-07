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
package com.vaadin.hilla.test.signals.security;

import com.vaadin.hilla.test.signals.data.User;
import com.vaadin.hilla.test.signals.data.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AuthenticatedUser {

    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext,
            UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<User> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> userRepository
                        .findByUsername(userDetails.getUsername()));
    }

    public void logout() {
        authenticationContext.logout();
    }

}
