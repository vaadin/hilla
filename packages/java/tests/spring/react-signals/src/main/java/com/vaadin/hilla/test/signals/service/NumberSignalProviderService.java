package com.vaadin.hilla.test.signals.service;

import com.vaadin.hilla.signals.NumberSignal;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;

@AnonymousAllowed
@BrowserCallable
public class NumberSignalProviderService {
    private final NumberSignal counter = new NumberSignal();
    private final NumberSignal sharedValue = new NumberSignal(0.5);

    public NumberSignal counter() {
        return counter;
    }

    public NumberSignal sharedValue() {
        return sharedValue;
    }
}
