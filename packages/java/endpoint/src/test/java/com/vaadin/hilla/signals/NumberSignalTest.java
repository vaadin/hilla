package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.StateEvent;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class NumberSignalTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void constructor_withValueArg_usesValueAsDefaultValue() {
        NumberSignal signal = new NumberSignal(42.0);

        assertEquals(42.0, signal.getValue(), 0.0);
    }

    @Test
    public void constructor_withoutValueArg_usesZeroAsDefaultValue() {
        NumberSignal signal = new NumberSignal();

        assertEquals(0.0, signal.getValue(), 0.0);
    }

    @Test
    public void constructor_withoutValueArg_acceptsNull() {
        NumberSignal signal = new NumberSignal(null);

        assertNull(signal.getValue());
    }

    @Test
    public void getId_returns_not_null() {
        NumberSignal signal1 = new NumberSignal();
        assertNotNull(signal1.getId());

        NumberSignal signal2 = new NumberSignal(null);
        assertNotNull(signal2.getId());

        NumberSignal signal3 = new NumberSignal(42.0);
        assertNotNull(signal3.getId());
    }

    @Test
    public void subscribe_returns_flux() {
        NumberSignal signal = new NumberSignal();

        Flux<ObjectNode> flux = signal.subscribe();

        assertNotNull(flux);
    }

    @Test
    public void subscribe_returns_flux_withJsonEvents() {
        NumberSignal signal = new NumberSignal();

        Flux<ObjectNode> flux = signal.subscribe();

        flux.subscribe(Assert::assertNotNull);
    }

    @Test
    public void submit_notifies_subscribers() {
        NumberSignal signal = new NumberSignal();

        Flux<ObjectNode> flux = signal.subscribe();

        var counter = new AtomicInteger(0);
        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new StateEvent<Double>(eventJson);
            if (counter.get() == 0) {
                // notification for the initial value
                assertEquals(0.0, stateEvent.getValue(), 0.0);
            } else if (counter.get() == 1) {
                assertEquals(42.0, stateEvent.getValue(), 0.0);
            }
            counter.incrementAndGet();
        });

        signal.submit(createSetEvent("42"));
    }

    @Test
    public void submit_eventWithUnknownCommand_throws() {
        NumberSignal signal = new NumberSignal();

        var exception = assertThrows(UnsupportedOperationException.class,
                () -> signal.submit(createUnknownCommandEvent()));
        assertTrue(exception.getMessage().startsWith("Unsupported JSON: "));
    }

    private ObjectNode createSetEvent(String value) {
        var setEvent = new StateEvent<>(UUID.randomUUID(),
                StateEvent.EventType.SET, Double.parseDouble(value));
        return setEvent.toJson();
    }

    private ObjectNode createUnknownCommandEvent() {
        var unknown = mapper.createObjectNode();
        unknown.put(StateEvent.Field.ID, UUID.randomUUID().toString());
        unknown.put("increase", "2");
        unknown.put(StateEvent.Field.VALUE, "42");
        return unknown;
    }
}
