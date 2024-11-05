package com.vaadin.hilla.signals;

import java.util.function.Function;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.MissingFieldException;
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

    protected NumberSignal(NumberSignal delegate) {
        super(delegate);
    }

    @Override
    protected NumberSignal getDelegate() {
        return (NumberSignal) super.getDelegate();
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
            var stateEvent = new StateEvent<>(event, Double.class);
            if (!StateEvent.EventType.INCREMENT
                    .equals(stateEvent.getEventType())) {
                return super.processEvent(event);
            }
            return handleIncrement(stateEvent);
        } catch (InvalidEventTypeException | MissingFieldException e) {
            throw new UnsupportedOperationException(
                    "Unsupported JSON: " + event, e);
        }
    }

    protected ObjectNode handleIncrement(StateEvent<Double> stateEvent) {
        if (getDelegate() != null) {
            return getDelegate().handleIncrement(stateEvent);
        } else {
            Double expectedValue = getValue();
            Double newValue = expectedValue + stateEvent.getValue();
            boolean accepted = super.compareAndSet(newValue, expectedValue);
            stateEvent.setAccepted(accepted);
            return stateEvent.toJson();
        }
    }

    public NumberSignal withIncrementOperationValidator(
            Function<Double, Boolean> validator) {
        return new IncrementOperationValidatedNumberSignal(this, validator);
    }

    private static class IncrementOperationValidatedNumberSignal extends NumberSignal {

        private final Function<Double, Boolean> validator;

        public IncrementOperationValidatedNumberSignal(NumberSignal delegate,
                Function<Double, Boolean> validator) {
            super(delegate);
            this.validator = validator;
        }

        @Override
        protected ObjectNode handleIncrement(StateEvent<Double> stateEvent) {
            if (validator.apply(stateEvent.getValue())) {
                return super.handleIncrement(stateEvent);
            }
            stateEvent.setAccepted(false);
            return stateEvent.toJson();
        }
    }
}
