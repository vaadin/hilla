package com.vaadin.hilla.gradle.plugin.test.endpoint;

import org.jspecify.annotations.NonNull;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

@Endpoint
@AnonymousAllowed
public class HelloReactEndpoint {

    @NonNull
    public String sayHello(@NonNull String name) {
        if (name.isEmpty()) {
            return "Hello stranger";
        } else {
            return "Hello " + name;
        }
    }
}
