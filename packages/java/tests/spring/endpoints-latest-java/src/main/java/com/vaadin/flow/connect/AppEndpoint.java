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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

/**
 * Simple Vaadin Connect Service definition.
 */
@Endpoint
@AnonymousAllowed
public class AppEndpoint {

    @NonNull
    public String hello(@Nullable String name) {
        // This intentionally uses Java 17 syntax to ensure it works
        switch (name) {
        case "John":
            return "Hi John!";
        case "Jeff":
            return "Hello Jeff";
        default:
            return """
                    Hello stranger!
                    """;
        }
    }

}
