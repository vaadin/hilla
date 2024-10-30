package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.MissingFieldException;
import com.vaadin.hilla.signals.core.event.StateEvent;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import com.vaadin.hilla.signals.operation.ValidationResult;
import reactor.core.publisher.Flux;

public class ValueSignal<T> extends Signal<T> {

    private T value;

    private ValueSignal<T> delegate;

    /**
     * Creates a new ValueSignal with the provided default value.
     *
     * @param defaultValue
     *            the default value, not <code>null</code>
     * @param valueType
     *            the value type class, not <code>null</code>
     * @throws NullPointerException
     *             if the default defaultValue or the valueType is
     *             <code>null</code>
     */
    public ValueSignal(T defaultValue, Class<T> valueType) {
        this(valueType);
        Objects.requireNonNull(defaultValue);
        this.value = defaultValue;
    }

    /**
     * Creates a new ValueSignal with provided valueType and <code>null</code>
     * as the default value.
     *
     * @param valueType
     *            the value type class, not <code>null</code>
     * @throws NullPointerException
     *             if the default defaultValue or the valueType is
     *             <code>null</code>
     */
    public ValueSignal(Class<T> valueType) {
        super(valueType);
    }

    protected void setDelegate(ValueSignal<T> delegate) {
        this.delegate = delegate;
    }

    protected Signal<T> getDelegate() {
        return delegate;
    }

    @Override
    public Flux<ObjectNode> subscribe() {
        if (delegate != null) {
            return delegate.subscribe();
        }
        return super.subscribe();
    }

    @Override
    public Flux<ObjectNode> subscribe(String signalId) {
        if (delegate != null) {
            return delegate.subscribe(signalId);
        }
        return subscribe();
    }

    /**
     * Returns the signal's current value.
     *
     * @return the value
     */
    @Nullable
    public T getValue() {
        return delegate != null ? delegate.getValue() : this.value;
    }

    @Override
    protected ObjectNode createSnapshotEvent() {
        if (delegate != null) {
            return delegate.createSnapshotEvent();
        }
        var snapshot = new StateEvent<>(getId().toString(),
                StateEvent.EventType.SNAPSHOT, this.value);
        snapshot.setAccepted(true);
        return snapshot.toJson();
    }

    /**
     * Processes the event and updates the signal value if needed. Note that
     * this method is not thread-safe and should be called from a synchronized
     * context.
     *
     * @param event
     *            the event to process
     * @return <code>true</code> if the event was successfully processed and the
     *         signal value was updated, <code>false</code> otherwise.
     */
    @Override
    protected ObjectNode processEvent(ObjectNode event) {
        try {
            var stateEvent = new StateEvent<>(event, getValueType());
            return switch (stateEvent.getEventType()) {
            case SET -> handleSetEvent(stateEvent);
            case REPLACE -> handleReplaceEvent(stateEvent);
            default -> throw new UnsupportedOperationException(
                    "Unsupported event: " + stateEvent.getEventType());
            };
        } catch (InvalidEventTypeException | MissingFieldException e) {
            throw new UnsupportedOperationException(
                    "Unsupported JSON: " + event, e);
        }
    }

    protected ObjectNode handleSetEvent(StateEvent<T> stateEvent) {
        if (delegate != null) {
            return delegate.handleSetEvent(stateEvent);
        } else {
            this.value = stateEvent.getValue();
            stateEvent.setAccepted(true);
            return stateEvent.toJson();
        }
    }

    protected ObjectNode handleReplaceEvent(StateEvent<T> stateEvent) {
        if (delegate != null) {
            return delegate.handleReplaceEvent(stateEvent);
        } else {
            boolean accepted = compareAndSet(stateEvent.getValue(),
                    stateEvent.getExpected());
            stateEvent.setAccepted(accepted);
            return stateEvent.toJson();
        }
    }

    /**
     * Compares the current value with the expected value and updates the signal
     * value if they match. Note that this method is not thread-safe and should
     * be called from a synchronized context.
     *
     * @param newValue
     *            the new value to set
     * @param expectedValue
     *            the expected value
     * @return <code>true</code> if the value was successfully updated,
     *         <code>false</code> otherwise
     */
    protected boolean compareAndSet(T newValue, T expectedValue) {
        if (Objects.equals(this.value, expectedValue)) {
            this.value = newValue;
            return true;
        }
        return false;
    }

    protected ObjectNode handleValidationResult(StateEvent<T> event,
            ValidationResult validationResult,
            Function<StateEvent<T>, ObjectNode> handler) {
        if (validationResult.isOk()) {
            return handler.apply(event);
        } else {
            event.setAccepted(false);
            event.setValidationError(validationResult.getErrorMessage());
            return event.toJson();
        }
    }

    public ValueSignal<T> withSetOperationValidator(
            Function<T, ValidationResult> validator) {
        return new SetOperationValidatedValueSignal<>(this, validator);
    }

    private static class SetOperationValidatedValueSignal<T>
            extends ValueSignal<T> {
        private final Function<T, ValidationResult> validator;

        public SetOperationValidatedValueSignal(ValueSignal<T> delegate,
                Function<T, ValidationResult> validator) {
            super(delegate.getValueType());
            setDelegate(delegate);
            this.validator = validator;
        }

        @Override
        protected ObjectNode handleSetEvent(StateEvent<T> stateEvent) {
            var validation = validator.apply(stateEvent.getValue());
            return handleValidationResult(stateEvent, validation,
                    super::handleSetEvent);
        }
    }

    public ValueSignal<T> withReplaceOperationValidator(
            BiFunction<T, T, ValidationResult> validator) {
        return new ReplaceOperationValidatedValueSignal<>(this, validator);
    }

    private static class ReplaceOperationValidatedValueSignal<T>
            extends ValueSignal<T> {
        private final BiFunction<T, T, ValidationResult> validator;

        public ReplaceOperationValidatedValueSignal(ValueSignal<T> delegate,
                BiFunction<T, T, ValidationResult> validator) {
            super(delegate.getValueType());
            setDelegate(delegate);
            this.validator = validator;
        }

        @Override
        protected ObjectNode handleReplaceEvent(StateEvent<T> stateEvent) {
            var validation = validator.apply(stateEvent.getValue(),
                    stateEvent.getExpected());
            return handleValidationResult(stateEvent, validation,
                    super::handleReplaceEvent);
        }
    }
}
