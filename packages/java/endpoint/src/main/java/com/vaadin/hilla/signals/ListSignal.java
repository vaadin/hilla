package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.ListStateEvent;
import com.vaadin.hilla.signals.core.event.StateEvent;
import com.vaadin.hilla.signals.core.event.exception.InvalidEventTypeException;
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
        private V value;

        public Entry(UUID id, @Nullable UUID prev, @Nullable UUID next,
                V value) {
            this.id = id;
            this.prev = prev;
            this.next = next;
            this.value = value;
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
    protected ObjectNode createStatusUpdateEvent(String eventId,
            StateEvent.EventType eventType) {
        var entries = this.entries.values().stream()
                .map(entry -> (ListEntry<T>) entry).toList();
        var listEventType = ListStateEvent.EventType.of(eventType.name());
        var event = new ListStateEvent<>(eventId, listEventType, entries);
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
        if (event.getEntries().isEmpty()) {
            return false;
        }
        if (event.getEntries().size() > 1) {
            throw new UnsupportedOperationException(
                    "Batch insert is not supported");
        }
        var toBeInserted = getAsEntry(event.getEntries().iterator().next());
        if (entries.containsKey(toBeInserted.id())) {
            return false;
        }
        switch (event.getDirection()) {
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

    private Entry<T> getAsEntry(ListEntry<T> entry) {
        return new Entry<>(entry.id(), entry.previous(), entry.next(),
                entry.value());
    }

    private boolean handleRemoval(ListStateEvent<T> event) {
        if (head == null || event.getEntries().isEmpty()) {
            return false;
        }
        if (event.getEntries().size() > 1) {
            throw new UnsupportedOperationException(
                    "Batch removal is not supported");
        }
        var toBeRemoved = event.getEntries().iterator().next();
        var toBeRemovedEntry = entries.get(toBeRemoved.id());
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
