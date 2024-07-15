package com.vaadin.hilla.signals.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.WeakHashMap;

@Component
public class SignalsRegistry {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SignalsRegistry.class);
    private final WeakHashMap<UUID, SignalQueue<?>> signals = new WeakHashMap<>();

    public synchronized void register(SignalQueue<?> signal) {
        signals.put(signal.getId(), signal);
        LOGGER.debug("Registered signal: {}", signal.getId());
    }

    public synchronized SignalQueue<?> get(UUID uuid) {
        return signals.get(uuid);
    }

    public synchronized void remove(UUID uuid) {
        signals.remove(uuid);
        LOGGER.debug("Removed signal: {}", uuid);
    }

    public synchronized void clear() {
        signals.clear();
        LOGGER.debug("Cleared all signal instances");
    }

    public synchronized boolean contains(UUID uuid) {
        return signals.containsKey(uuid);
    }

    public synchronized boolean isEmpty() {
        return signals.isEmpty();
    }

}
