package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.ListStateEvent;
import com.vaadin.hilla.signals.core.event.StateEvent;
import com.vaadin.hilla.signals.core.event.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.MissingFieldException;
import com.vaadin.hilla.signals.operation.ListInsertOperation;
import com.vaadin.hilla.signals.operation.ListRemoveOperation;
import com.vaadin.hilla.signals.operation.ReplaceValueOperation;
import com.vaadin.hilla.signals.operation.SetValueOperation;
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

        public Entry(UUID id, @Nullable UUID prev, @Nullable UUID next,
                ValueSignal<V> valueSignal) {
            this.id = id;
            this.prev = prev;
            this.next = next;
            this.value = valueSignal;
        }

        public Entry(UUID id, ValueSignal<V> valueSignal) {
            this(id, null, null, valueSignal);
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

    public ListSignal(Class<T> valueType) {
        super(valueType);
    }

    protected ListSignal(ListSignal<T> delegate) {
        super(delegate);
    }

    @Override
    protected ListSignal<T> getDelegate() {
        return (ListSignal<T>) super.getDelegate();
    }

    @Override
    public Flux<ObjectNode> subscribe(String signalId) {
        if (getDelegate() != null) {
            return getDelegate().subscribe(signalId);
        }
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
        if (getDelegate() != null) {
            return getDelegate().createSnapshotEvent();
        }
        var entries = this.entries.values().stream()
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
        if (getDelegate() != null) {
            return getDelegate().handleInsert(event);
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
        return new Entry<>(UUID.randomUUID(), createValueSignal(value));
    }

    protected ValueSignal<T> createValueSignal(T value) {
        if (getDelegate() != null) {
            return getDelegate().createValueSignal(value);
        }
        return new ValueSignal<>(value, getValueType());
    }

    protected ListStateEvent<T> handleRemoval(ListStateEvent<T> event) {
        if (getDelegate() != null) {
            return getDelegate().handleRemoval(event);
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

        protected ValidatedListSignal(ListSignal<T> delegate) {
            super(delegate);
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

        public InsertionValidatedListSignal(ListSignal<T> delegate,
                Function<ListInsertOperation<T>, ValidationResult> insertValidator) {
            super(delegate);
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
        return new InsertionValidatedListSignal<>(this, operation);
    }

    private static class RemovalValidatedListSignal<T>
            extends ValidatedListSignal<T> {
        private final Function<ListRemoveOperation<T>, ValidationResult> removalValidator;

        public RemovalValidatedListSignal(ListSignal<T> delegate,
                Function<ListRemoveOperation<T>, ValidationResult> removalValidator) {
            super(delegate);
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
        return new RemovalValidatedListSignal<>(this, operation);
    }

    private static class ItemSetValueValidatedListSignal<T>
            extends ListSignal<T> {
        private final Function<SetValueOperation<T>, ValidationResult> itemSetValueValidator;

        public ItemSetValueValidatedListSignal(ListSignal<T> delegate,
                Function<SetValueOperation<T>, ValidationResult> itemSetValueValidator) {
            super(delegate);
            this.itemSetValueValidator = itemSetValueValidator;
        }

        @Override
        protected ValueSignal<T> createValueSignal(T value) {
            return super.createValueSignal(value)
                    .withSetOperationValidator(itemSetValueValidator);
        }
    }

    public ListSignal<T> withItemSetValueValidator(
            Function<SetValueOperation<T>, ValidationResult> operation) {
        return new ItemSetValueValidatedListSignal<>(this, operation);
    }

    private static class ItemReplaceValueValidatedListSignal<T>
            extends ListSignal<T> {
        private final Function<ReplaceValueOperation<T>, ValidationResult> itemReplaceValueValidator;

        public ItemReplaceValueValidatedListSignal(ListSignal<T> delegate,
                Function<ReplaceValueOperation<T>, ValidationResult> itemReplaceValueValidator) {
            super(delegate);
            this.itemReplaceValueValidator = itemReplaceValueValidator;
        }

        @Override
        protected ValueSignal<T> createValueSignal(T value) {
            return super.createValueSignal(value)
                    .withReplaceOperationValidator(itemReplaceValueValidator);
        }
    }

    public ListSignal<T> withItemReplaceValueValidator(
            Function<ReplaceValueOperation<T>, ValidationResult> operation) {
        return new ItemReplaceValueValidatedListSignal<>(this, operation);
    }
}
