package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.InvalidEventTypeException;
import com.vaadin.hilla.signals.core.event.MissingFieldException;
import com.vaadin.hilla.signals.core.event.StateEvent;
import com.vaadin.hilla.signals.operation.IncrementOperation;
import com.vaadin.hilla.signals.operation.OperationValidator;
import com.vaadin.hilla.signals.operation.ReplaceValueOperation;
import com.vaadin.hilla.signals.operation.SetValueOperation;
import com.vaadin.hilla.signals.operation.ValidationResult;

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

    private static class OperationValidatedNumberSignal extends NumberSignal {

        private final OperationValidator<Double> validator;

        public OperationValidatedNumberSignal(NumberSignal delegate,
                OperationValidator<Double> validator) {
            super(delegate);
            this.validator = validator;
        }

        @Override
        protected ObjectNode handleIncrement(StateEvent<Double> stateEvent) {
            var operation = IncrementOperation.of(stateEvent.getId(),
                    stateEvent.getValue());
            var validationResult = validator.validate(operation);
            return handleValidationResult(stateEvent, validationResult,
                    super::handleIncrement);
        }

        @Override
        protected ObjectNode handleSetEvent(StateEvent<Double> stateEvent) {
            var operation = SetValueOperation.of(stateEvent.getId(),
                    stateEvent.getValue());
            var validation = validator.validate(operation);
            return handleValidationResult(stateEvent, validation,
                    super::handleSetEvent);
        }

        @Override
        protected ObjectNode handleReplaceEvent(StateEvent<Double> stateEvent) {
            var operation = ReplaceValueOperation.of(stateEvent.getId(),
                    stateEvent.getExpected(), stateEvent.getValue());
            var validation = validator.validate(operation);
            return handleValidationResult(stateEvent, validation,
                    super::handleReplaceEvent);
        }
    }

    @Override
    public NumberSignal withOperationValidator(
            OperationValidator<Double> validator) {
        return new OperationValidatedNumberSignal(this, validator);
    }

    @Override
    public NumberSignal asReadonly() {
        return this.withOperationValidator(op -> ValidationResult
                .rejected("Read-only signal does not allow any modifications"));
    }
}
