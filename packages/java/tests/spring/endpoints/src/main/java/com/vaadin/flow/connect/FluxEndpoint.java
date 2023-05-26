package com.vaadin.flow.connect;

import java.time.Duration;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import dev.hilla.Nullable;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import reactor.core.publisher.Flux;

@Endpoint
public class FluxEndpoint {

    // DenyAll by default
    @Nonnull
    public Flux<String> denied() {
        return Flux.just("Will never be accessible");
    }

    @PermitAll
    @Nonnull
    public Flux<String> hello(String name, @Nullable String title) {
        return Flux.just(
                "Hello, " + (title != null ? title + " " : "") + name + "!");
    }

    @AnonymousAllowed
    @Nonnull
    public Flux<String> helloAnonymous() {
        return Flux.just("Hello, stranger!");
    }

    @RolesAllowed("ADMIN")
    @Nonnull
    public Flux<String> helloAdmin() {
        return Flux.just("Hello, admin!");
    }

    @AnonymousAllowed
    @Nonnull
    public Flux<String> checkUser() {
        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            auth = null;
        }
        return Flux.just(auth == null ? "Not authenticated" : auth.getName());
    }

    @AnonymousAllowed
    @Nonnull
    public Flux<Integer> countTo(int n) {
        return Flux.range(1, n).delayElements(Duration.ofMillis(200));
    }

    @AnonymousAllowed
    @Nonnull
    public Flux<Integer> countEvenTo(int n) {
        return Flux.range(1, n).delayElements(Duration.ofMillis(200))
                .filter(number -> number % 2 == 0);
    }

    @AnonymousAllowed
    @Nonnull
    public Flux<Integer> countThrowError(int n) {
        return Flux.range(1, n).delayElements(Duration.ofMillis(200))
                .filter(number -> {
                    if (number == 3) {
                        throw new RuntimeException(
                                "Intentionally failing Flux");
                    }
                    return true;
                });
    }

}
