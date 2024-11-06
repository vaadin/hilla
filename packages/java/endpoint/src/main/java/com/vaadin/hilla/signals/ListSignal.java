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
import com.vaadin.hilla.signals.operation.SignalOperation;
import com.vaadin.hilla.signals.operation.ValidationResult;
import com.vaadin.hilla.signals.operation.ValueOperation;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
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
        // check if the event is targeting a child signal:
        if (StateEvent.EventType.find(rawEventType).isPresent()) {
            submitToChild(event);
        } else {
            super.submit(event);
        }
    }

    protected void submitToChild(ObjectNode event) {
        if (getDelegate() != null) {
            getDelegate().submitToChild(event);
            return;
        }
        // For internal signals, the signal id is the event id:
        var entryId = StateEvent.extractId(event);
        var signalEntry = entries.get(UUID.fromString(entryId));
        if (signalEntry == null) {
            LOGGER.debug(
                    "Signal entry not found for id: {}. Ignoring the event: {}",
                    entryId, event);
            return;
        }
        signalEntry.value.submit(event);
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

    private ValueSignal<T> createValueSignal(T value) {
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
                ListStateEvent<T> event, ValidationResult validationResult,
                Function<ListStateEvent<T>, ListStateEvent<T>> handler) {
            if (validationResult.isOk()) {
                return handler.apply(event);
            } else {
                return rejectEvent(event, validationResult);
            }
        }

        private ListStateEvent<T> rejectEvent(ListStateEvent<T> event,
                ValidationResult result) {
            event.setAccepted(false);
            event.setValidationError(result.getErrorMessage());
            return event;
        }

        protected void handleValidationResult(ObjectNode event,
                ValidationResult validationResult,
                Consumer<ObjectNode> handler) {
            if (validationResult.isOk()) {
                handler.accept(event);
            } else {
                handler.accept(rejectEvent(event, validationResult));
            }
        }

        private ObjectNode rejectEvent(ObjectNode event,
                ValidationResult result) {
            var stateEvent = new StateEvent<>(event, getValueType());
            stateEvent.setAccepted(false);
            stateEvent.setValidationError(result.getErrorMessage());
            return stateEvent.toJson();
        }
    }

    private static class ValueOperationValidatedListSignal<T>
            extends ValidatedListSignal<T> {
        private final Function<ValueOperation<T>, ValidationResult> valueValidator;

        public ValueOperationValidatedListSignal(ListSignal<T> delegate,
                Function<ValueOperation<T>, ValidationResult> valueValidator) {
            super(delegate);
            this.valueValidator = valueValidator;
        }

        @Override
        protected ListStateEvent<T> handleInsert(ListStateEvent<T> event) {
            var listInsertOperation = new ListInsertOperation<>(event.getId(),
                    event.getPosition(), event.getValue());
            var validationResult = valueValidator.apply(listInsertOperation);
            return handleValidationResult(event, validationResult,
                    super::handleInsert);
        }

        @Override
        protected void submitToChild(ObjectNode event) {
            // are we interested in this event:
            if (!StateEvent.isSetEvent(event)
                    && !StateEvent.isReplaceEvent(event)) {
                super.submitToChild(event);
                return;
            }

            var valueOperation = extractValueOperation(event);
            var validationResult = valueValidator.apply(valueOperation);
            handleValidationResult(event, validationResult,
                    super::submitToChild);
        }

        private ValueOperation<T> extractValueOperation(ObjectNode event) {
            if (StateEvent.isSetEvent(event)) {
                return SetValueOperation.of(event, getValueType());
            } else if (StateEvent.isReplaceEvent(event)) {
                return ReplaceValueOperation.of(event, getValueType());
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported event: " + event);
            }
        }
    }

    public ListSignal<T> withValueOperationValidator(
            Function<ValueOperation<T>, ValidationResult> operation) {
        return new ValueOperationValidatedListSignal<>(this, operation);
    }

    private static class GenericOperationValidatedListSignal<T>
            extends ValidatedListSignal<T> {
        private final Function<SignalOperation, ValidationResult> operationValidator;

        public GenericOperationValidatedListSignal(ListSignal<T> delegate,
                Function<SignalOperation, ValidationResult> operationValidator) {
            super(delegate);
            this.operationValidator = operationValidator;
        }

        @Override
        protected ListStateEvent<T> handleInsert(ListStateEvent<T> event) {
            var listInsertOperation = new ListInsertOperation<>(event.getId(),
                    event.getPosition(), event.getValue());
            var validationResult = operationValidator
                    .apply(listInsertOperation);
            return handleValidationResult(event, validationResult,
                    super::handleInsert);
        }

        @Override
        protected ListStateEvent<T> handleRemoval(ListStateEvent<T> event) {
            if (event.getEntryId() == null) {
                throw new MissingFieldException(ListStateEvent.Field.ENTRY_ID);
            }
            var entryToRemove = getEntry(event.getEntryId());
            var listRemoveOperation = new ListRemoveOperation<>(event.getId(),
                    entryToRemove);
            var validationResult = operationValidator
                    .apply(listRemoveOperation);
            return handleValidationResult(event, validationResult,
                    super::handleRemoval);
        }

        @Override
        protected void submitToChild(ObjectNode event) {
            // are we interested in this event:
            if (!StateEvent.isSetEvent(event)
                    && !StateEvent.isReplaceEvent(event)) {
                super.submitToChild(event);
                return;
            }

            var valueOperation = extractValueOperation(event);
            var validationResult = operationValidator.apply(valueOperation);
            handleValidationResult(event, validationResult,
                    super::submitToChild);
        }

        private ValueOperation<T> extractValueOperation(ObjectNode event) {
            if (StateEvent.isSetEvent(event)) {
                return SetValueOperation.of(event, getValueType());
            } else if (StateEvent.isReplaceEvent(event)) {
                return ReplaceValueOperation.of(event, getValueType());
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported event: " + event);
            }
        }
    }

    public ListSignal<T> withOperationValidator(
            Function<SignalOperation, ValidationResult> operation) {
        return new GenericOperationValidatedListSignal<>(this, operation);
    }

    @Override
    public ListSignal<T> asReadonly() {
        return this.withOperationValidator(op -> ValidationResult
                .rejected("Read-only signal does not allow any modifications"));
    }
}
