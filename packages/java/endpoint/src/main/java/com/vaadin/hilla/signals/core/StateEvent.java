package com.vaadin.hilla.signals.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.Arrays;
import java.util.UUID;

public class StateEvent<T> {

    public static final class Field {
        public static final String ID = "id";
        public static final String TYPE = "type";
        public static final String VALUE = "value";
    }

    public enum EventType {
        SNAPSHOT, SET,
    }

    public static class InvalidEventTypeException extends RuntimeException {
        public InvalidEventTypeException(String message) {
            super(message);
        }

        public InvalidEventTypeException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private final UUID id;
    private final EventType eventType;
    private final T value;

    public StateEvent(UUID id, EventType eventType, T value) {
        this.id = id;
        this.eventType = eventType;
        this.value = value;
    }

    public StateEvent(ObjectNode json) {
        this.id = UUID.fromString(json.get(Field.ID).asText());
        this.eventType = extractEventType(json);
        JsonNode value = json.get(Field.VALUE);
        if (value.isTextual()) {
            this.value = (T) value.asText();
        } else if (value.isBoolean()) {
            this.value = (T) Boolean.valueOf(value.asBoolean());
        } else if (value.isNumber()) {
            this.value = (T) Double.valueOf(value.asDouble());
        } else {
            throw new IllegalArgumentException(
                    "Unsupported value type: " + value);
        }
    }

    private EventType extractEventType(JsonNode json) {
        var rawType = json.get(Field.TYPE);
        if (rawType == null) {
            var message = String.format(
                    "Missing event type. Type is required and should be either of: %s",
                    Arrays.toString(EventType.values()));
            throw new InvalidEventTypeException(message);
        }
        try {
            return EventType.valueOf(rawType.asText().toUpperCase());
        } catch (IllegalArgumentException e) {
            var message = String.format(
                    "Invalid event type %s. Type should be either of: %s",
                    rawType.asText(), Arrays.toString(EventType.values()));
            throw new InvalidEventTypeException(message, e);
        }
    }

    private JsonNode getValueAsJson() {
        if (value instanceof String) {
            return new TextNode((String) value);
        } else if (value instanceof Boolean) {
            return BooleanNode.valueOf((Boolean) value);
        } else if (value instanceof Number) {
            return new DoubleNode((Double) value);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported value type: " + value);
        }
    }

    public ObjectNode toJson() {
        ObjectNode json = mapper.createObjectNode();
        json.put(Field.ID, id.toString());
        json.put(Field.TYPE, eventType.name().toLowerCase());
        json.set(Field.VALUE, getValueAsJson());
        return json;
    }

    public UUID getId() {
        return id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public T getValue() {
        return value;
    }
}
