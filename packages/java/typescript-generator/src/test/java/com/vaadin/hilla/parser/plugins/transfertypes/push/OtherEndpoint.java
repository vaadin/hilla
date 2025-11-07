package com.vaadin.hilla.parser.plugins.transfertypes.push;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import reactor.core.publisher.Flux;

// The purpose of this additional endpoint is to test multiple endpoints
@Endpoint
public class OtherEndpoint {

    public Flux<String> getMessageFlux(int count) {
        return Flux.just("Hello", "World").repeat(count);
    }

    public String toUpperCase(String message) {
        return message.toUpperCase();
    }

}
