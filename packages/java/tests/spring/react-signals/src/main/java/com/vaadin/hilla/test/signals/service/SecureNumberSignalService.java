package com.vaadin.hilla.test.signals.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.signals.NumberSignal;
import com.vaadin.hilla.signals.core.StateEvent;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.util.UUID;

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
        return userCounter.getValue().longValue();
    }

    @AnonymousAllowed
    public Long fetchAdminCounterValue() {
        return adminCounter.getValue().longValue();
    }

    @AnonymousAllowed
    public void resetCounters() {
        var setValueTo20 = new StateEvent<>(UUID.randomUUID().toString(),
                StateEvent.EventType.SET, 20d);
        userCounter.submit(setValueTo20.toJson());

        var setValueTo30 = new StateEvent<>(UUID.randomUUID().toString(),
                StateEvent.EventType.SET, 30d);
        adminCounter.submit(setValueTo30.toJson());
    }
}
