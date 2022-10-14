package com.vaadin.flow.connect;

import javax.annotation.Nullable;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;
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
