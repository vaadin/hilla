package com.vaadin.hilla.gradle.test.services

import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.hilla.BrowserCallable

@BrowserCallable
@AnonymousAllowed
class HelloService {

    fun sayHello(name: String): String =
        if (name.isBlank()) {
            "Hello stranger"
        } else {
            "Hello $name"
        }
}
