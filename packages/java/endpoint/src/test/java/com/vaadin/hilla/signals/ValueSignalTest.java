package com.vaadin.hilla.signals;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.StateEvent;
import com.vaadin.hilla.signals.operation.ReplaceValueOperation;
import com.vaadin.hilla.signals.operation.SetValueOperation;
import com.vaadin.hilla.signals.operation.ValidationResult;

import reactor.core.publisher.Flux;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ValueSignalTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void constructor_withValueArg_usesValueAsDefaultValue() {
        var numberValueSignal = new ValueSignal<>(42.0, Double.class);
        assertEquals(42.0, numberValueSignal.getValue(), 0.0);

        var stringValueSignal = new ValueSignal<>("test", String.class);
        assertEquals("test", stringValueSignal.getValue());

        var booleanValueSignal = new ValueSignal<>(true, Boolean.class);
        assertEquals(true, booleanValueSignal.getValue());

        var name = "John";
        var age = 42;
        var adult = true;
        var objectValueSignal = new ValueSignal<>(new Person(name, age, adult),
                Person.class);
        assertEquals(name, objectValueSignal.getValue().getName());
        assertEquals(age, objectValueSignal.getValue().getAge());
        assertEquals(adult, objectValueSignal.getValue().isAdult());
    }

    @Test
    public void constructor_withoutValueArg_usesNullAsDefaultValue() {
        var numberValueSignal = new ValueSignal<>(Double.class);
        assertNull(numberValueSignal.getValue());

        var stringValueSignal = new ValueSignal<>(String.class);
        assertNull(stringValueSignal.getValue());

        var booleanValueSignal = new ValueSignal<>(Boolean.class);
        assertNull(booleanValueSignal.getValue());

        var objectValueSignal = new ValueSignal<>(Person.class);
        assertNull(objectValueSignal.getValue());
    }

    @Test
    public void constructor_withNullArgs_doesNotAcceptNull() {
        assertThrows(NullPointerException.class,
                () -> new ValueSignal<>((Class<?>) null));
        assertThrows(NullPointerException.class,
                () -> new ValueSignal<>(null, Double.class));
    }

    @Test
    public void getId_returns_not_null() {
        var signal1 = new ValueSignal<>(String.class);
        assertNotNull(signal1.getId());

        var signal3 = new ValueSignal<>("foo", String.class);
        assertNotNull(signal3.getId());
    }

    @Test
    public void subscribe_returns_flux_withJsonEvents() {
        var signal = new ValueSignal<>(Person.class);

        var flux = signal.subscribe();

        flux.subscribe(Assert::assertNotNull);
    }

    @Test
    public void submit_notifies_subscribers() {
        var signal = new ValueSignal<>(Person.class);
        Flux<ObjectNode> flux = signal.subscribe();

        var name = "John";
        var age = 42;
        var adult = true;

        var counter = new AtomicInteger(0);
        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new StateEvent<>(eventJson, Person.class);
            if (counter.get() == 0) {
                // notification for the initial value
                assertNull(stateEvent.getValue());
                assertTrue(StateEvent.isAccepted(eventJson));
            } else if (counter.get() == 1) {
                assertTrue(StateEvent.isAccepted(eventJson));
                assertEquals(name, stateEvent.getValue().getName());
                assertEquals(name, signal.getValue().getName());
                assertEquals(age, stateEvent.getValue().getAge());
                assertEquals(age, signal.getValue().getAge());
                assertEquals(adult, stateEvent.getValue().isAdult());
                assertEquals(adult, signal.getValue().isAdult());
            }
            counter.incrementAndGet();
        });

        var person = new Person(name, age, adult);
        signal.submit(createSetEvent(person));

        assertEquals(2, counter.get());
    }

    @Test
    public void submit_conditionIsMet_notifies_subscribers_with_snapshot_event() {
        var signal = new ValueSignal<>(2.0, Double.class);
        Flux<ObjectNode> flux = signal.subscribe();

        var conditionalReplaceEvent = createReplaceEvent(2.0, 3.0);

        var counter = new AtomicInteger(0);
        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new StateEvent<>(eventJson, Double.class);
            if (counter.get() == 0) {
                // notification for the initial value
                assertEquals(2.0, stateEvent.getValue(), 0.0);
                assertTrue(StateEvent.isAccepted(eventJson));
            } else if (counter.get() == 1) {
                assertEquals(conditionalReplaceEvent.get(StateEvent.Field.ID)
                        .asText(), stateEvent.getId());
                assertEquals(StateEvent.EventType.REPLACE,
                        stateEvent.getEventType());
                assertTrue(StateEvent.isAccepted(eventJson));
                assertEquals(3.0, signal.getValue(), 0.0);
            }
            counter.incrementAndGet();
        });

        signal.submit(conditionalReplaceEvent);

        assertEquals(2, counter.get());
    }

    @Test
    public void submit_conditionIsNotMet_notifies_subscribers_with_reject_event() {
        var signal = new ValueSignal<>(1.0, Double.class);
        Flux<ObjectNode> flux = signal.subscribe();

        var conditionalReplaceEvent = createReplaceEvent(2.0, 3.0);

        var counter = new AtomicInteger(0);
        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new StateEvent<>(eventJson, Double.class);
            if (counter.get() == 0) {
                // notification for the initial value
                assertTrue(StateEvent.isAccepted(eventJson));
            } else if (counter.get() == 1) {
                assertEquals(conditionalReplaceEvent.get(StateEvent.Field.ID)
                        .asText(), stateEvent.getId());
                assertEquals(StateEvent.EventType.REPLACE,
                        stateEvent.getEventType());
                assertFalse(StateEvent.isAccepted(eventJson));
                assertEquals(1.0, signal.getValue(), 0.0);
            }
            counter.incrementAndGet();
        });

        signal.submit(conditionalReplaceEvent);

        assertEquals(2, counter.get());
    }

    @Test
    public void submit_eventWithUnknownCommand_throws() {
        var signal = new ValueSignal<>("Foo", String.class);

        var exception = assertThrows(UnsupportedOperationException.class,
                () -> signal.submit(createUnknownCommandEvent()));
        assertTrue(exception.getMessage().startsWith("Unsupported JSON: "));
    }

    @Test
    public void setOperationValidated_originalInstanceIsNotLimited() {
        ValueSignal<String> unrestrictedSignal = new ValueSignal<>("Foo",
            String.class);

        unrestrictedSignal.submit(createSetEvent("Bar"));
        assertEquals("Bar", unrestrictedSignal.getValue());

        ValueSignal<String> noSetAllowedSignal = unrestrictedSignal
            .withOperationValidator(value -> {
                    if (value instanceof SetValueOperation) {
                        return ValidationResult.rejected("No set allowed");
                    }
                    return ValidationResult.ok();
                });
        // the restricted instance sees the same value as the original one:
        assertEquals("Bar", noSetAllowedSignal.getValue());

        // the restricted instance doesn't allow set operation:
        noSetAllowedSignal.submit(createSetEvent("Should-be rejected!"));
        assertEquals("Bar", noSetAllowedSignal.getValue());

        unrestrictedSignal.submit(createSetEvent("Baz"));
        assertEquals("Baz", unrestrictedSignal.getValue());
        assertEquals("Baz", noSetAllowedSignal.getValue());
    }

    @Test
    public void setOperationValidated_subscriptionWorks() {
        ValueSignal<String> unrestrictedSignal = new ValueSignal<>("Foo",
            String.class);
        ValueSignal<String> noSetAllowedSignal = unrestrictedSignal
            .withOperationValidator(value -> {
                if (value instanceof SetValueOperation) {
                    return ValidationResult.rejected("No set allowed");
                }
                return ValidationResult.ok();
            });

        Flux<ObjectNode> unrestrictedFlux = unrestrictedSignal.subscribe();
        AtomicInteger unrestrictedCounter = new AtomicInteger(0);
        unrestrictedFlux.subscribe(eventJson -> {
            unrestrictedCounter.incrementAndGet();
        });
        assertEquals(1, unrestrictedCounter.get()); // initial state

        Flux<ObjectNode> noSetAllowedFlux = noSetAllowedSignal.subscribe();
        AtomicInteger noSetAllowedCounter = new AtomicInteger(0);
        noSetAllowedFlux.subscribe(eventJson -> {
            noSetAllowedCounter.incrementAndGet();
        });
        assertEquals(1, noSetAllowedCounter.get()); // initial state

        unrestrictedSignal.submit(createSetEvent("Bar"));
        assertEquals(2, unrestrictedCounter.get());
        assertEquals(2, noSetAllowedCounter.get());

        unrestrictedSignal.submit(createSetEvent("Baz"));
        assertEquals(3, unrestrictedCounter.get());
        assertEquals(3, noSetAllowedCounter.get());
    }

    @Test
    public void setOperationValidated_otherOperationsAreNotAffected() {
        ValueSignal<String> unrestrictedSignal = new ValueSignal<>("Foo",
            String.class);
        ValueSignal<String> noSetAllowedSignal = unrestrictedSignal
            .withOperationValidator(value -> {
                if (value instanceof SetValueOperation) {
                    return ValidationResult.rejected("No set allowed");
                }
                return ValidationResult.ok();
            });

        unrestrictedSignal.submit(createSetEvent("Bar"));
        // make sure restriction is intact:
        noSetAllowedSignal.submit(createSetEvent("Should-be Rejected"));

        assertEquals("Bar", unrestrictedSignal.getValue());
        assertEquals("Bar", noSetAllowedSignal.getValue());

        // perform another operation via the restricted signal:
        noSetAllowedSignal.submit(createReplaceEvent("Bar", "Baz"));
        assertEquals("Baz", noSetAllowedSignal.getValue());
        assertEquals("Baz", unrestrictedSignal.getValue());
    }

    @Test
    public void replaceOperationValidated_originalInstanceIsNotRestricted() {
        ValueSignal<String> unrestrictedSignal = new ValueSignal<>("Foo",
            String.class);

        unrestrictedSignal.submit(createReplaceEvent("Foo", "Bar"));
        assertEquals("Bar", unrestrictedSignal.getValue());

        ValueSignal<String> noReplaceAllowedSignal = unrestrictedSignal
            .withOperationValidator(op -> {
                if (op instanceof ReplaceValueOperation) {
                    return ValidationResult.rejected("No replace allowed");
                }
                return ValidationResult.ok();
            });
        // the restricted instance sees the same value as the original one:
        assertEquals("Bar", noReplaceAllowedSignal.getValue());

        // the restricted instance doesn't allow replace operation:
        noReplaceAllowedSignal
            .submit(createReplaceEvent("Bar", "Should-be rejected!"));
        assertEquals("Bar", noReplaceAllowedSignal.getValue());

        unrestrictedSignal.submit(createReplaceEvent("Bar", "Baz"));
        assertEquals("Baz", unrestrictedSignal.getValue());
        assertEquals("Baz", noReplaceAllowedSignal.getValue());
    }

    @Test
    public void replaceOperationValidated_subscriptionWorks() {
        ValueSignal<String> unrestrictedSignal = new ValueSignal<>("Foo",
            String.class);
        ValueSignal<String> noReplaceAllowedSignal = unrestrictedSignal
            .withOperationValidator(op -> {
                if (op instanceof ReplaceValueOperation) {
                    return ValidationResult.rejected("No replace allowed");
                }
                return ValidationResult.ok();
            });

        Flux<ObjectNode> unrestrictedFlux = unrestrictedSignal.subscribe();
        AtomicInteger unrestrictedCounter = new AtomicInteger(0);
        unrestrictedFlux.subscribe(eventJson -> {
            unrestrictedCounter.incrementAndGet();
        });
        assertEquals(1, unrestrictedCounter.get()); // initial state

        Flux<ObjectNode> noReplaceAllowedFlux = noReplaceAllowedSignal
            .subscribe();
        AtomicInteger noReplaceAllowedCounter = new AtomicInteger(0);
        noReplaceAllowedFlux.subscribe(eventJson -> {
            noReplaceAllowedCounter.incrementAndGet();
        });
        assertEquals(1, noReplaceAllowedCounter.get()); // initial state

        unrestrictedSignal.submit(createReplaceEvent("Foo", "Bar"));
        assertEquals(2, unrestrictedCounter.get());
        assertEquals(2, noReplaceAllowedCounter.get());

        unrestrictedSignal.submit(createReplaceEvent("Bar", "Baz"));
        assertEquals(3, unrestrictedCounter.get());
        assertEquals(3, noReplaceAllowedCounter.get());
    }

    @Test
    public void replaceOperationValidated_otherOperationsAreNotAffected() {
        ValueSignal<String> unrestrictedSignal = new ValueSignal<>("Foo",
            String.class);
        ValueSignal<String> noReplaceAllowedSignal = unrestrictedSignal
            .withOperationValidator(op -> {
                if (op instanceof ReplaceValueOperation) {
                    return ValidationResult.rejected("No replace allowed");
                }
                return ValidationResult.ok();
            });

        unrestrictedSignal.submit(createReplaceEvent("Foo", "Bar"));
        // make sure restriction is intact:
        noReplaceAllowedSignal
            .submit(createReplaceEvent("Bar", "Should-be Rejected"));

        assertEquals("Bar", unrestrictedSignal.getValue());
        assertEquals("Bar", noReplaceAllowedSignal.getValue());

        // perform another operation via the restricted signal:
        noReplaceAllowedSignal.submit(createSetEvent("Baz"));
        assertEquals("Baz", noReplaceAllowedSignal.getValue());
        assertEquals("Baz", unrestrictedSignal.getValue());
    }

    @Test
    public void withMultipleOperationValidators_allValidatorsAreApplied() {
        ValueSignal<String> partiallyRestrictedSignal = new ValueSignal<>("Foo",
                String.class).withOperationValidator(validator -> {
                    if (validator instanceof SetValueOperation<String> setOp
                            && setOp.value().startsWith("Joe")) {
                        return ValidationResult.rejected("No Joe is allowed");
                    }
                    return ValidationResult.ok();
                });
        ValueSignal<String> fullyRestrictedSignal = partiallyRestrictedSignal
                .withOperationValidator(op -> {
                    if (op instanceof SetValueOperation) {
                        return ValidationResult.rejected("No set allowed");
                    } else if (op instanceof ReplaceValueOperation) {
                        return ValidationResult.rejected("No replace allowed");
                    }
                    return ValidationResult.ok();
                });

        partiallyRestrictedSignal.submit(createSetEvent("John Normal"));
        partiallyRestrictedSignal.submit(createSetEvent("Jane Executive"));
        partiallyRestrictedSignal
                .submit(createSetEvent("Joe Should-be-rejected"));

        assertEquals("Jane Executive", fullyRestrictedSignal.getValue());

        fullyRestrictedSignal.submit(
                createReplaceEvent("Jane Executive", "Should-be Rejected"));
        assertEquals("Jane Executive", fullyRestrictedSignal.getValue());

        fullyRestrictedSignal.submit(createSetEvent("Emma Executive"));
        assertEquals("Jane Executive", fullyRestrictedSignal.getValue());

        partiallyRestrictedSignal.submit(createSetEvent("Emma Executive"));
        assertEquals("Emma Executive", partiallyRestrictedSignal.getValue());
        assertEquals("Emma Executive", fullyRestrictedSignal.getValue());
    }

    @Test
    public void withOperationValidator_throws_whenValidatorIsNull() {
         assertThrows(NullPointerException.class, () -> new ValueSignal<>("Foo", String.class)
                .withOperationValidator(null));
    }

    @Test
    public void readonlyInstance_doesNotAllowAnyModifications() {
        ValueSignal<String> signal = new ValueSignal<>("Foo", String.class);
        ValueSignal<String> readonlySignal = signal.asReadonly();

        readonlySignal.submit(createSetEvent("Bar"));
        assertEquals("Foo", readonlySignal.getValue());

        readonlySignal.submit(createReplaceEvent("Foo", "Bar"));
        assertEquals("Foo", readonlySignal.getValue());
    }

    private <T> ObjectNode createSetEvent(T value) {
        var setEvent = new StateEvent<>(UUID.randomUUID().toString(),
                StateEvent.EventType.SET, value);
        return setEvent.toJson();
    }

    private <T> ObjectNode createReplaceEvent(T expectedValue, T value) {
        var setEvent = new StateEvent<>(UUID.randomUUID().toString(),
                StateEvent.EventType.REPLACE, value, expectedValue);
        return setEvent.toJson();
    }

    private ObjectNode createUnknownCommandEvent() {
        var unknown = mapper.createObjectNode();
        unknown.put(StateEvent.Field.ID, UUID.randomUUID().toString());
        unknown.put("concat", "bar");
        unknown.put(StateEvent.Field.VALUE, "Foo");
        return unknown;
    }
}
