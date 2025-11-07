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
package com.vaadin.flow.connect;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

/**
 * Simple Vaadin Connect Service definition.
 */
@Endpoint
public class AppEndpoint {

    // DenyAll by default
    public String denied() {
        return "Will never be accessible";
    }

    @PermitAll
    public String hello(String name, @Nullable String title) {
        return "Hello, " + (title != null ? title + " " : "") + name + "!";
    }

    @AnonymousAllowed
    public String echoWithOptional(@Nullable String first,
            @Nullable String second, Optional<String> third,
            Optional<String> fourth) {
        String result = "";
        if (first != null) {
            result += "1. " + first + " ";
        }

        if (second != null) {
            result += "2. " + second + " ";
        }

        if (third.isPresent()) {
            result += "3. " + third.get() + " ";
        }

        if (fourth.isPresent()) {
            result += "4. " + fourth.get();
        }
        return result;
    }

    @AnonymousAllowed
    public String helloAnonymous() {
        return "Hello, stranger!";
    }

    @RolesAllowed("ADMIN")
    public String helloAdmin() {
        return "Hello, admin!";
    }

    @AnonymousAllowed
    public String checkUser() {
        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();
        return auth == null ? null : auth.getName();
    }

    @PermitAll
    public String checkUserFromVaadinRequest() {
        return "Hello, "
                + VaadinRequest.getCurrent().getUserPrincipal().getName() + "!";
    }

    @AnonymousAllowed
    public ObjectWithNullValues getObjectWithNullValues() {
        return new ObjectWithNullValues();
    }

}
