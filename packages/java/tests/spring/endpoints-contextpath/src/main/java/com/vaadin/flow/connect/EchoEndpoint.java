package com.vaadin.flow.connect;

import dev.hilla.Endpoint;
import reactor.core.publisher.Flux;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@Endpoint
public class EchoEndpoint {

    @AnonymousAllowed
    public String echo(String input) {
        return input;
    }

    @AnonymousAllowed
    public Flux<String> fluxEcho(String input) {
        return Flux.just(input);
    }

}
