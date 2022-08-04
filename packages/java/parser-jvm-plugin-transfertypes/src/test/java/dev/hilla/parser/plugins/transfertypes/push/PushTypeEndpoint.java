package dev.hilla.parser.plugins.transfertypes.push;

import dev.hilla.EndpointSubscription;
import reactor.core.publisher.Flux;

@Endpoint
public class PushTypeEndpoint {

    public Flux<String> getMessageFlux() {
        return Flux.just();
    }

    public EndpointSubscription<String> getSubscription() {
        return new EndpointSubscription<>();
    }

}
