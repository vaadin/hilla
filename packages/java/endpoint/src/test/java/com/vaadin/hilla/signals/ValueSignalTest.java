package com.vaadin.hilla.signals;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.StateEvent;

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
        assertThrows(NullPointerException.class, () -> new ValueSignal<>(null));
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
}
