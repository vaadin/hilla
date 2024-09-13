package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.ListStateEvent;
import com.vaadin.hilla.signals.core.event.StateEvent;
import com.vaadin.hilla.signals.core.event.exception.InvalidEventTypeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.vaadin.hilla.signals.core.event.ListStateEvent.ListEntry;

public class ListSignal<T> extends Signal<T> {

    public static final class Entry<T> implements ListEntry<T> {
        private final UUID id;
        private UUID prev;
        private UUID next;
        private T value;

        public Entry(UUID id, UUID prev, UUID next, T value) {
            this.id = id;
            this.prev = prev;
            this.next = next;
            this.value = value;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public UUID getPrev() {
            return prev;
        }

        @Override
        public UUID getNext() {
            return next;
        }

        @Override
        public T getValue() {
            return value;
        }

        public void setPrev(UUID prev) {
            this.prev = prev;
        }

        public void setNext(UUID next) {
            this.next = next;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ListEntry<?> entry))
                return false;
            return Objects.equals(id, entry.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    private final Map<UUID, ListEntry<T>> entries = new HashMap<>();

    private UUID head;
    private UUID tail;

    public ListSignal(Class<T> valueType) {
        super(valueType);
    }

    @Override
    protected ObjectNode createStatusUpdateEvent(String eventId,
            StateEvent.EventType eventType) {
        return ListStateEvent.toJson(eventId, eventType, entries.values());
    }

    @Override
    protected boolean processEvent(ObjectNode event) {
        try {
            var stateEvent = new ListStateEvent<>(event, getValueType(), Entry::new);
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
            throw new UnsupportedOperationException("Batch insert is not supported");
        }
        var toBeInserted = event.getEntries().iterator().next();
        if (entries.containsKey(toBeInserted.getId())) {
            return false;
        }
        switch (event.getDirection()) {
            case FIRST -> throw new UnsupportedOperationException("Insert first is not supported");
            case BEFORE -> throw new UnsupportedOperationException("Insert before is not supported");
            case AFTER -> {
                if (head == null) {
                    // first entry being added:
                    head = tail = toBeInserted.getId();
                    entries.put(toBeInserted.getId(), toBeInserted);
                    return true;
                }
                var refEntry = entries.get(toBeInserted.getPrev());
                if (refEntry == null) {
                    throw new RuntimeException("Insert after non-existing entry is not supported");
                }
                var next = entries.get(refEntry.getNext());
                refEntry.setNext(toBeInserted.getId());
                toBeInserted.setPrev(refEntry.getId());
                if (next != null) {
                    toBeInserted.setNext(next.getId());
                    next.setPrev(toBeInserted.getId());
                }
                entries.put(toBeInserted.getId(), toBeInserted);
                return true;
            }
            case LAST -> {
                if (tail == null) {
                    // first entry being added:
                    head = tail = toBeInserted.getId();
                    entries.put(toBeInserted.getId(), toBeInserted);
                    return true;
                }
                var currentTail = entries.get(tail);
                currentTail.setNext(toBeInserted.getId());
                toBeInserted.setPrev(tail);
                tail = toBeInserted.getId();
                entries.put(toBeInserted.getId(), toBeInserted);
                return true;
            }
        }
        return false;
    }

    private boolean handleRemoval(ListStateEvent<T> event) {
        if (head == null || event.getEntries().isEmpty()) {
            return false;
        }
        if (event.getEntries().size() > 1) {
            throw new UnsupportedOperationException("Batch removal is not supported");
        }
        var toBeRemoved = event.getEntries().iterator().next();
        var toBeRemovedEntry = entries.get(toBeRemoved.getId());
        if (toBeRemovedEntry == null) {
            return false;
        }

        entries.remove(toBeRemovedEntry.getId());
        if (head.equals(toBeRemovedEntry.getId())) {
            head = toBeRemovedEntry.getNext();
        } else {
            var prev = entries.get(toBeRemovedEntry.getPrev());
            var next = entries.get(toBeRemovedEntry.getNext());
            if (next == null) {
                prev.setNext(null);
                return true;
            }
            prev.setNext(next.getId());
            next.setPrev(prev.getId());
        }
        return true;
    }

}
