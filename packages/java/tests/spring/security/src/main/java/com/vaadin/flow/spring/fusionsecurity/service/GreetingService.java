package com.vaadin.flow.spring.fusionsecurity.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import dev.hilla.Nonnull;
import org.springframework.transaction.annotation.Transactional;

@BrowserCallable
@Transactional
public class GreetingService {

    @AnonymousAllowed
    public @Nonnull String sayHello() {
        return "Hello from GreetingService";
    }
}
