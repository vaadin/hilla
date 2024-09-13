package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.exception.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.StateEvent;

/**
 * A signal that holds a number value.
 */
public class NumberSignal extends ValueSignal<Double> {

    /**
     * Creates a new NumberSignal with the provided default value.
     *
     * @param defaultValue
     *            the default value
     *
     * @throws NullPointerException
     *             if the default value is null
     */
    public NumberSignal(Double defaultValue) {
        super(defaultValue, Double.class);
    }

    /**
     * Creates a new NumberSignal with the default value of 0.
     */
    public NumberSignal() {
        this(0.0);
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
    protected boolean processEvent(ObjectNode event) {
        try {
            var stateEvent = new StateEvent<>(event, Double.class);
            if (!StateEvent.EventType.INCREMENT
                    .equals(stateEvent.getEventType())) {
                return super.processEvent(event);
            }
            Double expectedValue = getValue();
            Double newValue = expectedValue + stateEvent.getValue();
            return super.compareAndSet(newValue, expectedValue);
        } catch (InvalidEventTypeException e) {
            throw new UnsupportedOperationException(
                    "Unsupported JSON: " + event, e);
        }
    }
}
