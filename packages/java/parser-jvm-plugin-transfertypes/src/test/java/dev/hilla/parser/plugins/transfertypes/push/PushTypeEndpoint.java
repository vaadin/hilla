package dev.hilla.parser.plugins.transfertypes.push;

import reactor.core.publisher.Flux;

@Endpoint
public class PushTypeEndpoint {

    public Flux<String> getMessageFlux() {
        return Flux.just();
    }

}
