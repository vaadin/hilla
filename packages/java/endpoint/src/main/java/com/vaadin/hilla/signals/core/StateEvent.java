package com.vaadin.hilla.signals.core;

import java.util.UUID;

public class StateEvent {
    private final UUID id;

    public StateEvent(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
