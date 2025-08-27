package com.vaadin.hilla.test.signals.service;

import com.vaadin.signals.NumberSignal;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

import org.springframework.context.annotation.DependsOn;

@AnonymousAllowed
@BrowserCallable
@DependsOn("signalsConfiguration")
public class NumberSignalService {
    private final NumberSignal counter = new NumberSignal();
    private final NumberSignal sharedValue = new NumberSignal(0.5);

    private final NumberSignal high = new NumberSignal(100.0);
    private final NumberSignal low = new NumberSignal(-100.0);

    public NumberSignal counter() {
        return counter;
    }

    public NumberSignal sharedValue() {
        return sharedValue;
    }

    @NotNull
    public Double fetchSharedValue() {
        return sharedValue.value();
    }

    @NotNull
    public Long fetchCounterValue() {
        return Optional.ofNullable(counter.value()).map(Double::longValue)
                .orElse(null);
    }

    public NumberSignal numberSignal(boolean isHigh) {
        return isHigh ? high : low;
    }
}
