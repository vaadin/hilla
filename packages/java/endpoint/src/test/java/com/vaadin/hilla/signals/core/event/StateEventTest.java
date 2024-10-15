package com.vaadin.hilla.signals.core.event;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.UUID;

import com.vaadin.hilla.signals.Person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

        Exception exception = assertThrows(InvalidEventTypeException.class,
                () -> new StateEvent<>(json, String.class));

        String expectedMessage = "Invalid event type invalidType. Type should be one of: [SNAPSHOT, SET, REPLACE, INCREMENT]";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void constructor_withJsonMissingEventType_shouldThrowMissingFieldException() {
        String clientId = UUID.randomUUID().toString();
        String value = "testValue";

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, clientId);
        json.put(StateEvent.Field.VALUE, value);

        Exception exception = assertThrows(MissingFieldException.class,
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

    @Test
    public void eventType_of_shouldReturnCorrectEventType() {
        assertEquals(StateEvent.EventType.SNAPSHOT,
                StateEvent.EventType.of("snapshot"));
        assertEquals(StateEvent.EventType.SET, StateEvent.EventType.of("set"));
        assertEquals(StateEvent.EventType.REPLACE,
                StateEvent.EventType.of("replace"));
        assertEquals(StateEvent.EventType.INCREMENT,
                StateEvent.EventType.of("increment"));
    }

    @Test
    public void eventType_of_withInvalidType_shouldThrowIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> StateEvent.EventType.of("invalidType"));

        String expectedMessage = "No enum constant com.vaadin.hilla.signals.core.event.StateEvent.EventType.INVALIDTYPE";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void eventType_of_null_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> StateEvent.EventType.of(null));
    }

    @Test
    public void evenType_find_shouldReturnCorrectEventType() {
        assertEquals(StateEvent.EventType.SNAPSHOT,
                StateEvent.EventType.find("snapshot").get());
        assertEquals(StateEvent.EventType.SET,
                StateEvent.EventType.find("set").get());
        assertEquals(StateEvent.EventType.REPLACE,
                StateEvent.EventType.find("replace").get());
        assertEquals(StateEvent.EventType.INCREMENT,
                StateEvent.EventType.find("increment").get());
    }

    @Test
    public void eventType_find_withInvalidType_shouldReturnEmptyOptional() {
        assertTrue(StateEvent.EventType.find("invalidType").isEmpty());
        assertTrue(StateEvent.EventType.find(null).isEmpty());
    }

    @Test
    public void accepted_setter_mutatesTheAcceptedValue() {
        var event = new StateEvent<>("id", StateEvent.EventType.SET, "value");
        assertNull(event.getAccepted());
        event.setAccepted(true);
        assertTrue(event.getAccepted());
        event.setAccepted(false);
        assertFalse(event.getAccepted());
    }

    @Test
    public void isAccepted_staticMethod_returnsFalseIfAcceptedField_isNull() {
        var event = new StateEvent<>("id", StateEvent.EventType.SET, "value");
        assertFalse(StateEvent.isAccepted(event.toJson()));
    }

    @Test
    public void toJson_withAcceptedField_returnsAcceptedField() {
        var event = new StateEvent<>("id", StateEvent.EventType.SET, "value");
        var eventJson = event.toJson();
        assertFalse(eventJson.has(StateEvent.Field.ACCEPTED));
        assertFalse(StateEvent.isAccepted(eventJson));
        event.setAccepted(true);
        eventJson = event.toJson();
        assertTrue(eventJson.has(StateEvent.Field.ACCEPTED));
        assertTrue(StateEvent.isAccepted(eventJson));
    }

    @Test
    public void extractEventType_jsonWithEventType_returnsEventType() {
        var eventJson = MAPPER.createObjectNode();
        eventJson.put(StateEvent.Field.TYPE, "set");
        assertEquals(StateEvent.EventType.SET,
                StateEvent.extractEventType(eventJson));
    }

    @Test
    public void extractEventType_jsonWithoutEventType_throwsMissingFieldException() {
        var eventJson = MAPPER.createObjectNode();
        assertThrows(MissingFieldException.class,
                () -> StateEvent.extractEventType(eventJson));
    }

    @Test
    public void extractEventType_jsonWithInvalidEventType_throwsMissingFieldException() {
        var eventJson = MAPPER.createObjectNode();
        eventJson.put(StateEvent.Field.TYPE, "someType");
        assertThrows(InvalidEventTypeException.class,
                () -> StateEvent.extractEventType(eventJson));
        eventJson.set(StateEvent.Field.TYPE, MAPPER.createObjectNode());
        assertThrows(InvalidEventTypeException.class,
                () -> StateEvent.extractEventType(eventJson));
        eventJson.set(StateEvent.Field.TYPE, eventJson.nullNode());
        assertThrows(InvalidEventTypeException.class,
                () -> StateEvent.extractEventType(eventJson));
    }

    @Test
    public void extractRawEventType_jsonWithEventType_returnsEventType() {
        var eventJson = MAPPER.createObjectNode();
        eventJson.put(StateEvent.Field.TYPE, "set");
        assertEquals("set", StateEvent.extractRawEventType(eventJson));
    }

    @Test
    public void extractRawEventType_jsonWithoutEventType_throwsMissingFieldException() {
        var eventJson = MAPPER.createObjectNode();
        assertThrows(MissingFieldException.class,
                () -> StateEvent.extractRawEventType(eventJson));
    }

    @Test
    public void extractValue_jsonWithValue_returnsValue() {
        var eventJson = MAPPER.createObjectNode();
        eventJson.put(StateEvent.Field.VALUE, "value");
        assertEquals("value",
                StateEvent.extractValue(eventJson, true).asText());
    }

    @Test
    public void extractValue_jsonWithoutValue_and_mandatory_throwsMissingFieldException() {
        var eventJson = MAPPER.createObjectNode();
        assertThrows(MissingFieldException.class,
                () -> StateEvent.extractValue(eventJson, true));
    }

    @Test
    public void extractValue_jsonWithoutValue_and_optional_returnsNull() {
        var eventJson = MAPPER.createObjectNode();
        assertNull(StateEvent.extractValue(eventJson, false));
    }

    @Test
    public void extractId_jsonWithId_returnsId() {
        var eventJson = MAPPER.createObjectNode();
        eventJson.put(StateEvent.Field.ID, "1234");
        assertEquals("1234", StateEvent.extractId(eventJson));
    }

    @Test
    public void extractId_jsonWithoutId_throwsMissingFieldException() {
        var eventJson = MAPPER.createObjectNode();
        assertThrows(MissingFieldException.class,
                () -> StateEvent.extractId(eventJson));
    }

    @Test
    public void convertValue_jsonWithValidValue_returnsConvertedValue() {
        var eventJson = MAPPER.createObjectNode();
        eventJson.put(StateEvent.Field.VALUE, "value");
        assertEquals("value", StateEvent.convertValue(
                eventJson.get(StateEvent.Field.VALUE), String.class));

        eventJson.put(StateEvent.Field.VALUE, 42);
        assertEquals(42,
                StateEvent.convertValue(eventJson.get(StateEvent.Field.VALUE),
                        Integer.class).intValue());

        eventJson.set(StateEvent.Field.VALUE,
                MAPPER.valueToTree(new Person("John Doe", 42, true)));
        var person = StateEvent.convertValue(
                eventJson.get(StateEvent.Field.VALUE), Person.class);
        assertEquals(new Person("John Doe", 42, true), person);
    }
}
