package com.vaadin.hilla.test.signals.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.signals.NumberSignal;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@BrowserCallable
public class SecureNumberSignalService {

    private final NumberSignal userCounter = new NumberSignal(20d);
    private final NumberSignal adminCounter = new NumberSignal(30d);

    @PermitAll
    public NumberSignal userCounter() {
        return userCounter;
    }

    @RolesAllowed("ADMIN")
    public NumberSignal adminCounter() {
        return adminCounter;
    }

    @AnonymousAllowed
    public Long fetchUserCounterValue() {
        return userCounter.value().longValue();
    }

    @AnonymousAllowed
    public Long fetchAdminCounterValue() {
        return adminCounter.value().longValue();
    }

    @AnonymousAllowed
    public void resetCounters() {
        userCounter.value(20d);
        adminCounter.value(30d);
    }
}
