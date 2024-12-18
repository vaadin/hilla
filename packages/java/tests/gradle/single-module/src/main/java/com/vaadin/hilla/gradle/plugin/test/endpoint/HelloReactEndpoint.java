package com.vaadin.hilla.gradle.plugin.test.endpoint;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;

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
