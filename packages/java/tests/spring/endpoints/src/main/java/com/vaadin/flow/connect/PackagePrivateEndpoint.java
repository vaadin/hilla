package com.vaadin.flow.connect;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

import org.jspecify.annotations.Nullable;
import jakarta.annotation.security.PermitAll;

/**
 * Simple Vaadin Connect Service definition.
 */
@Endpoint
class PackagePrivateEndpoint {

    @PermitAll
    public String hello(String name, @Nullable String title) {
        return "Hello, " + (title != null ? title + " " : "") + name + "!";
    }

    @AnonymousAllowed
    public String helloAnonymous() {
        return "Hello from package private endpoint!";
    }

}
