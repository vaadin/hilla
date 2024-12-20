package com.vaadin.flow.connect;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
