package com.vaadin.flow.connect;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;
import com.vaadin.hilla.Nullable;

/**
 * Simple Vaadin Connect Service definition.
 */
@Endpoint
@AnonymousAllowed
public class AppEndpoint {

    @Nonnull
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
