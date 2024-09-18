package com.vaadin.hilla.signals.core.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.exception.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.exception.MissingFieldException;

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
        public static final String EXPECTED = "expected";
        public static final String ACCEPTED = "accepted";
    }

    /**
     * Possible types of state events.
     */
    public enum EventType {
        SNAPSHOT, SET, REPLACE, INCREMENT;

        public static EventType of(String type) {
            return EventType.valueOf(type.toUpperCase());
        }
    }

    static final ObjectMapper MAPPER = new ObjectMapper();

    private final String id;
    private final EventType eventType;
    private final T value;
    private final T expected;

    /**
     * Creates a new state event using the given parameters.
     *
     * @param id
     *            The unique identifier of the event.
     * @param eventType
     *            The type of the event.
     * @param value
     *            The value of the event.
     * @param expected
     *            The expected value of the event before the change is applied.
     */
    public StateEvent(String id, EventType eventType, T value, T expected) {
        this.id = id;
        this.eventType = eventType;
        this.value = value;
        this.expected = expected;
    }

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
        this(id, eventType, value, null);
    }

    /**
     * Creates a new state event using the given JSON representation.
     *
     * @param json
     *            The JSON representation of the event.
     */
    public StateEvent(ObjectNode json, Class<T> valueType) {
        this.id = extractId(json);
        this.eventType = extractEventType(json);
        this.value = convertValue(extractValue(json), valueType);

        JsonNode expected = json.get(Field.EXPECTED);
        this.expected = convertValue(expected, valueType);
    }

    static <X> X convertValue(JsonNode rawValue, Class<X> valueType) {
        if (rawValue == null) {
            return null;
        }
        return MAPPER.convertValue(rawValue, valueType);
    }

    static String extractId(JsonNode json) {
        var id = json.get(Field.ID);
        if (id == null) {
            throw new MissingFieldException(Field.ID);
        }
        return id.asText();
    }

    static JsonNode extractValue(JsonNode json) {
        var value = json.get(Field.VALUE);
        if (value == null) {
            throw new MissingFieldException(Field.VALUE);
        }
        return value;
    }

    static EventType extractEventType(JsonNode json) {
        var rawType = json.get(Field.TYPE);
        if (rawType == null) {
            var message = String.format(
                    "Missing event type. Type is required, and should be one of: %s",
                    Arrays.toString(EventType.values()));
            throw new InvalidEventTypeException(message);
        }
        try {
            return EventType.of(rawType.asText());
        } catch (IllegalArgumentException e) {
            var message = String.format(
                    "Invalid event type %s. Type should be one of: %s",
                    rawType.asText(), Arrays.toString(EventType.values()));
            throw new InvalidEventTypeException(message, e);
        }
    }

    public static ObjectNode setAccepted(ObjectNode event, boolean accepted) {
        return event.put(Field.ACCEPTED, accepted);
    }

    public static boolean isAccepted(ObjectNode event) {
        return event.has(Field.ACCEPTED)
                && event.get(Field.ACCEPTED).asBoolean();
    }

    /**
     * Returns the JSON representation of the event.
     *
     * @return The JSON representation of the event.
     */
    public ObjectNode toJson() {
        ObjectNode json = MAPPER.createObjectNode();
        json.put(Field.ID, id);
        json.put(Field.TYPE, eventType.name().toLowerCase());
        json.set(Field.VALUE, valueAsJsonNode(getValue()));
        if (getExpected() != null) {
            json.set(Field.EXPECTED, valueAsJsonNode(getExpected()));
        }
        return json;
    }

    private static JsonNode valueAsJsonNode(Object value) {
        return MAPPER.valueToTree(value);
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

    /**
     * Returns the expected value of the event if exists.
     *
     * @return The expected value of the event if exists.
     */
    public T getExpected() {
        return expected;
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
