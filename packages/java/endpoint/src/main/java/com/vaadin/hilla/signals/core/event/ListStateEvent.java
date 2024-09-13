package com.vaadin.hilla.signals.core.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.exception.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.exception.MissingFieldException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ListStateEvent<T> {

    public interface ListEntry<T> {
        UUID getId();
        UUID getPrev();
        void setPrev(UUID prev);
        UUID getNext();
        void setNext(UUID next);
        T getValue();
        void setValue(T value);
    }

    @FunctionalInterface
    public interface ListEntryFactory<T> {
        ListEntry<T> create(UUID id, UUID prev, UUID next, T value);
    }

    /**
     * The field names used in the JSON representation of the state event.
     */
    public static final class Field {
        public static final String NEXT = "next";
        public static final String PREV = "prev";
        public static final String POSITION = "position";
        public static final String ENTRIES = "entries";
    }

    /**
     * Possible types of state events.
     */
    public enum InsertPosition {
        FIRST, LAST, BEFORE, AFTER;

        public static InsertPosition of(String direction) {
            return InsertPosition.valueOf(direction.toUpperCase());
        }
    }

    /**
     * Possible types of state events.
     */
    public enum EventType {
        SNAPSHOT, REJECT, INSERT, REMOVE;

        public static EventType of(String type) {
            return EventType.valueOf(type.toUpperCase());
        }
    }

    private final String id;
    private final EventType eventType;
    private final Collection<ListEntry<T>> entries;
    private final InsertPosition insertPosition;


    /**
     * Creates a new state event using the given JSON representation.
     *
     * @param json
     *            The JSON representation of the event.
     */
    public ListStateEvent(ObjectNode json, Class<T> valueType, ListEntryFactory<T> entryFactory) {
        this.id = StateEvent.extractId(json);
        this.eventType = extractEventType(json);
        this.entries = extractEntries(json, valueType, entryFactory);
        this.insertPosition = this.eventType == EventType.INSERT ? extractDirection(json) : null;
    }

    private static EventType extractEventType(JsonNode json) {
        var rawType = json.get(StateEvent.Field.TYPE);
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

    private static <X> List<ListEntry<X>> extractEntries(JsonNode json, Class<X> valueType, ListEntryFactory<X> entryFactory) {
        var rawEntries = json.get(Field.ENTRIES);
        if (rawEntries == null) {
            throw new MissingFieldException(Field.ENTRIES);
        }
        List<ListEntry<X>> entries = new ArrayList<>();
        for (JsonNode rawEntry : rawEntries) {
            var id = extractEntryId(rawEntry);
            var next = extractUUIDOrNull(rawEntry, Field.NEXT);
            var prev = extractUUIDOrNull(rawEntry, Field.PREV);
            var value = StateEvent.convertValue(StateEvent.extractValue(rawEntry), valueType);
            entries.add(entryFactory.create(id, next, prev, value));
        }
        return entries;
    }

    private static UUID extractEntryId(JsonNode rawEntry) {
        var id = rawEntry.get(StateEvent.Field.ID);
        if (id == null) {
            return UUID.randomUUID();
        }
        return UUID.fromString(id.asText());
    }

    private static UUID extractUUIDOrNull(JsonNode json, String fieldName) {
        var rawId = json.get(fieldName);
        return rawId == null ? null : UUID.fromString(rawId.asText());
    }

    private static InsertPosition extractDirection(JsonNode json) {
        var rawDirection = json.get(Field.POSITION);
        if (rawDirection == null) {
            var message = String.format(
                "Missing event direction. Direction is required, and should be one of: %s",
                Arrays.toString(InsertPosition.values()));
            throw new InvalidEventTypeException(message);
        }
        try {
            return InsertPosition.valueOf(rawDirection.asText());
        } catch (IllegalArgumentException e) {
            var message = String.format(
                "Invalid event direction %s. Direction should be one of: %s",
                rawDirection.asText(), Arrays.toString(InsertPosition.values()));
            throw new InvalidEventTypeException(message, e);
        }
    }

    public static <X> ObjectNode toJson(String id, StateEvent.EventType eventType, Collection<ListEntry<X>> entries) {
        ObjectNode snapshotData = StateEvent.MAPPER.createObjectNode();
        snapshotData.put(StateEvent.Field.ID, id);
        snapshotData.put(StateEvent.Field.TYPE, eventType.name().toLowerCase());
        if (entries != null) {
            ArrayNode snapshotEntries = StateEvent.MAPPER.createArrayNode();
            entries.forEach(entry -> {
                ObjectNode entryNode = snapshotEntries.addObject();
                entryNode.put(StateEvent.Field.ID, entry.getId().toString());
                entryNode.put(Field.NEXT, toStringOrNull(entry.getNext()));
                entryNode.put(Field.PREV, toStringOrNull(entry.getPrev()));
                entryNode.set(StateEvent.Field.VALUE, StateEvent.MAPPER.valueToTree(entry.getValue()));
            });
            snapshotData.set(Field.ENTRIES, snapshotEntries);
        }
        return snapshotData;
    }

    private static String toStringOrNull(UUID uuid) {
        return Objects.toString(uuid, null);
    }

    public String getId() {
        return id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Collection<ListEntry<T>> getEntries() {
        return entries;
    }

    public InsertPosition getDirection() {
        return insertPosition;
    }
}
