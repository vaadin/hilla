package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.ListStateEvent;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import static com.vaadin.hilla.signals.core.event.ListStateEvent.InsertPosition;
import static com.vaadin.hilla.signals.core.event.ListStateEvent.ListEntry;

public class ListSignalTest {

    // @formatter:off
    private record Entry<V>(UUID id, UUID previous, UUID next, V value)
        implements ListStateEvent.ListEntry<V>
    {
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ListStateEvent.ListEntry<?> entry))
                return false;
            return Objects.equals(id(), entry.id());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id());
        }
    }
    // @formatter:on

    @Test
    public void constructor_withNullArgs_doesNotAcceptNull() {
        assertThrows(NullPointerException.class, () -> new ListSignal<>(null));
    }

    @Test
    public void getId_returns_not_null() {
        var listSignal = new ListSignal<>(String.class);
        assertNotNull(listSignal.getId());
    }

    @Test
    public void subscribe_returns_flux_withJsonEvents() {
        var signal = new ListSignal<>(Person.class);

        var flux = signal.subscribe();

        flux.subscribe(Assert::assertNotNull);
    }

    @Test
    public void submit_notifies_subscribers_whenInsertingAtLast() {
        var signal = new ListSignal<>(Person.class);
        Flux<ObjectNode> flux = signal.subscribe();

        var name = "John";
        var age = 42;
        var adult = true;

        var counter = new AtomicInteger(0);
        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new ListStateEvent<>(eventJson, Person.class,
                    Entry::new);
            if (counter.get() == 0) {
                // notification for the initial state
                assertEquals(0, stateEvent.getEntries().size());
            } else if (counter.get() == 1) {
                assertEquals(1, stateEvent.getEntries().size());
                var entry = stateEvent.getEntries().iterator().next();
                assertEquals(name, entry.value().getName());
                assertEquals(age, entry.value().getAge());
                assertEquals(adult, entry.value().isAdult());
            }
            counter.incrementAndGet();
        });

        var evt = createInsertEvent(new Person(name, age, adult),
                InsertPosition.LAST);
        signal.submit(evt);

        assertEquals(2, counter.get());
    }

    @Test
    public void submit_willThrow_when_insertingAtPositionsOtherThanLast() {
        var signal = new ListSignal<>(Person.class);

        var name = "John";
        var age = 42;
        var adult = true;

        var person = new Person(name, age, adult);

        assertThrows(UnsupportedOperationException.class, () -> signal
                .submit(createInsertEvent(person, InsertPosition.FIRST)));
        assertThrows(UnsupportedOperationException.class, () -> signal
                .submit(createInsertEvent(person, InsertPosition.BEFORE)));
        assertThrows(UnsupportedOperationException.class, () -> signal
                .submit(createInsertEvent(person, InsertPosition.AFTER)));
    }

    @Test
    public void submit_many_insertLastEvents_notifiesSubscribersWithCorrectStateChanges() {
        var signal = new ListSignal<>(Person.class);
        Flux<ObjectNode> flux = signal.subscribe();

        var name = "John";
        var age = 42;
        var adult = true;

        var receivedEntries = new ArrayList<ListStateEvent.ListEntry<Person>>();
        var counter = new AtomicInteger(0);
        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new ListStateEvent<>(eventJson, Person.class,
                    Entry::new);
            if (counter.get() == 0) {
                // notification for the initial state
                assertEquals(0, stateEvent.getEntries().size());
            } else {
                receivedEntries.clear();
                receivedEntries.addAll(stateEvent.getEntries());
            }
            counter.incrementAndGet();
        });

        IntStream.of(1, 2, 3, 4, 5).forEach(i -> {
            var evt = createInsertEvent(new Person(name + i, age + i, adult),
                    InsertPosition.LAST);
            signal.submit(evt);
        });
        assertEquals(6, counter.get());

        var linkedList = buildLinkedList(receivedEntries);
        assertEquals(5, linkedList.size());

        for (int i = 0; i < linkedList.size(); i++) {
            var entry = linkedList.get(i);
            assertEquals(name + (i + 1), entry.value().getName());
            assertEquals(age + (i + 1), entry.value().getAge());
            if (i < linkedList.size() - 1) {
                var nextEntry = linkedList.get(i + 1);
                assertEquals(entry.id(), nextEntry.previous());
                assertEquals(entry.next(), nextEntry.id());
            }
        }
    }

    @Test
    public void submit_remove_notifiesWithCorrectStateChanges() {
        var signal = new ListSignal<>(Person.class);
        Flux<ObjectNode> flux = signal.subscribe();

        var name = "John";
        var age = 42;
        var adult = true;

        var receivedEntries = new ArrayList<ListStateEvent.ListEntry<Person>>();
        var counter = new AtomicInteger(0);
        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new ListStateEvent<>(eventJson, Person.class,
                    Entry::new);
            if (counter.get() == 0) {
                // notification for the initial state
                assertEquals(0, stateEvent.getEntries().size());
            } else {
                receivedEntries.clear();
                receivedEntries.addAll(stateEvent.getEntries());
            }
            counter.incrementAndGet();
        });

        var person1 = new Person(name, age, adult);
        var person2 = new Person(name + 2, age + 2, adult);
        var person3 = new Person(name + 3, age + 3, adult);

        var insert1 = createInsertEvent(person1, InsertPosition.LAST);
        var insert2 = createInsertEvent(person2, InsertPosition.LAST);
        var insert3 = createInsertEvent(person3, InsertPosition.LAST);

        signal.submit(insert1);
        signal.submit(insert2);
        signal.submit(insert3);

        var linkedList = buildLinkedList(receivedEntries);
        assertEquals(3, linkedList.size());

        var entry2 = linkedList.get(1);
        var removeEvent = new ListStateEvent<>(UUID.randomUUID().toString(),
                ListStateEvent.EventType.REMOVE, List.of(entry2));
        signal.submit(removeEvent.toJson());

        assertEquals(5, counter.get());

        linkedList = buildLinkedList(receivedEntries);
        assertEquals(2, linkedList.size());

        var entry1 = linkedList.get(0);
        var entry3 = linkedList.get(1);

        assertEquals(person1.getName(), entry1.value().getName());
        assertEquals(person1.getAge(), entry1.value().getAge());
        assertEquals(person3.getName(), entry3.value().getName());
        assertEquals(person3.getAge(), entry3.value().getAge());
    }

    @Test
    public void submit_remove_notifiesWithCorrectStateChanges_whenRemovingTheOnlyEntry() {
        var signal = new ListSignal<>(Person.class);
        Flux<ObjectNode> flux = signal.subscribe();

        var name = "John";
        var age = 42;
        var adult = true;

        var receivedEntries = new ArrayList<ListStateEvent.ListEntry<Person>>();

        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new ListStateEvent<>(eventJson, Person.class,
                    Entry::new);
            receivedEntries.clear();
            receivedEntries.addAll(stateEvent.getEntries());
        });

        var person1 = new Person(name, age, adult);
        var insert1 = createInsertEvent(person1, InsertPosition.LAST);
        signal.submit(insert1);

        var linkedList = buildLinkedList(receivedEntries);
        assertEquals(1, linkedList.size());

        var entry1 = linkedList.get(0);
        var removeEvent = new ListStateEvent<>(UUID.randomUUID().toString(),
                ListStateEvent.EventType.REMOVE, List.of(entry1));
        signal.submit(removeEvent.toJson());

        assertEquals(0, receivedEntries.size());
    }

    @Test
    public void submit_remove_notifiesWithCorrectStateChanges_whenRemovingTheHead() {
        var signal = new ListSignal<>(Person.class);
        Flux<ObjectNode> flux = signal.subscribe();

        var name = "John";
        var age = 42;
        var adult = true;

        var receivedEntries = new ArrayList<ListStateEvent.ListEntry<Person>>();

        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new ListStateEvent<>(eventJson, Person.class,
                    Entry::new);
            receivedEntries.clear();
            receivedEntries.addAll(stateEvent.getEntries());
        });

        var person1 = new Person(name, age, adult);
        var person2 = new Person(name + 2, age + 2, adult);
        var insert1 = createInsertEvent(person1, InsertPosition.LAST);
        var insert2 = createInsertEvent(person2, InsertPosition.LAST);
        signal.submit(insert1);
        signal.submit(insert2);

        var linkedList = buildLinkedList(receivedEntries);
        assertEquals(2, linkedList.size());

        var head = linkedList.get(0);
        var removeEvent = new ListStateEvent<>(UUID.randomUUID().toString(),
                ListStateEvent.EventType.REMOVE, List.of(head));
        signal.submit(removeEvent.toJson());

        linkedList = buildLinkedList(receivedEntries);
        assertEquals(1, linkedList.size());

        var entry2 = linkedList.get(0);
        assertEquals(person2.getName(), entry2.value().getName());
        assertEquals(person2.getAge(), entry2.value().getAge());

        var person3 = new Person(name + 4, age + 4, adult);
        var insert3 = createInsertEvent(person3, InsertPosition.LAST);
        signal.submit(insert3);

        linkedList = buildLinkedList(receivedEntries);
        assertEquals(2, linkedList.size());
        var newHead = linkedList.get(0);
        assertEquals(person2.getName(), newHead.value().getName());
        assertEquals(person2.getAge(), newHead.value().getAge());
    }

    @Test
    public void submit_remove_notifiesWithCorrectStateChanges_whenRemovingTheTail() {
        var signal = new ListSignal<>(Person.class);
        Flux<ObjectNode> flux = signal.subscribe();

        var name = "John";
        var age = 42;
        var adult = true;

        var receivedEntries = new ArrayList<ListStateEvent.ListEntry<Person>>();

        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new ListStateEvent<>(eventJson, Person.class,
                    Entry::new);
            receivedEntries.clear();
            receivedEntries.addAll(stateEvent.getEntries());
        });

        var person1 = new Person(name, age, adult);
        var person2 = new Person(name + 2, age + 2, adult);
        var insert1 = createInsertEvent(person1, InsertPosition.LAST);
        var insert2 = createInsertEvent(person2, InsertPosition.LAST);
        signal.submit(insert1);
        signal.submit(insert2);

        var linkedList = buildLinkedList(receivedEntries);
        assertEquals(2, linkedList.size());

        var tail = linkedList.get(1);
        var removeEvent = new ListStateEvent<>(UUID.randomUUID().toString(),
                ListStateEvent.EventType.REMOVE, List.of(tail));
        signal.submit(removeEvent.toJson());

        linkedList = buildLinkedList(receivedEntries);
        assertEquals(1, linkedList.size());

        var head = linkedList.get(0);
        assertEquals(person1.getName(), head.value().getName());
        assertEquals(person1.getAge(), head.value().getAge());

        // insert the second person again
        signal.submit(insert2);

        var person3 = new Person(name + 4, age + 4, adult);
        var insert3 = createInsertEvent(person3, InsertPosition.LAST);
        signal.submit(insert3);

        linkedList = buildLinkedList(receivedEntries);
        tail = linkedList.get(2);
        removeEvent = new ListStateEvent<>(UUID.randomUUID().toString(),
                ListStateEvent.EventType.REMOVE, List.of(tail));
        signal.submit(removeEvent.toJson());

        linkedList = buildLinkedList(receivedEntries);
        var newTail = linkedList.get(1);
        assertEquals(person2.getName(), newTail.value().getName());
        assertEquals(person2.getAge(), newTail.value().getAge());
    }

    @Test
    public void submit_various_insert_and_remove_notifiesWithCorrectStateChanges() {
        var signal = new ListSignal<>(Person.class);
        Flux<ObjectNode> flux = signal.subscribe();

        var name = "John";
        var age = 42;
        var adult = true;

        var receivedEntries = new ArrayList<ListStateEvent.ListEntry<Person>>();

        flux.subscribe(eventJson -> {
            assertNotNull(eventJson);
            var stateEvent = new ListStateEvent<>(eventJson, Person.class,
                    Entry::new);
            receivedEntries.clear();
            receivedEntries.addAll(stateEvent.getEntries());
        });

        var person1 = new Person(name + 1, age + 1, adult);
        var person2 = new Person(name + 2, age + 2, adult);
        var person3 = new Person(name + 3, age + 3, adult);
        var person4 = new Person(name + 4, age + 4, adult);
        var person5 = new Person(name + 5, age + 5, adult);
        var person6 = new Person(name + 6, age + 6, adult);

        var insert1 = createInsertEvent(person1, InsertPosition.LAST);
        var insert2 = createInsertEvent(person2, InsertPosition.LAST);
        var insert3 = createInsertEvent(person3, InsertPosition.LAST);
        var insert4 = createInsertEvent(person4, InsertPosition.LAST);
        var insert5 = createInsertEvent(person5, InsertPosition.LAST);
        var insert6 = createInsertEvent(person6, InsertPosition.LAST);

        signal.submit(insert1);
        signal.submit(insert2);
        signal.submit(insert3);
        signal.submit(insert4);
        signal.submit(insert5);

        var linkedList = buildLinkedList(receivedEntries);
        assertEquals(5, linkedList.size());

        var entry1 = linkedList.get(0);
        var entry2 = linkedList.get(1);
        var entry3 = linkedList.get(2);
        var entry4 = linkedList.get(3);
        var entry5 = linkedList.get(4);

        signal.submit(createRemoveEvent(entry2));
        signal.submit(createRemoveEvent(entry4));

        signal.submit(insert6);

        signal.submit(createRemoveEvent(entry5));

        linkedList = buildLinkedList(receivedEntries);
        assertEquals(3, linkedList.size());

        var entry6 = linkedList.get(2);

        signal.submit(insert2);
        signal.submit(insert4);

        signal.submit(createRemoveEvent(entry1));
        signal.submit(createRemoveEvent(entry3));
        signal.submit(createRemoveEvent(entry6));

        linkedList = buildLinkedList(receivedEntries);
        assertEquals(2, linkedList.size());

        assertEquals(person2.getName(), linkedList.get(0).value().getName());
        assertEquals(person4.getName(), linkedList.get(1).value().getName());

        signal.submit(createRemoveEvent(linkedList.get(0)));
        signal.submit(createRemoveEvent(linkedList.get(1)));

        assertEquals(0, receivedEntries.size());

        signal.submit(insert6);
        signal.submit(insert5);
        signal.submit(insert4);
        signal.submit(insert3);
        signal.submit(insert2);
        signal.submit(insert1);

        linkedList = buildLinkedList(receivedEntries);
        assertEquals(6, linkedList.size());

        for (int i = linkedList.size(); i > 0; i--) {
            var entry = linkedList.get(6 - i);
            assertEquals(name + i, entry.value().getName());
            assertEquals(age + i, entry.value().getAge());
        }
    }

    private <T> ObjectNode createInsertEvent(T value, InsertPosition position) {
        return new ListStateEvent<>(UUID.randomUUID().toString(),
                ListStateEvent.EventType.INSERT,
                List.of(new Entry<>(UUID.randomUUID(), null, null, value)),
                position).toJson();
    }

    private <T> ObjectNode createRemoveEvent(ListEntry<T> toRemove) {
        return new ListStateEvent<>(UUID.randomUUID().toString(),
                ListStateEvent.EventType.REMOVE, List.of(toRemove)).toJson();
    }

    private <V> List<ListStateEvent.ListEntry<V>> buildLinkedList(
            Collection<ListStateEvent.ListEntry<V>> entries) {
        Map<UUID, ListStateEvent.ListEntry<V>> entryMap = new HashMap<>();
        // Populate the map with entries, using their id as the key
        for (ListStateEvent.ListEntry<V> entry : entries) {
            entryMap.put(entry.id(), entry);
        }

        // Find the starting entry (where previous is null or a specific UUID
        // indicating the start)
        ListStateEvent.ListEntry<V> start = null;
        for (ListStateEvent.ListEntry<V> entry : entries) {
            if (entry.previous() == null) {
                start = entry;
                break;
            }
        }
        if (start == null) {
            throw new IllegalArgumentException(
                    "No head entry found (entry with previous == null).");
        }

        // Traverse and build the linked list
        List<ListStateEvent.ListEntry<V>> linkedList = new ArrayList<>();
        ListStateEvent.ListEntry<V> current = start;
        while (current != null) {
            linkedList.add(current);
            current = entryMap.get(current.next());
        }
        return linkedList;
    }
}
