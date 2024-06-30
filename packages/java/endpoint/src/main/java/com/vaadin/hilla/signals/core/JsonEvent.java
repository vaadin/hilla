package com.vaadin.hilla.signals.core;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.UUID;

public class JsonEvent extends StateEvent {

    private final ObjectNode json;

    public JsonEvent(UUID id, ObjectNode json) {
        super(id);
        this.json = json;
    }

    public ObjectNode getJson() {
        return json;
    }

}
