package com.vaadin.hilla.signals;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.EndpointControllerMockBuilder;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.signals.core.event.StateEvent;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import reactor.core.publisher.Flux;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.vaadin.hilla.signals.internal.InternalSignal;
import com.vaadin.signals.Id;
import com.vaadin.signals.SignalEnvironment;
import com.vaadin.signals.ValueSignal;

public class ValueSignalTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void setup() {
        var appCtx = Mockito.mock(ApplicationContext.class);
        var endpointObjectMapper = EndpointControllerMockBuilder
                .createEndpointObjectMapper(appCtx,
                        new JacksonObjectMapperFactory.Json());
        SignalEnvironment.tryInitialize(endpointObjectMapper, Runnable::run);
    }

    @AfterClass
    public static void tearDown() {
    }

    @Test
    public void constructor_withValueArg_usesValueAsDefaultValue() {
        var numberValueSignal = new ValueSignal<>(42.0);
        assertEquals(42.0, numberValueSignal.peek(), 0.0);

        var stringValueSignal = new ValueSignal<>("test");
        assertEquals("test", stringValueSignal.peek());

        var booleanValueSignal = new ValueSignal<>(true);
        assertEquals(true, booleanValueSignal.peek());

        var name = "John";
        var age = 42;
        var adult = true;
        var objectValueSignal = new ValueSignal<>(new Person(name, age, adult));
        assertEquals(name, objectValueSignal.peek().getName());
        assertEquals(age, objectValueSignal.peek().getAge());
        assertEquals(adult, objectValueSignal.peek().isAdult());
    }

    @Test
    public void constructor_withoutValueArg_usesNullAsDefaultValue() {
        var numberValueSignal = new ValueSignal<>(Double.class);
        assertNull(numberValueSignal.peek());

        var stringValueSignal = new ValueSignal<>(String.class);
        assertNull(stringValueSignal.peek());

        var booleanValueSignal = new ValueSignal<>(Boolean.class);
        assertNull(booleanValueSignal.peek());

        var objectValueSignal = new ValueSignal<>(Person.class);
        assertNull(objectValueSignal.peek());
    }

    @Test
    public void constructor_withNullArgs_doesNotAcceptNull() {
        assertThrows(NullPointerException.class,
                () -> new ValueSignal<>((Class<?>) null));
        assertThrows(NullPointerException.class, () -> new ValueSignal<>(null));
    }

    @Test
    public void getId_returns_not_null() {
        var signal1 = new ValueSignal<>(String.class);
        assertNotNull(signal1.id());

        var signal3 = new ValueSignal<>("foo");
        assertNotNull(signal3.id());
    }

    @Test
    public void subscribe_returns_flux_withJsonEvents() {
        var signal = new ValueSignal<>(Person.class);

        var flux = new InternalSignal(signal).subscribe(Id.random().toString());

        flux.subscribe(Assert::assertNotNull);
    }

    @Test
    public void submit_notifies_subscribers() {
        var signal = new ValueSignal<>(Person.class);
        var clientSignalId = Id.random().toString();
        var internalSignal = new InternalSignal(signal);
        var flux = internalSignal.subscribe(clientSignalId);

        var name = "John";
        var age = 42;
        var adult = true;

        var counter = new AtomicInteger(0);
        flux.subscribe(json -> {
            assertNotNull(json);
            var eventJson = (ObjectNode) json;
            var stateEvent = new StateEvent<>(eventJson, Person.class);
            if (counter.get() == 0) {
                // notification for the initial value
                assertNull(stateEvent.getValue());
                assertTrue(StateEvent.isAccepted(eventJson));
            } else if (counter.get() == 1) {
                assertTrue(StateEvent.isAccepted(eventJson));
                assertEquals(name, stateEvent.getValue().getName());
                assertEquals(name, signal.peek().getName());
                assertEquals(age, stateEvent.getValue().getAge());
                assertEquals(age, signal.peek().getAge());
                assertEquals(adult, stateEvent.getValue().isAdult());
                assertEquals(adult, signal.peek().isAdult());
            }
            counter.incrementAndGet();
        });

        var person = new Person(name, age, adult);
        internalSignal.submit(clientSignalId, createSetEvent(person));

        assertEquals(2, counter.get());
    }

    @Test
    public void serialization_for_java8_datetime_is_supported() {
        var currentTimestamp = LocalDateTime.now();
        var signal = new ValueSignal<>(
                new Message("Hi", "John Doe", currentTimestamp));
        var clientSignalId = Id.random().toString();
        var internalSignal = new InternalSignal(signal);
        var flux = internalSignal.subscribe(clientSignalId);
        AtomicInteger counter = new AtomicInteger(0);
        var laterTimestamp = LocalDateTime.now();
        flux.subscribe(json -> {
            if (counter.get() == 0) {
                assertNotNull(json);
                var eventJson = (ObjectNode) json;
                var stateEvent = new StateEvent<>(eventJson, Message.class);
                assertEquals("Hi", stateEvent.getValue().text());
                assertEquals("John Doe", stateEvent.getValue().author());
                assertEquals(currentTimestamp,
                        stateEvent.getValue().timestamp());
            } else if (counter.get() == 1) {
                var eventJson = (ObjectNode) json;
                var stateEvent = new StateEvent<>(eventJson, Message.class);
                assertEquals("Hey", stateEvent.getValue().text());
                assertEquals("Jane Smith", stateEvent.getValue().author());
                assertEquals(laterTimestamp, stateEvent.getValue().timestamp());
            }
            counter.incrementAndGet();
        });

        internalSignal.submit(clientSignalId, createSetEvent(
                new Message("Hey", "Jane Smith", laterTimestamp)));

        assertEquals(2, counter.get());
    }

    @Test
    public void submit_conditionIsMet_notifies_subscribers_with_snapshot_event() {
        var signal = new ValueSignal<>(2.0);
        var clientSignalId = Id.random().toString();
        var internalSignal = new InternalSignal(signal);
        var flux = internalSignal.subscribe(clientSignalId);

        var conditionalReplaceEvent = createReplaceEvent(2.0, 3.0);

        var counter = new AtomicInteger(0);
        flux.subscribe(json -> {
            assertNotNull(json);
            var eventJson = (ObjectNode) json;
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
                assertEquals(3.0, signal.peek(), 0.0);
            }
            counter.incrementAndGet();
        });

        internalSignal.submit(clientSignalId, conditionalReplaceEvent);

        assertEquals(2, counter.get());
    }

    @Test
    public void submit_conditionIsNotMet_notifies_subscribers_with_reject_event() {
        var signal = new ValueSignal<>(1.0);
        var clientSignalId = Id.random().toString();
        var internalSignal = new InternalSignal(signal);
        Flux<JsonNode> flux = internalSignal.subscribe(clientSignalId);

        var conditionalReplaceEvent = createReplaceEvent(2.0, 3.0);

        var counter = new AtomicInteger(0);
        flux.subscribe(json -> {
            assertNotNull(json);
            var eventJson = (ObjectNode) json;
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
                assertEquals(1.0, signal.peek(), 0.0);
            }
            counter.incrementAndGet();
        });

        internalSignal.submit(clientSignalId, conditionalReplaceEvent);

        assertEquals(2, counter.get());
    }

    @Test
    public void submit_eventWithUnknownCommand_throws() {
        var signal = new ValueSignal<>("Foo");
        var clientSignalId = Id.random().toString();
        var internalSignal = new InternalSignal(signal);

        var exception = assertThrows(UnsupportedOperationException.class,
                () -> internalSignal.submit(clientSignalId,
                        createUnknownCommandEvent()));
        assertTrue(exception.getMessage().startsWith("Unsupported JSON: "));
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
