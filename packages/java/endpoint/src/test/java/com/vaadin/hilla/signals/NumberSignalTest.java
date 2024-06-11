package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.hilla.signals.core.JsonEvent;
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

        Flux<JsonEvent> flux = signal.subscribe();

        assertNotNull(flux);
    }

    @Test
    public void subscribe_returns_flux_withJsonEvents() {
        NumberSignal signal = new NumberSignal();

        Flux<JsonEvent> flux = signal.subscribe();

        flux.subscribe(jsonEvent -> assertNotNull(jsonEvent));
    }

    @Test
    public void submit_notifies_subscribers() {
        NumberSignal signal = new NumberSignal();

        Flux<JsonEvent> flux = signal.subscribe();

        var counter = new AtomicInteger(0);
        flux.subscribe(jsonEvent -> {
            assertNotNull(jsonEvent);
            if (counter.get() == 0) {
                // notification for the initial value
                assertEquals(0.0, jsonEvent.getJson().get("value").asDouble(),
                        0.0);
            } else if (counter.get() == 1) {
                assertEquals(42.0, jsonEvent.getJson().get("value").asDouble(),
                        0.0);
            }
            counter.incrementAndGet();
        });

        signal.submit(createSetEvent("42"));
    }

    @Test
    public void submit_setEventWithConditions_validatesValueConditionBeforeSettingNewValue() {
        NumberSignal signal = new NumberSignal();

        Flux<JsonEvent> flux = signal.subscribe();

        var counter = new AtomicInteger(0);
        flux.subscribe(jsonEvent -> {
            assertNotNull(jsonEvent);
            if (counter.get() == 1) { // ignoring the initial value notification
                // condition not met, value should not be updated
                assertEquals(0.0, jsonEvent.getJson().get("value").asDouble(),
                        0.0);
            } else if (counter.get() == 2) {
                // condition met, value should be updated
                assertEquals(42.0, jsonEvent.getJson().get("value").asDouble(),
                        0.0);
            }
            counter.incrementAndGet();
        });

        // this event should not change the value:
        signal.submit(createSetEventWithExpectedValueCondition("42", "1"));
        // this event should change the value:
        signal.submit(createSetEventWithExpectedValueCondition("42", "0"));
    }

    @Test
    public void submit_eventWithUnknownCommand_throws() {
        NumberSignal signal = new NumberSignal();

        var exception = assertThrows(UnsupportedOperationException.class,
                () -> signal.submit(createUnknownCommandEvent()));
        assertTrue(exception.getMessage().startsWith("Unsupported JSON: "));
    }

    private JsonEvent createSetEventWithExpectedValueCondition(String value,
            String expectedOriginalValue) {
        var condition = mapper.createObjectNode();
        condition.put("value", expectedOriginalValue);

        var conditions = mapper.createArrayNode();
        conditions.add(condition);

        var objectNode = mapper.createObjectNode();
        objectNode.put("set", "id");
        objectNode.put("value", value);
        objectNode.set("conditions", conditions);

        return new JsonEvent(UUID.randomUUID(), objectNode);
    }

    private JsonEvent createSetEvent(String value) {
        var objectNode = mapper.createObjectNode();
        objectNode.put("set", "id");
        objectNode.put("value", value);
        return new JsonEvent(UUID.randomUUID(), objectNode);
    }

    private JsonEvent createUnknownCommandEvent() {
        var objectNode = mapper.createObjectNode();
        objectNode.put("increase", "id");
        objectNode.put("value", "42");
        return new JsonEvent(UUID.randomUUID(), objectNode);
    }
}
