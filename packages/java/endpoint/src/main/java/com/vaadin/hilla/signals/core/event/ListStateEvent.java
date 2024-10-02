package com.vaadin.hilla.signals.core.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.ValueSignal;

import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class ListStateEvent<T> {

    public interface ListEntry<T> {
        UUID id();

        @Nullable
        UUID previous();

        @Nullable
        UUID next();

        T value();

        ValueSignal<T> getValueSignal();
    }

    @FunctionalInterface
    public interface ListEntryFactory<T> {
        ListEntry<T> create(UUID id, UUID prev, UUID next, T value,
                Class<T> valueType);
    }

    /**
     * The field names used in the JSON representation of the state event.
     */
    public static final class Field {
        public static final String NEXT = "next";
        public static final String PREV = "prev";
        public static final String POSITION = "position";
        public static final String ENTRIES = "entries";
        public static final String ENTRY_ID = "entryId";
        public static final String PARENT_SIGNAL_ID = "parentSignalId";
    }

    /**
     * Possible types of state events.
     */
    public enum InsertPosition {
        FIRST, LAST, BEFORE, AFTER;

        public static InsertPosition of(String direction) {
            return valueOf(direction.toUpperCase());
        }
    }

    /**
     * Possible types of state events.
     */
    public enum EventType {
        SNAPSHOT, INSERT, REMOVE;

        public static EventType of(String type) {
            return valueOf(type.toUpperCase());
        }
    }

    private final String id;
    private final EventType eventType;
    private Boolean accepted;
    private final T value;
    // Only used for snapshot event:
    private final Collection<ListEntry<T>> entries;
    private UUID entryId;
    // Only used for insert event:
    private final InsertPosition insertPosition;

    public ListStateEvent(String id, EventType eventType,
            Collection<ListEntry<T>> entries) {
        this.id = id;
        this.eventType = eventType;
        this.insertPosition = null;
        this.value = null;
        this.entries = entries;
    }

    public ListStateEvent(String id, EventType eventType, T value,
            InsertPosition insertPosition) {
        this.id = id;
        this.eventType = eventType;
        this.value = value;
        this.insertPosition = insertPosition;
        this.entries = null;
    }

    /**
     * Creates a new state event using the given JSON representation.
     *
     * @param json
     *            The JSON representation of the event.
     */
    public ListStateEvent(ObjectNode json, Class<T> valueType) {
        this.id = StateEvent.extractId(json);
        this.eventType = extractEventType(json);
        this.value = this.eventType == EventType.INSERT
                ? StateEvent.convertValue(StateEvent.extractValue(json, true),
                        valueType)
                : null;
        this.insertPosition = this.eventType == EventType.INSERT
                ? extractPosition(json)
                : null;
        this.entryId = this.eventType == EventType.REMOVE
                ? UUID.fromString(extractEntryId(json))
                : null;
        this.entries = null;
    }

    private static EventType extractEventType(JsonNode json) {
        var rawType = StateEvent.extractRawEventType(json);
        try {
            return EventType.of(rawType);
        } catch (IllegalArgumentException e) {
            var message = String.format(
                    "Invalid event type %s. Type should be one of: %s", rawType,
                    Arrays.toString(EventType.values()));
            throw new InvalidEventTypeException(message, e);
        }
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

    public static String extractParentSignalId(JsonNode json) {
        var rawParentSignalId = json.get(Field.PARENT_SIGNAL_ID);
        if (rawParentSignalId == null) {
            return null;
        }
        return rawParentSignalId.asText();
    }

    private static String extractEntryId(JsonNode json) {
        var entryId = json.get(Field.ENTRY_ID);
        return entryId == null ? null : entryId.asText();
    }

    public ObjectNode toJson() {
        ObjectNode snapshotData = StateEvent.MAPPER.createObjectNode();
        snapshotData.put(StateEvent.Field.ID, id);
        snapshotData.put(StateEvent.Field.TYPE, eventType.name().toLowerCase());
        if (value != null) {
            snapshotData.set(StateEvent.Field.VALUE,
                    StateEvent.MAPPER.valueToTree(value));
        }
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
        if (entryId != null) {
            snapshotData.put(Field.ENTRY_ID, entryId.toString());
        }
        if (accepted != null) {
            snapshotData.put(StateEvent.Field.ACCEPTED, accepted);
        }
        return snapshotData;
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

    public T getValue() {
        return value;
    }

    public InsertPosition getPosition() {
        return insertPosition;
    }

    public UUID getEntryId() {
        return entryId;
    }

    public void setEntryId(UUID entryId) {
        this.entryId = entryId;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }
}
