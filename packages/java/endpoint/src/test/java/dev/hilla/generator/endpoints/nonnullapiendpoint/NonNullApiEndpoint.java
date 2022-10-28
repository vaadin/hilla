package dev.hilla.generator.endpoints.nonnullapiendpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;
import reactor.core.publisher.Flux;

@Endpoint
@AnonymousAllowed
public class NonNullApiEndpoint {

    public String hello(String hello) {
        return "Hello " + hello;
    }

    @Nullable
    public String helloNullable(@Nullable String hello) {
        return null;
    }

    public Flux<String> helloFlux(String hello) {
        return Flux.just("Hello");
    }

    public Map<List<String>, Set<Map<Integer, String>>> helloNestedTypes(
            Map<Integer, List<String>> param) {
        return new HashMap<>();
    }

}
