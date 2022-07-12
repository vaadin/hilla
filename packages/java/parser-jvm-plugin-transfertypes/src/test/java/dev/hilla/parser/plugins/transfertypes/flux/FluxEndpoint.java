package dev.hilla.parser.plugins.transfertypes.flux;

import reactor.core.publisher.Flux;

@Endpoint
public class FluxEndpoint {

    public Flux<String> getMessages() {
        return Flux.just();
    }

}
