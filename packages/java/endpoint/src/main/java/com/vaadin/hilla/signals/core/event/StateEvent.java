package com.vaadin.hilla.signals.core.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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
        public static final String VALIDATION_ERROR = "validationError";
    }

    /**
     * Possible types of state events.
     */
    public enum EventType {
        SNAPSHOT, SET, REPLACE, INCREMENT;

        public static EventType of(String type) {
            return valueOf(type.toUpperCase());
        }

        public static Optional<EventType> find(String type) {
            return Arrays.stream(values())
                    .filter(e -> e.name().equalsIgnoreCase(type)).findFirst();
        }
    }

    static ObjectMapper MAPPER;

    private final String id;
    private final EventType eventType;
    private final T value;
    private final T expected;
    private Boolean accepted;
    private String validationError;

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
        this.value = convertValue(extractValue(json, true), valueType);

        JsonNode expected = json.get(Field.EXPECTED);
        this.expected = convertValue(expected, valueType);

    }

    /**
     * Sets the object mapper to be used for serialization and deserialization
     * of state events in Signal library.
     *
     * @param mapper
     *            The object mapper to be used for serialization and
     *            deserialization of state events.
     */
    public static void setMapper(ObjectMapper mapper) {
        MAPPER = mapper;
    }

    public static <X> X convertValue(JsonNode rawValue, Class<X> valueType) {
        if (rawValue == null) {
            return null;
        }
        return MAPPER.convertValue(rawValue, valueType);
    }

    public static String extractId(JsonNode json) {
        var id = json.get(Field.ID);
        if (id == null) {
            throw new MissingFieldException(Field.ID);
        }
        return id.asText();
    }

    public static JsonNode extractValue(JsonNode json, boolean required) {
        var value = json.get(Field.VALUE);
        if (value == null) {
            if (required) {
                throw new MissingFieldException(Field.VALUE);
            }
            return null;
        }
        return value;
    }

    public static JsonNode extractExpected(JsonNode json, boolean required) {
        var expected = json.get(Field.EXPECTED);
        if (expected == null) {
            if (required) {
                throw new MissingFieldException(Field.EXPECTED);
            }
            return null;
        }
        return expected;
    }

    public static String extractRawEventType(JsonNode json) {
        var rawType = json.get(Field.TYPE);
        if (rawType == null) {
            var message = String.format(
                    "Missing event type. Type is required, and should be one of: %s",
                    Arrays.toString(EventType.values()));
            throw new MissingFieldException(message);
        }
        return rawType.asText();
    }

    public static EventType extractEventType(JsonNode json) {
        var rawType = extractRawEventType(json);
        try {
            return EventType.of(rawType);
        } catch (IllegalArgumentException e) {
            var message = String.format(
                    "Invalid event type %s. Type should be one of: %s", rawType,
                    Arrays.toString(EventType.values()));
            throw new InvalidEventTypeException(message, e);
        }
    }

    /**
     * Checks if the given JSON object represents a SET state event.
     *
     * @param event
     *            The JSON object to check.
     * @return <code>true</code> if the given JSON object represents a SET state
     *         event, <code>false</code> otherwise.
     * @throws MissingFieldException
     *             If the event does not contain the TYPE field.
     * @throws InvalidEventTypeException
     *             If the event contains an invalid event type.
     */
    public static boolean isSetEvent(ObjectNode event) {
        var rawEventType = StateEvent.extractRawEventType(event);
        if (rawEventType == null) {
            throw new MissingFieldException(Field.TYPE);
        }
        var eventType = StateEvent.EventType.find(rawEventType)
                .orElseThrow(() -> new InvalidEventTypeException(rawEventType));
        return eventType == EventType.SET;
    }

    /**
     * Checks if the given JSON object represents a REPLACE state event.
     *
     * @param event
     *            The JSON object to check.
     * @return <code>true</code> if the given JSON object represents a REPLACE
     *         state event, <code>false</code> otherwise.
     * @throws MissingFieldException
     *             If the event does not contain the TYPE field.
     * @throws InvalidEventTypeException
     *             If the event contains an invalid event type.
     */
    public static boolean isReplaceEvent(ObjectNode event) {
        var rawEventType = StateEvent.extractRawEventType(event);
        if (rawEventType == null) {
            throw new MissingFieldException(Field.TYPE);
        }
        var eventType = StateEvent.EventType.find(rawEventType)
                .orElseThrow(() -> new InvalidEventTypeException(rawEventType));
        return eventType == EventType.REPLACE;
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
        if (accepted != null) {
            json.put(Field.ACCEPTED, accepted);
        }
        if (validationError != null) {
            json.put(StateEvent.Field.VALIDATION_ERROR, validationError);
        }
        return json;
    }

    public static boolean isAccepted(ObjectNode event) {
        return event.has(Field.ACCEPTED)
                && event.get(Field.ACCEPTED).asBoolean();
    }

    public static boolean isRejected(ObjectNode event) {
        return event.has(Field.ACCEPTED)
                && !event.get(Field.ACCEPTED).asBoolean()
                && event.has(Field.VALIDATION_ERROR)
                && event.get(Field.VALIDATION_ERROR).asText() != null;
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

    /**
     * Returns whether the event was accepted or not.
     *
     * @return whether the event was accepted or not.
     */
    public Boolean getAccepted() {
        return accepted;
    }

    /**
     * Sets whether the event was accepted or not.
     *
     * @param accepted
     *            whether the event was accepted or not.
     */
    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
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

    public String getValidationError() {
        return validationError;
    }

    public void setValidationError(String validationError) {
        this.validationError = validationError;
    }
}
