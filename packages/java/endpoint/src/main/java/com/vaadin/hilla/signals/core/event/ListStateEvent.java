package com.vaadin.hilla.signals.core.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.exception.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.exception.MissingFieldException;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ListStateEvent<T> {

    public interface ListEntry<T> {
        UUID id();

        @Nullable
        UUID previous();

        @Nullable
        UUID next();

        T value();
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
    private Boolean accepted;
    // Only used for snapshot events:
    private final Collection<ListEntry<T>> entries;
    // Only used for insert events:
    private final InsertPosition insertPosition;

    public ListStateEvent(String id, EventType eventType,
            Collection<ListEntry<T>> entries, InsertPosition insertPosition) {
        this.id = id;
        this.eventType = eventType;
        this.entries = entries;
        this.insertPosition = insertPosition;
    }

    public ListStateEvent(String id, EventType eventType,
            Collection<ListEntry<T>> entries) {
        this(id, eventType, entries, null);
    }

    /**
     * Creates a new state event using the given JSON representation.
     *
     * @param json
     *            The JSON representation of the event.
     */
    public ListStateEvent(ObjectNode json, Class<T> valueType,
            ListEntryFactory<T> entryFactory) {
        this.id = StateEvent.extractId(json);
        this.eventType = extractEventType(json);
        this.entries = extractEntries(json, valueType, entryFactory);
        this.insertPosition = this.eventType == EventType.INSERT
                ? extractPosition(json)
                : null;
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

    private static <X> List<ListEntry<X>> extractEntries(JsonNode json,
            Class<X> valueType, ListEntryFactory<X> entryFactory) {
        var rawEntries = json.get(Field.ENTRIES);
        if (rawEntries == null) {
            throw new MissingFieldException(Field.ENTRIES);
        }
        List<ListEntry<X>> entries = new ArrayList<>();
        for (JsonNode rawEntry : rawEntries) {
            var id = extractEntryId(rawEntry);
            var next = extractUUIDOrNull(rawEntry, Field.NEXT);
            var prev = extractUUIDOrNull(rawEntry, Field.PREV);
            var value = StateEvent
                    .convertValue(StateEvent.extractValue(rawEntry), valueType);
            entries.add(entryFactory.create(id, prev, next, value));
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

    private static InsertPosition extractPosition(JsonNode json) {
        var rawDirection = json.get(Field.POSITION);
        if (rawDirection == null) {
            var message = String.format(
                    "Missing event direction. Direction is required, and should be one of: %s",
                    Arrays.toString(InsertPosition.values()));
            throw new InvalidEventTypeException(message);
        }
        try {
            return InsertPosition.of(rawDirection.asText());
        } catch (IllegalArgumentException e) {
            var message = String.format(
                    "Invalid event direction %s. Direction should be one of: %s",
                    rawDirection.asText(),
                    Arrays.toString(InsertPosition.values()));
            throw new InvalidEventTypeException(message, e);
        }
    }

    public ObjectNode toJson() {
        ObjectNode snapshotData = StateEvent.MAPPER.createObjectNode();
        snapshotData.put(StateEvent.Field.ID, id);
        snapshotData.put(StateEvent.Field.TYPE, eventType.name().toLowerCase());
        if (entries != null) {
            ArrayNode snapshotEntries = StateEvent.MAPPER.createArrayNode();
            entries.forEach(entry -> {
                ObjectNode entryNode = snapshotEntries.addObject();
                entryNode.put(StateEvent.Field.ID, entry.id().toString());
                if (entry.next() != null) {
                    entryNode.put(Field.NEXT, entry.next().toString());
                }
                if (entry.previous() != null) {
                    entryNode.put(Field.PREV, entry.previous().toString());
                }
                if (entry.value() != null) {
                    entryNode.set(StateEvent.Field.VALUE,
                            StateEvent.MAPPER.valueToTree(entry.value()));
                }
            });
            snapshotData.set(Field.ENTRIES, snapshotEntries);
        }
        if (insertPosition != null) {
            snapshotData.put(Field.POSITION,
                    insertPosition.name().toLowerCase());
        }
        if (getAccepted() != null) {
            snapshotData.put(StateEvent.Field.ACCEPTED, getAccepted());
        }
        return snapshotData;
    }

    public String getId() {
        return id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Collection<ListEntry<T>> getEntries() {
        return entries;
    }

    public InsertPosition getPosition() {
        return insertPosition;
    }
}
