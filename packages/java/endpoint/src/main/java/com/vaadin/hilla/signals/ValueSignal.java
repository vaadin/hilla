package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.exception.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.StateEvent;
import jakarta.annotation.Nullable;

import java.util.Objects;

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

    /**
     * Returns the signal's current value.
     *
     * @return the value
     */
    @Nullable
    public T getValue() {
        return this.value;
    }

    @Override
    protected ObjectNode createStatusUpdateEvent(String eventId,
            StateEvent.EventType eventType) {
        var snapshot = new StateEvent<>(eventId, eventType, this.value);
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
    protected boolean processEvent(ObjectNode event) {
        try {
            var stateEvent = new StateEvent<>(event, getValueType());
            return switch (stateEvent.getEventType()) {
                case SET -> {
                    this.value = stateEvent.getValue();
                    yield true;
                }
                case REPLACE -> compareAndSet(stateEvent.getValue(),
                    stateEvent.getExpected());
                default -> throw new UnsupportedOperationException(
                    "Unsupported event: " + stateEvent.getEventType());
            };
        } catch (InvalidEventTypeException e) {
            throw new UnsupportedOperationException(
                    "Unsupported JSON: " + event, e);
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
}
