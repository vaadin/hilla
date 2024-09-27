package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.ListStateEvent;
import com.vaadin.hilla.signals.core.event.StateEvent;
import com.vaadin.hilla.signals.core.event.exception.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.exception.MissingFieldException;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.vaadin.hilla.signals.core.event.ListStateEvent.ListEntry;

public class ListSignal<T> extends Signal<T> {

    private static final class Entry<V> implements ListEntry<V> {
        private final UUID id;
        private UUID prev;
        private UUID next;
        private final ValueSignal<V> value;

        public Entry(UUID id, @Nullable UUID prev, @Nullable UUID next, V value,
                Class<V> valueType) {
            this.id = id;
            this.prev = prev;
            this.next = next;
            this.value = new ValueSignal<V>(value, valueType);
        }

        @Override
        public UUID id() {
            return id;
        }

        @Override
        public UUID previous() {
            return prev;
        }

        @Override
        public UUID next() {
            return next;
        }

        @Override
        public V value() {
            return value.getValue();
        }

        @Override
        public ValueSignal<V> getValueSignal() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ListEntry<?> entry))
                return false;
            return Objects.equals(id, entry.id());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    private final Map<UUID, Entry<T>> entries = new HashMap<>();

    private UUID head;
    private UUID tail;

    public ListSignal(Class<T> valueType) {
        super(valueType);
    }

    @Override
    protected ObjectNode createSnapshotEvent() {
        var entries = this.entries.values().stream()
                .map(entry -> (ListEntry<T>) entry).toList();
        var listEventType = ListStateEvent.EventType.SNAPSHOT;
        var event = new ListStateEvent<>(getId().toString(), listEventType,
                entries);
        return event.toJson();
    }

    @Override
    protected boolean processEvent(ObjectNode event) {
        try {
            var stateEvent = new ListStateEvent<>(event, getValueType(),
                    Entry::new);
            return switch (stateEvent.getEventType()) {
            case INSERT -> handleInsert(stateEvent);
            case REMOVE -> handleRemoval(stateEvent);
            default -> throw new UnsupportedOperationException(
                    "Unsupported event: " + stateEvent.getEventType());
            };
        } catch (InvalidEventTypeException e) {
            throw new UnsupportedOperationException(
                    "Unsupported JSON: " + event, e);
        }
    }

    private boolean handleInsert(ListStateEvent<T> event) {
        if (event.getValue() == null) {
            throw new MissingFieldException(StateEvent.Field.VALUE);
        }
        var toBeInserted = createEntry(event.getValue());
        if (entries.containsKey(toBeInserted.id())) {
            return false;
        }
        switch (event.getPosition()) {
        case FIRST -> throw new UnsupportedOperationException(
                "Insert first is not supported");
        case BEFORE -> throw new UnsupportedOperationException(
                "Insert before is not supported");
        case AFTER -> throw new UnsupportedOperationException(
                "Insert after is not supported");
        case LAST -> {
            if (tail == null) {
                // first entry being added:
                head = tail = toBeInserted.id();
                entries.put(toBeInserted.id(), toBeInserted);
                return true;
            }
            var currentTail = entries.get(tail);
            currentTail.next = toBeInserted.id();
            toBeInserted.prev = currentTail.id();
            tail = toBeInserted.id();
            entries.put(toBeInserted.id(), toBeInserted);
            return true;
        }
        }
        return false;
    }

    private Entry<T> createEntry(T value) {
        return new Entry<>(UUID.randomUUID(), null, null, value,
                getValueType());
    }

    private boolean handleRemoval(ListStateEvent<T> event) {
        if (event.getEntryId() == null) {
            throw new MissingFieldException(ListStateEvent.Field.ENTRY_ID);
        }
        if (head == null || entries.isEmpty()) {
            return false;
        }
        var toBeRemovedEntry = entries.get(event.getEntryId());
        if (toBeRemovedEntry == null) {
            // no longer exists anyway
            return true;
        }

        entries.remove(toBeRemovedEntry.id());
        if (head.equals(toBeRemovedEntry.id())) {
            // removing head
            if (toBeRemovedEntry.next() == null) {
                // removing the only entry
                head = tail = null;
                return true;
            }
            var newHead = entries.get(toBeRemovedEntry.next());
            head = newHead.id();
            newHead.prev = null;
        } else {
            var prev = entries.get(toBeRemovedEntry.previous());
            var next = entries.get(toBeRemovedEntry.next());
            if (next == null) {
                // removing tail
                tail = prev.id();
                prev.next = null;
                return true;
            }
            prev.next = next.id();
            next.prev = prev.id();
        }
        return true;
    }
}
