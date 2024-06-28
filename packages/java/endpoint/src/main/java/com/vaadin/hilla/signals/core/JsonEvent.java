package com.vaadin.hilla.signals.core;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.UUID;

/**
 * Event that contains the related data for Signal state changes in a JSON
 * format.
 */
public class JsonEvent extends StateEvent {

    private final ObjectNode json;

    /**
     * Creates new JsonEvent
     *
     * @param id
     *            the event unique id
     * @param json
     *            the json payload containing the metadata of the event
     */
    public JsonEvent(UUID id, ObjectNode json) {
        super(id);
        this.json = json;
    }

    public ObjectNode getJson() {
        return json;
    }
}
