package com.vaadin.hilla.signals.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.Arrays;
import java.util.Objects;

/**
 * A utility class for representing state events out of an ObjectNode. This
 * helps to serialize and deserialize state events without getting involved with
 * the string literals for field names and event types.
 *
 * @param <T>
 *            The type of the value of the event.
 */
public class StateEvent<T> {

    /**
     * The field names used in the JSON representation of the state event.
     */
    public static final class Field {
        public static final String ID = "id";
        public static final String TYPE = "type";
        public static final String VALUE = "value";
    }

    /**
     * Possible types of state events.
     */
    public enum EventType {
        SNAPSHOT, SET,
    }

    /**
     * An exception thrown when the event type is null or invalid.
     */
    public static class InvalidEventTypeException extends RuntimeException {
        public InvalidEventTypeException(String message) {
            super(message);
        }

        public InvalidEventTypeException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private final String id;
    private final EventType eventType;
    private final T value;

    /**
     * Creates a new state event using the given parameters.
     *
     * @param id
     *            The unique identifier of the event.
     * @param eventType
     *            The type of the event.
     * @param value
     *            The value of the event.
     */
    public StateEvent(String id, EventType eventType, T value) {
        this.id = id;
        this.eventType = eventType;
        this.value = value;
    }

    /**
     * Creates a new state event using the given JSON representation.
     *
     * @param json
     *            The JSON representation of the event.
     */
    public StateEvent(ObjectNode json) {
        this.id = json.get(Field.ID).asText();
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
                    "Missing event type. Type is required, and should be either of: %s",
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

    /**
     * Returns the JSON representation of the event.
     *
     * @return The JSON representation of the event.
     */
    public ObjectNode toJson() {
        ObjectNode json = mapper.createObjectNode();
        json.put(Field.ID, id);
        json.put(Field.TYPE, eventType.name().toLowerCase());
        json.set(Field.VALUE, getValueAsJson());
        return json;
    }

    /**
     * Returns the unique identifier of the event.
     *
     * @return The unique identifier of the event.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the type of the event.
     *
     * @return The type of the event.
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * Returns the value of the event.
     *
     * @return The value of the event.
     */
    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StateEvent<?> that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
