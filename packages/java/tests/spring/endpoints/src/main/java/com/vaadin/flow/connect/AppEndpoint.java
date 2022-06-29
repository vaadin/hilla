package com.vaadin.flow.connect;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.server.VaadinRequest;
import dev.hilla.Endpoint;
import com.vaadin.flow.server.auth.AnonymousAllowed;

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
