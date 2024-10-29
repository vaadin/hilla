package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.ListStateEvent;
import com.vaadin.hilla.signals.core.event.StateEvent;
import com.vaadin.hilla.signals.core.event.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.MissingFieldException;
import com.vaadin.hilla.signals.operation.ListInsertOperation;
import com.vaadin.hilla.signals.operation.ListRemoveOperation;
import com.vaadin.hilla.signals.operation.ValidationResult;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

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
            this.value = new ValueSignal<>(value, valueType);
        }

        public Entry(UUID id, V value, Class<V> valueType) {
            this(id, null, null, value, valueType);
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

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ListSignal.class);
    private final Map<UUID, Entry<T>> entries = new HashMap<>();

    private UUID head;
    private UUID tail;

    private ListSignal<T> delegate;

    public ListSignal(Class<T> valueType) {
        super(valueType);
    }

    protected ListSignal(Class<T> valueType, ListSignal<T> delegate) {
        this(valueType);
        this.delegate = delegate;
    }

    @Override
    public Flux<ObjectNode> subscribe(String signalId) {
        var signalEntry = entries.get(UUID.fromString(signalId));
        return signalEntry.value.subscribe();
    }

    @Override
    public void submit(ObjectNode event) {
        var rawEventType = StateEvent.extractRawEventType(event);
        if (StateEvent.EventType.find(rawEventType).isPresent()) {
            // It is not a List structure event, find the signal to submit to.
            // For internal signals, the signal id is the event id:
            var signalId = StateEvent.extractId(event);
            var signalEntry = entries.get(UUID.fromString(signalId));
            if (signalEntry == null) {
                LOGGER.debug(
                        "Signal entry not found for id: {}. Ignoring the event: {}",
                        signalId, event);
                return;
            }
            signalEntry.value.submit(event);
        } else {
            super.submit(event);
        }
    }

    @Override
    protected ObjectNode createSnapshotEvent() {
        var entriesRef = this.delegate != null ? this.delegate.entries
                : this.entries;
        var entries = entriesRef.values().stream()
                .map(entry -> (ListEntry<T>) entry).toList();
        var event = new ListStateEvent<>(getId().toString(),
                ListStateEvent.EventType.SNAPSHOT, entries);
        event.setAccepted(true);
        return event.toJson();
    }

    @Override
    protected ObjectNode processEvent(ObjectNode event) {
        try {
            var stateEvent = new ListStateEvent<>(event, getValueType());
            return switch (stateEvent.getEventType()) {
            case INSERT -> handleInsert(stateEvent).toJson();
            case REMOVE -> handleRemoval(stateEvent).toJson();
            default -> throw new UnsupportedOperationException(
                    "Unsupported event: " + stateEvent.getEventType());
            };
        } catch (InvalidEventTypeException e) {
            throw new UnsupportedOperationException(
                    "Unsupported JSON: " + event, e);
        }
    }

    protected ListStateEvent<T> handleInsert(ListStateEvent<T> event) {
        if (delegate != null) {
            return delegate.handleInsert(event);
        }
        if (event.getValue() == null) {
            throw new MissingFieldException(StateEvent.Field.VALUE);
        }
        var toBeInserted = createEntry(event.getValue());
        if (entries.containsKey(toBeInserted.id())) {
            // already exists (the chances of this happening are extremely low)
            LOGGER.warn(
                    "Duplicate UUID generation detected when adding a new entry: {}, rejecting the insert event.",
                    toBeInserted.id());
            event.setAccepted(false);
            return event;
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
            } else {
                var currentTail = entries.get(tail);
                currentTail.next = toBeInserted.id();
                toBeInserted.prev = currentTail.id();
                tail = toBeInserted.id();
            }
            entries.put(toBeInserted.id(), toBeInserted);
            event.setEntryId(toBeInserted.id());
            event.setAccepted(true);
            return event;
        }
        }
        return event;
    }

    private Entry<T> createEntry(T value) {
        return new Entry<>(UUID.randomUUID(), value, getValueType());
    }

    protected ListStateEvent<T> handleRemoval(ListStateEvent<T> event) {
        if (delegate != null) {
            return delegate.handleRemoval(event);
        }
        if (event.getEntryId() == null) {
            throw new MissingFieldException(ListStateEvent.Field.ENTRY_ID);
        }
        if (head == null || entries.isEmpty()) {
            event.setAccepted(false);
            return event;
        }
        var toBeRemovedEntry = entries.get(event.getEntryId());
        if (toBeRemovedEntry == null) {
            // no longer exists anyway
            event.setAccepted(true);
            return event;
        }

        if (head.equals(toBeRemovedEntry.id())) {
            // removing head
            if (toBeRemovedEntry.next() == null) {
                // removing the only entry
                head = tail = null;
            } else {
                var newHead = entries.get(toBeRemovedEntry.next());
                head = newHead.id();
                newHead.prev = null;
            }
        } else {
            var prev = entries.get(toBeRemovedEntry.previous());
            var next = entries.get(toBeRemovedEntry.next());
            if (next == null) {
                // removing tail
                tail = prev.id();
                prev.next = null;
            } else {
                prev.next = next.id();
                next.prev = prev.id();
            }
        }
        entries.remove(toBeRemovedEntry.id());

        event.setAccepted(true);
        return event;
    }

    protected ListEntry<T> getEntry(UUID entryId) {
        return entries.get(entryId);
    }

    private static class ValidatedListSignal<T> extends ListSignal<T> {

        protected ValidatedListSignal(Class<T> valueType,
                ListSignal<T> delegate) {
            super(valueType, delegate);
        }

        protected ListStateEvent<T> handleValidationResult(
                ListStateEvent<T> event, ValidationResult validationResult) {
            if (validationResult.isOk()) {
                return super.handleInsert(event);
            } else {
                event.setAccepted(false);
                event.setValidationError(validationResult.getErrorMessage());
                return event;
            }
        }
    }

    private static class InsertionValidatedListSignal<T>
            extends ValidatedListSignal<T> {
        private final Function<ListInsertOperation<T>, ValidationResult> insertValidator;

        public InsertionValidatedListSignal(Class<T> valueType,
                ListSignal<T> delegate,
                Function<ListInsertOperation<T>, ValidationResult> insertValidator) {
            super(valueType, delegate);
            this.insertValidator = insertValidator;
        }

        @Override
        protected ListStateEvent<T> handleInsert(ListStateEvent<T> event) {
            var listInsertOperation = new ListInsertOperation<>(event.getId(),
                    event.getPosition(), event.getValue());
            var validationResult = insertValidator.apply(listInsertOperation);
            return handleValidationResult(event, validationResult);
        }
    }

    public ListSignal<T> withInsertionValidator(
            Function<ListInsertOperation<T>, ValidationResult> operation) {
        return new InsertionValidatedListSignal<>(getValueType(), this,
                operation);
    }

    private static class RemovalValidatedListSignal<T>
            extends ValidatedListSignal<T> {
        private final Function<ListRemoveOperation<T>, ValidationResult> removalValidator;

        public RemovalValidatedListSignal(Class<T> valueType,
                ListSignal<T> delegate,
                Function<ListRemoveOperation<T>, ValidationResult> removalValidator) {
            super(valueType, delegate);
            this.removalValidator = removalValidator;
        }

        @Override
        protected ListStateEvent<T> handleRemoval(ListStateEvent<T> event) {
            if (event.getEntryId() == null) {
                throw new MissingFieldException(ListStateEvent.Field.ENTRY_ID);
            }
            var entryToRemove = getEntry(event.getEntryId());
            var listRemoveOperation = new ListRemoveOperation<>(event.getId(),
                    entryToRemove);
            var validationResult = removalValidator.apply(listRemoveOperation);
            return handleValidationResult(event, validationResult);
        }
    }

    public ListSignal<T> withRemovalValidator(
            Function<ListRemoveOperation<T>, ValidationResult> operation) {
        return new RemovalValidatedListSignal<>(getValueType(), this,
                operation);
    }
}
