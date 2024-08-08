package com.vaadin.hilla.signals.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class StateEventTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void constructor_withParameters_shouldCreateStateEvent() {
        String id = UUID.randomUUID().toString();
        StateEvent.EventType eventType = StateEvent.EventType.SET;
        String value = "testValue";

        StateEvent<String> event = new StateEvent<>(id, eventType, value);

        assertEquals(id, event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(value, event.getValue());
    }

    @Test
    public void constructor_withJson_shouldCreateStateEvent() {
        String clientId = UUID.randomUUID().toString();
        StateEvent.EventType eventType = StateEvent.EventType.SET;
        String value = "testValue";

        ObjectNode json = mapper.createObjectNode();
        json.put(StateEvent.Field.ID, clientId);
        json.put(StateEvent.Field.TYPE, eventType.name().toLowerCase());
        json.put(StateEvent.Field.VALUE, value);

        StateEvent<String> event = new StateEvent<>(json);

        assertEquals(clientId, event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(value, event.getValue());
    }

    @Test
    public void toJson_whenCalled_shouldReturnCorrectJson() {
        String id = UUID.randomUUID().toString();
        StateEvent.EventType eventType = StateEvent.EventType.SNAPSHOT;
        String value = "testValue";

        StateEvent<String> event = new StateEvent<>(id, eventType, value);
        ObjectNode json = event.toJson();

        assertEquals(id, json.get(StateEvent.Field.ID).asText());
        assertEquals(eventType.name().toLowerCase(),
                json.get(StateEvent.Field.TYPE).asText());
        assertEquals(value, json.get(StateEvent.Field.VALUE).asText());
    }

    @Test
    public void constructor_withJsonInvalidEventType_shouldThrowInvalidEventTypeException() {
        String clientId = UUID.randomUUID().toString();
        String value = "testValue";

        ObjectNode json = mapper.createObjectNode();
        json.put(StateEvent.Field.ID, clientId);
        json.put(StateEvent.Field.TYPE, "invalidType");
        json.put(StateEvent.Field.VALUE, value);

        Exception exception = assertThrows(
                StateEvent.InvalidEventTypeException.class,
                () -> new StateEvent<>(json));

        String expectedMessage = "Invalid event type invalidType. Type should be either of: [SNAPSHOT, SET]";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void constructor_withJsonMissingEventType_shouldThrowInvalidEventTypeException() {
        String clientId = UUID.randomUUID().toString();
        String value = "testValue";

        ObjectNode json = mapper.createObjectNode();
        json.put(StateEvent.Field.ID, clientId);
        json.put(StateEvent.Field.VALUE, value);

        Exception exception = assertThrows(
                StateEvent.InvalidEventTypeException.class,
                () -> new StateEvent<>(json));

        String expectedMessage = "Missing event type. Type is required, and should be either of: [SNAPSHOT, SET]";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void constructor_withJsonUnsupportedValueType_shouldThrowIllegalArgumentException() {
        String clientId = UUID.randomUUID().toString();
        ObjectNode json = mapper.createObjectNode();
        json.put(StateEvent.Field.ID, clientId);
        json.put(StateEvent.Field.TYPE,
                StateEvent.EventType.SET.name().toLowerCase());
        json.set(StateEvent.Field.VALUE, mapper.createArrayNode()); // Unsupported
                                                                    // type

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new StateEvent<>(json));

        String expectedMessage = "Unsupported value type";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
