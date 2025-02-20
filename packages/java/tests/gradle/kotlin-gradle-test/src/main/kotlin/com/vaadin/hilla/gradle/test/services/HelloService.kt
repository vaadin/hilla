package com.vaadin.hilla.gradle.test.services

import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.hilla.BrowserCallable
import org.jspecify.annotations.NonNull

@BrowserCallable
@AnonymousAllowed
class HelloService {

    fun sayHello(name: @NonNull String): @NonNull String =
        if (name.isEmpty()) {
            "Hello stranger"
        } else {
            "Hello $name"
        }
}
