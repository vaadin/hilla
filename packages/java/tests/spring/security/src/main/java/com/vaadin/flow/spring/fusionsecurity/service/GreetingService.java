package com.vaadin.flow.spring.fusionsecurity.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.Nonnull;

import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Transactional;

@BrowserCallable
@Transactional
public class GreetingService {

    @AnonymousAllowed
    public @NonNull String sayHello() {
        return "Hello from GreetingService";
    }
}
