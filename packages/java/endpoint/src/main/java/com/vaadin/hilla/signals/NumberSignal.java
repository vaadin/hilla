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

import java.util.Objects;

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

    private static class ValidatedNumberSignal extends NumberSignal {

        private final OperationValidator<Double> validator;

        private ValidatedNumberSignal(NumberSignal delegate,
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

    /**
     * Returns a new signal that validates the operations with the provided
     * validator. As the same validator is for all operations, the validator
     * should be able to handle all operations that the signal supports.
     * <p>
     * For example, the following code creates a signal that only allows
     * increment by 1:
     * <!-- @formatter:off -->
     * <pre><code>
     * NumberSignal number = new NumberSignal(42.0);
     * NumberSignal limitedNumber = number.withOperationValidator(operation -&gt; {
     *     if (op instanceof IncrementOperation increment
     *             &amp;&amp; increment.value() != 1) {
     *         return ValidationResult
     *                 .reject("Only increment by 1 is allowed");
     *     }
     *     return ValidationResult.allow();
     * });
     * </code></pre>
     * <!-- @formatter:on -->
     * Note that the above allows other operations without any validations.
     * If more concise restrictions are needed, specialized operation type
     * should be used:
     * <!-- @formatter:off -->
     * <pre><code>
     * NumberSignal number = new NumberSignal(42.0);
     * NumberSignal limitedNumber = number.withOperationValidator(operation -&gt; {
     *     return switch (operation) {
     *         case IncrementOperation increment -&gt; {
     *             if (increment.value() != 1) {
     *                 yield ValidationResult
     *                     .reject("Only increment by 1 is allowed");
     *             }
     *             yield ValidationResult.allow();
     *         }
     *         case ReplaceValueOperation&lt;Double&gt; ignored -&gt;
     *                     ValidationResult.reject("No setting is allowed");
     *         case SetValueOperation&lt;Double&gt; ignored -&gt;
     *                     ValidationResult.reject("No replacing is allowed");
     *         default -&gt; ValidationResult.reject("Unknown operation is not allowed");
     *     };
     * });
     * </code></pre>
     * <!-- @formatter:on -->
     * @param validator
     *            the operation validator, not <code>null</code>
     * @return a new signal that validates the operations with the provided
     *         validator.
     * @throws NullPointerException
     *             if the validator is <code>null</code>
     */
    @Override
    public NumberSignal withOperationValidator(
            OperationValidator<Double> validator) {
        Objects.requireNonNull(validator, "Validator cannot be null");
        return new ValidatedNumberSignal(this, validator);
    }

    @Override
    public NumberSignal asReadonly() {
        return this.withOperationValidator(op -> ValidationResult
                .reject("Read-only signal does not allow any modifications"));
    }
}
