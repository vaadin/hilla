package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.MissingFieldException;
import com.vaadin.hilla.signals.core.event.StateEvent;
import com.vaadin.hilla.signals.operation.SignalOperation;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.function.Function;

import com.vaadin.hilla.signals.operation.ReplaceValueOperation;
import com.vaadin.hilla.signals.operation.SetValueOperation;
import com.vaadin.hilla.signals.operation.ValidationResult;
import reactor.core.publisher.Flux;

public class ValueSignal<T> extends Signal<T> {

    private T value;

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
        value = Objects.requireNonNull(defaultValue);
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

    protected ValueSignal(ValueSignal<T> delegate) {
        super(delegate);
    }

    @Override
    protected ValueSignal<T> getDelegate() {
        return (ValueSignal<T>) super.getDelegate();
    }

    @Override
    public Flux<ObjectNode> subscribe() {
        if (getDelegate() != null) {
            return getDelegate().subscribe();
        }
        return super.subscribe();
    }

    @Override
    public Flux<ObjectNode> subscribe(String signalId) {
        if (getDelegate() != null) {
            return getDelegate().subscribe(signalId);
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
        return getDelegate() != null ? getDelegate().getValue() : this.value;
    }

    @Override
    protected ObjectNode createSnapshotEvent() {
        if (getDelegate() != null) {
            return getDelegate().createSnapshotEvent();
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
     * @return the processed event, with the accepted flag set to either
     *         <code>true</code> or <code>false</code>, and the validation error
     *         set with the validator message (if case of a failure).
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
        if (getDelegate() != null) {
            return getDelegate().handleSetEvent(stateEvent);
        } else {
            this.value = stateEvent.getValue();
            stateEvent.setAccepted(true);
            return stateEvent.toJson();
        }
    }

    protected ObjectNode handleReplaceEvent(StateEvent<T> stateEvent) {
        if (getDelegate() != null) {
            return getDelegate().handleReplaceEvent(stateEvent);
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

    public ValueSignal<T> withOperationValidator(
            Function<SignalOperation, ValidationResult> validator) {
        return new OperationValidatedValueSignal<>(this, validator);
    }

    private static class OperationValidatedValueSignal<T>
            extends ValueSignal<T> {
        private final Function<SignalOperation, ValidationResult> validator;

        public OperationValidatedValueSignal(ValueSignal<T> delegate,
                Function<SignalOperation, ValidationResult> validator) {
            super(delegate);
            this.validator = validator;
        }

        @Override
        protected ObjectNode handleSetEvent(StateEvent<T> stateEvent) {
            var operation = new SetValueOperation<>(stateEvent.getId(),
                    stateEvent.getValue());
            var validation = validator.apply(operation);
            return handleValidationResult(stateEvent, validation,
                    super::handleSetEvent);
        }

        @Override
        protected ObjectNode handleReplaceEvent(StateEvent<T> stateEvent) {
            var operation = new ReplaceValueOperation<>(stateEvent.getId(),
                    stateEvent.getExpected(), stateEvent.getValue());
            var validation = validator.apply(operation);
            return handleValidationResult(stateEvent, validation,
                    super::handleReplaceEvent);
        }
    }

    @Override
    public ValueSignal<T> asReadonly() {
        return this.withOperationValidator(op -> ValidationResult
                .rejected("Read-only signal does not allow any modifications"));
    }
}
