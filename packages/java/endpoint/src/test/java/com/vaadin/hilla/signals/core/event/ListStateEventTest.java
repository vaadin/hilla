package com.vaadin.hilla.signals.core.event;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.EndpointControllerMockBuilder;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.signals.ValueSignal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.vaadin.hilla.signals.Person;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.*;

import static com.vaadin.hilla.signals.core.event.StateEvent.MAPPER;

public class ListStateEventTest {

    @BeforeClass
    public static void setup() {
        var appCtx = Mockito.mock(ApplicationContext.class);
        var endpointObjectMapper = EndpointControllerMockBuilder
                .createEndpointObjectMapper(appCtx,
                        new JacksonObjectMapperFactory.Json());
        StateEvent.setMapper(endpointObjectMapper);
    }

    @AfterClass
    public static void tearDown() {
        StateEvent.setMapper(null);
    }

    @Test
    public void constructor_withEntries_shouldCreateListStateEvent() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.SNAPSHOT;

        List<ListStateEvent.ListEntry<String>> entries = Arrays.asList(
                new MockListEntry<>(UUID.randomUUID(), null, null, "value1"),
                new MockListEntry<>(UUID.randomUUID(), null, null, "value2"));

        ListStateEvent<String> event = new ListStateEvent<>(id, eventType,
                entries);

        assertEquals(id, event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(entries, event.getEntries());
        assertNull(event.getValue());
        assertNull(event.getPosition());
    }

    @Test
    public void constructor_withValueAndPosition_shouldCreateListStateEvent() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.INSERT;
        String value = "testValue";
        ListStateEvent.InsertPosition position = ListStateEvent.InsertPosition.FIRST;

        ListStateEvent<String> event = new ListStateEvent<>(id, eventType,
                value, position);

        assertEquals(id, event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(value, event.getValue());
        assertEquals(position, event.getPosition());
        assertNull(event.getEntries());
    }

    @Test
    public void constructor_withJson_shouldCreateListStateEvent() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.INSERT;
        String value = "testValue";
        ListStateEvent.InsertPosition position = ListStateEvent.InsertPosition.AFTER;

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, id);
        json.put(StateEvent.Field.TYPE, eventType.name().toLowerCase());
        json.put(ListStateEvent.Field.POSITION, position.name().toLowerCase());
        json.set(StateEvent.Field.VALUE, MAPPER.valueToTree(value));

        ListStateEvent<String> event = new ListStateEvent<>(json, String.class);

        assertEquals(id, event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(value, event.getValue());
        assertEquals(position, event.getPosition());
    }

    @Test
    public void toJson_withEntries_shouldReturnCorrectJson() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.SNAPSHOT;

        UUID entryId1 = UUID.randomUUID();
        UUID entryId2 = UUID.randomUUID();

        List<ListStateEvent.ListEntry<String>> entries = Arrays.asList(
                new MockListEntry<>(entryId1, null, entryId2, "value1"),
                new MockListEntry<>(entryId2, entryId1, null, "value2"));

        ListStateEvent<String> event = new ListStateEvent<>(id, eventType,
                entries);
        ObjectNode eventJson = event.toJson();

        assertEquals(id, eventJson.get(StateEvent.Field.ID).asText());
        assertEquals(eventType.name().toLowerCase(),
                eventJson.get(StateEvent.Field.TYPE).asText());

        ArrayNode entriesJson = (ArrayNode) eventJson
                .get(ListStateEvent.Field.ENTRIES);
        assertNotNull(entriesJson);
        assertEquals(2, entriesJson.size());

        ObjectNode entryJson1 = (ObjectNode) entriesJson.get(0);
        assertEquals(entryId1.toString(),
                entryJson1.get(StateEvent.Field.ID).asText());
        assertEquals(entryId2.toString(),
                entryJson1.get(ListStateEvent.Field.NEXT).asText());
        assertFalse(entryJson1.has(ListStateEvent.Field.PREV));
        assertEquals("value1", entryJson1.get(StateEvent.Field.VALUE).asText());

        ObjectNode entryJson2 = (ObjectNode) entriesJson.get(1);
        assertEquals(entryId2.toString(),
                entryJson2.get(StateEvent.Field.ID).asText());
        assertEquals(entryId1.toString(),
                entryJson2.get(ListStateEvent.Field.PREV).asText());
        assertFalse(entryJson2.has(ListStateEvent.Field.NEXT));
        assertEquals("value2", entryJson2.get(StateEvent.Field.VALUE).asText());
    }

    @Test
    public void toJson_withValueAndPosition_shouldReturnCorrectJson() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.INSERT;
        String value = "testValue";
        ListStateEvent.InsertPosition position = ListStateEvent.InsertPosition.BEFORE;

        ListStateEvent<String> event = new ListStateEvent<>(id, eventType,
                value, position);
        ObjectNode eventJson = event.toJson();

        assertEquals(id, eventJson.get(StateEvent.Field.ID).asText());
        assertEquals(eventType.name().toLowerCase(),
                eventJson.get(StateEvent.Field.TYPE).asText());
        assertEquals(position.name().toLowerCase(),
                eventJson.get(ListStateEvent.Field.POSITION).asText());
        assertEquals(value, eventJson.get(StateEvent.Field.VALUE).asText());
    }

    @Test
    public void constructor_withJsonInvalidEventType_shouldThrowInvalidEventTypeException() {
        String id = UUID.randomUUID().toString();

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, id);
        json.put(StateEvent.Field.TYPE, "invalidType");

        Exception exception = assertThrows(InvalidEventTypeException.class,
                () -> new ListStateEvent<>(json, String.class));

        String expectedMessage = "Invalid event type invalidType. Type should be one of: [SNAPSHOT, INSERT, REMOVE]";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void constructor_withJsonInvalidInsertPosition_shouldThrowInvalidEventTypeException() {
        String id = UUID.randomUUID().toString();
        String value = "testValue";

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, id);
        json.put(StateEvent.Field.TYPE,
                ListStateEvent.EventType.INSERT.name().toLowerCase());
        json.put(ListStateEvent.Field.POSITION, "invalidPosition");
        json.set(StateEvent.Field.VALUE, MAPPER.valueToTree(value));

        Exception exception = assertThrows(InvalidEventTypeException.class,
                () -> new ListStateEvent<>(json, String.class));

        String expectedMessage = "Invalid event position: invalidPosition. Position should be one of: [FIRST, LAST, BEFORE, AFTER]";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void when_typeIsInsert_constructor_withJsonMissingPosition_shouldThrowMissingFieldException() {
        String id = UUID.randomUUID().toString();
        String value = "testValue";

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, id);
        json.put(StateEvent.Field.TYPE,
                ListStateEvent.EventType.INSERT.name().toLowerCase());
        json.set(StateEvent.Field.VALUE, MAPPER.valueToTree(value));

        Exception exception = assertThrows(MissingFieldException.class,
                () -> new ListStateEvent<>(json, String.class));

        String expectedMessage = "Missing event position. Position is required, and should be one of: [FIRST, LAST, BEFORE, AFTER]";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void when_typeIsInsert_constructor_withJsonMissingValue_shouldThrowMissingFieldException() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.INSERT;
        ListStateEvent.InsertPosition position = ListStateEvent.InsertPosition.AFTER;

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, id);
        json.put(StateEvent.Field.TYPE, eventType.name().toLowerCase());
        json.put(ListStateEvent.Field.POSITION, position.name().toLowerCase());

        Exception exception = assertThrows(MissingFieldException.class,
                () -> new ListStateEvent<>(json, String.class));

        String expectedMessage = "Missing field: value";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void getEntryId_withEntryId_shouldReturnEntryId() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.REMOVE;
        UUID entryId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, id);
        json.put(StateEvent.Field.TYPE, eventType.name().toLowerCase());
        json.put(ListStateEvent.Field.ENTRY_ID, entryId.toString());

        ListStateEvent<String> event = new ListStateEvent<>(json, String.class);

        assertEquals(entryId, event.getEntryId());
    }

    @Test
    public void setEntryId_shouldSetEntryId() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.REMOVE;

        ListStateEvent<String> event = new ListStateEvent<>(id, eventType,
                null);
        UUID entryId = UUID.randomUUID();
        event.setEntryId(entryId);

        assertEquals(entryId, event.getEntryId());
    }

    @Test
    public void accepted_setter_mutatesTheAcceptedValue() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.INSERT;
        String value = "testValue";
        ListStateEvent.InsertPosition position = ListStateEvent.InsertPosition.FIRST;

        ListStateEvent<String> event = new ListStateEvent<>(id, eventType,
                value, position);

        assertNull(event.getAccepted());
        event.setAccepted(true);
        assertTrue(event.getAccepted());
        event.setAccepted(false);
        assertFalse(event.getAccepted());
    }

    @Test
    public void toJson_withAcceptedField_returnsAcceptedField() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.INSERT;
        String value = "testValue";
        ListStateEvent.InsertPosition position = ListStateEvent.InsertPosition.FIRST;

        ListStateEvent<String> event = new ListStateEvent<>(id, eventType,
                value, position);

        ObjectNode json = event.toJson();
        assertFalse(json.has(StateEvent.Field.ACCEPTED));
        event.setAccepted(true);
        json = event.toJson();
        assertTrue(json.has(StateEvent.Field.ACCEPTED));
        assertTrue(json.get(StateEvent.Field.ACCEPTED).asBoolean());
    }

    @Test
    public void properlySerializesArbitraryValueTypes() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.INSERT;
        Person value = new Person("John Doe", 42, true);
        ListStateEvent.InsertPosition position = ListStateEvent.InsertPosition.LAST;

        ListStateEvent<Person> event = new ListStateEvent<>(id, eventType,
                value, position);

        ObjectNode eventJson = event.toJson();

        assertEquals(id, eventJson.get(StateEvent.Field.ID).asText());
        assertEquals(eventType.name().toLowerCase(),
                eventJson.get(StateEvent.Field.TYPE).asText());
        assertEquals(position.name().toLowerCase(),
                eventJson.get(ListStateEvent.Field.POSITION).asText());

        ObjectNode valueJson = (ObjectNode) eventJson
                .get(StateEvent.Field.VALUE);
        assertEquals(value.getName(), valueJson.get("name").asText());
        assertEquals(value.getAge(), valueJson.get("age").asInt());
        assertEquals(value.isAdult(), valueJson.get("adult").asBoolean());

        ListStateEvent<Person> deserializedEvent = new ListStateEvent<>(
                eventJson, Person.class);

        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getEventType(), deserializedEvent.getEventType());
        assertEquals(event.getPosition(), deserializedEvent.getPosition());

        Person deserializedValue = deserializedEvent.getValue();
        assertEquals(value.getName(), deserializedValue.getName());
        assertEquals(value.getAge(), deserializedValue.getAge());
        assertEquals(value.isAdult(), deserializedValue.isAdult());
    }

    @Test
    public void eventType_of_withValidType_shouldReturnCorrectEventType() {
        assertEquals(ListStateEvent.EventType.SNAPSHOT,
                ListStateEvent.EventType.of("snapshot"));
        assertEquals(ListStateEvent.EventType.INSERT,
                ListStateEvent.EventType.of("insert"));
        assertEquals(ListStateEvent.EventType.REMOVE,
                ListStateEvent.EventType.of("remove"));
    }

    @Test
    public void eventType_of_withInvalidType_shouldThrowIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ListStateEvent.EventType.of("invalidType"));

        String expectedMessage = "No enum constant";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void insertPosition_of_withValidPosition_shouldReturnCorrectInsertPosition() {
        assertEquals(ListStateEvent.InsertPosition.FIRST,
                ListStateEvent.InsertPosition.of("first"));
        assertEquals(ListStateEvent.InsertPosition.LAST,
                ListStateEvent.InsertPosition.of("last"));
        assertEquals(ListStateEvent.InsertPosition.BEFORE,
                ListStateEvent.InsertPosition.of("before"));
        assertEquals(ListStateEvent.InsertPosition.AFTER,
                ListStateEvent.InsertPosition.of("after"));
    }

    @Test
    public void insertPosition_of_withInvalidPosition_shouldThrowIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ListStateEvent.InsertPosition.of("invalidPosition"));

        String expectedMessage = "No enum constant";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void when_typeIsRemove_constructor_withJsonMissingEntryId_shouldThrowMissingFieldException() {
        String id = UUID.randomUUID().toString();
        ListStateEvent.EventType eventType = ListStateEvent.EventType.REMOVE;

        ObjectNode json = MAPPER.createObjectNode();
        json.put(StateEvent.Field.ID, id);
        json.put(StateEvent.Field.TYPE, eventType.name().toLowerCase());
        // Intentionally omitting 'entryId' field to simulate missing entryId

        Exception exception = assertThrows(MissingFieldException.class,
                () -> new ListStateEvent<>(json, String.class));

        String expectedMessage = "Missing field: entryId";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void extractParentSignalId_withParentSignalId_shouldReturnParentSignalId() {
        ObjectNode json = MAPPER.createObjectNode();
        json.put(ListStateEvent.Field.PARENT_SIGNAL_ID, "parent-signal-id");
        String parentSignalId = ListStateEvent.extractParentSignalId(json);
        assertEquals("parent-signal-id", parentSignalId);
    }

    @Test
    public void extractParentSignalId_withoutParentSignalId_shouldReturnNull() {
        ObjectNode json = MAPPER.createObjectNode();
        String parentSignalId = ListStateEvent.extractParentSignalId(json);
        assertNull(parentSignalId);
    }

    static class MockListEntry<T> implements ListStateEvent.ListEntry<T> {
        private final UUID id;
        private final UUID previous;
        private final UUID next;
        private final T value;

        public MockListEntry(UUID id, UUID previous, UUID next, T value) {
            this.id = id;
            this.previous = previous;
            this.next = next;
            this.value = value;
        }

        @Override
        public UUID id() {
            return id;
        }

        @Override
        public UUID previous() {
            return previous;
        }

        @Override
        public UUID next() {
            return next;
        }

        @Override
        public T value() {
            return value;
        }

        @Override
        public ValueSignal<T> getValueSignal() {
            return null;
        }
    }
}
