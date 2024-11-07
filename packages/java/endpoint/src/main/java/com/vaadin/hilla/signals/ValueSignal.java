package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.MissingFieldException;
import com.vaadin.hilla.signals.core.event.StateEvent;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ValueSignal<T> extends Signal<T> {

    protected AtomicReference<T> value;

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
        this.value = new AtomicReference<>(defaultValue);
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
        return value.get();
    }

    @Override
    protected ObjectNode createSnapshotEvent() {
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
                case SET -> {
                    value.set(stateEvent.getValue());
                    stateEvent.setAccepted(true);
                    yield stateEvent.toJson();
                }
                case REPLACE -> {
                    boolean accepted = value.compareAndSet(
                        stateEvent.getExpected(),
                        stateEvent.getValue());
                    stateEvent.setAccepted(accepted);
                    yield stateEvent.toJson();
                }
                default -> throw new UnsupportedOperationException(
                    "Unsupported event: " + stateEvent.getEventType());
            };
        } catch (InvalidEventTypeException | MissingFieldException e) {
            throw new UnsupportedOperationException(
                    "Unsupported JSON: " + event, e);
        }
    }
}
