package com.vaadin.hilla.signals.core.event;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.UUID;

import com.vaadin.hilla.signals.Person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import static com.vaadin.hilla.signals.core.event.StateEvent.MAPPER;

public class StateEventTest {

    @Test
    public void constructor_withoutExpected_shouldCreateStateEvent() {
        String id = UUID.randomUUID().toString();
        StateEvent.EventType eventType = StateEvent.EventType.SET;
        String value = "testValue";

        StateEvent<String> event = new StateEvent<>(id, eventType, value);

        assertEquals(id, event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(value, event.getValue());
        assertNull(event.getExpected());
    }

    @Test
    public void constructor_withExpected_shouldCreateStateEventContainingExpectedValue() {
        String id = UUID.randomUUID().toString();
        StateEvent.EventType eventType = StateEvent.EventType.SET;
        String value = "testValue";
        String expected = "expectedValue";

        StateEvent<String> event = new StateEvent<>(id, eventType, value,
                expected);

        assertEquals(id, event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(value, event.getValue());
        assertEquals(expected, event.getExpected());
    }

    @Test
    public void constructor_withJson_shouldCreateStateEvent() {
        String clientId = UUID.randomUUID().toString();
        StateEvent.EventType eventType = StateEvent.EventType.SET;
        String value = "testValue";

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, clientId);
        json.put(StateEvent.Field.TYPE, eventType.name().toLowerCase());
        json.put(StateEvent.Field.VALUE, value);

        StateEvent<String> event = new StateEvent<>(json, String.class);

        assertEquals(clientId, event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(value, event.getValue());
        assertNull(event.getExpected());
    }

    @Test
    public void constructor_withJsonContainingExpected_shouldCreateStateEvent() {
        String clientId = UUID.randomUUID().toString();
        StateEvent.EventType eventType = StateEvent.EventType.SET;
        String value = "testValue";
        String expected = "expectedValue";

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, clientId);
        json.put(StateEvent.Field.TYPE, eventType.name().toLowerCase());
        json.put(StateEvent.Field.VALUE, value);
        json.put(StateEvent.Field.EXPECTED, expected);

        StateEvent<String> event = new StateEvent<>(json, String.class);

        assertEquals(clientId, event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(value, event.getValue());
        assertEquals(expected, event.getExpected());
    }

    @Test
    public void toJson_whenCalled_shouldReturnCorrectJson() {
        String id = UUID.randomUUID().toString();
        StateEvent.EventType eventType = StateEvent.EventType.SNAPSHOT;
        String value = "testValue";

        // Without expected value:
        StateEvent<String> eventWithoutExpected = new StateEvent<>(id,
                eventType, value);
        ObjectNode jsonWithoutExpected = eventWithoutExpected.toJson();

        assertEquals(id, jsonWithoutExpected.get(StateEvent.Field.ID).asText());
        assertEquals(eventType.name().toLowerCase(),
                jsonWithoutExpected.get(StateEvent.Field.TYPE).asText());
        assertEquals(value,
                jsonWithoutExpected.get(StateEvent.Field.VALUE).asText());
        assertNull(jsonWithoutExpected.get(StateEvent.Field.EXPECTED));

        // With expected value:
        String expected = "expectedValue";
        StateEvent<String> eventWitExpected = new StateEvent<>(id, eventType,
                value, expected);
        ObjectNode jsonWithExpected = eventWitExpected.toJson();

        assertEquals(id, jsonWithExpected.get(StateEvent.Field.ID).asText());
        assertEquals(eventType.name().toLowerCase(),
                jsonWithExpected.get(StateEvent.Field.TYPE).asText());
        assertEquals(value,
                jsonWithExpected.get(StateEvent.Field.VALUE).asText());
        assertEquals(expected,
                jsonWithExpected.get(StateEvent.Field.EXPECTED).asText());
    }

    @Test
    public void constructor_withJsonInvalidEventType_shouldThrowInvalidEventTypeException() {
        String clientId = UUID.randomUUID().toString();
        String value = "testValue";

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, clientId);
        json.put(StateEvent.Field.TYPE, "invalidType");
        json.put(StateEvent.Field.VALUE, value);

        Exception exception = assertThrows(
                StateEvent.InvalidEventTypeException.class,
                () -> new StateEvent<>(json, String.class));

        String expectedMessage = "Invalid event type invalidType. Type should be one of: [SNAPSHOT, SET, REPLACE, INCREMENT]";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void constructor_withJsonMissingEventType_shouldThrowInvalidEventTypeException() {
        String clientId = UUID.randomUUID().toString();
        String value = "testValue";

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, clientId);
        json.put(StateEvent.Field.VALUE, value);

        Exception exception = assertThrows(
                StateEvent.InvalidEventTypeException.class,
                () -> new StateEvent<>(json, String.class));

        String expectedMessage = "Missing event type. Type is required, and should be one of: [SNAPSHOT, SET, REPLACE, INCREMENT]";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void properlySerializesArbitraryValueTypes() {
        var id = UUID.randomUUID().toString();
        var eventType = StateEvent.EventType.SET;
        var value = new Person("John Doe", 42, true);

        var event = new StateEvent<>(id, eventType, value);

        var eventJson = event.toJson();

        assertEquals(id, eventJson.get(StateEvent.Field.ID).asText());
        assertEquals(eventType.name().toLowerCase(),
                eventJson.get(StateEvent.Field.TYPE).asText());
        var personJson = eventJson.get(StateEvent.Field.VALUE);
        assertEquals(value.getName(), personJson.get("name").asText());
        assertEquals(value.getAge(), personJson.get("age").asInt());
        assertEquals(value.isAdult(), personJson.get("adult").asBoolean());

        var deserializedEvent = new StateEvent<>(eventJson, Person.class);
        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getEventType(), deserializedEvent.getEventType());
        assertEquals(event.getValue().getName(),
                deserializedEvent.getValue().getName());
        assertEquals(event.getValue().getAge(),
                deserializedEvent.getValue().getAge());
        assertEquals(event.getValue().isAdult(),
                deserializedEvent.getValue().isAdult());
    }

}
