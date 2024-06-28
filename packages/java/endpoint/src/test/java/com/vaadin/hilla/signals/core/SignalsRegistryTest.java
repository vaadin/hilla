package com.vaadin.hilla.signals.core;

import com.vaadin.hilla.signals.NumberSignal;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class SignalsRegistryTest {

    @Test
    public void when_inputsAreNull_throws() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();

        assertThrows(NullPointerException.class,
                () -> signalsRegistry.register(null, new NumberSignal()));
        assertThrows(NullPointerException.class,
                () -> signalsRegistry.register(UUID.randomUUID(), null));
        assertThrows(NullPointerException.class,
                () -> signalsRegistry.register(null, null));
    }

    @Test
    public void when_signalIsRegistered_clientIdToSignalIdMapping_isAlsoCreated() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        UUID clientSignalId = UUID.randomUUID();
        NumberSignal signal = new NumberSignal();

        signalsRegistry.register(clientSignalId, signal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(signal, signalsRegistry.getBySignalId(signal.getId()));

        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(1, signalsRegistry.getAllClientSignalIdsFor(signal.getId())
                .size());
        assertEquals(signal, signalsRegistry.get(clientSignalId));
    }

    @Test
    public void when_signalIsAlreadyRegistered_signalIsNotRegisteredAgain() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        UUID clientSignalId = UUID.randomUUID();
        NumberSignal signal = new NumberSignal();

        signalsRegistry.register(clientSignalId, signal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(signal, signalsRegistry.get(clientSignalId));

        UUID anotherClientSignalId = UUID.randomUUID();
        signalsRegistry.register(anotherClientSignalId, signal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(2, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(2, signalsRegistry.getAllClientSignalIdsFor(signal.getId())
                .size());
        assertEquals(signal, signalsRegistry.get(anotherClientSignalId));
    }

    @Test
    public void get_nullClientSignalIdArg_throws() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertThrows(NullPointerException.class,
                () -> signalsRegistry.get(null));
    }

    @Test
    public void when_noSignalIsFoundForClientSignalId_returnsNull() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        UUID clientSignalId = UUID.randomUUID();
        NumberSignal signal = new NumberSignal();

        signalsRegistry.register(clientSignalId, signal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(signal, signalsRegistry.get(clientSignalId));

        UUID anotherClientSignalId = UUID.randomUUID();
        assertNull(signalsRegistry.get(anotherClientSignalId));
    }

    @Test
    public void getBySignalId_nullSignalIdArg_throws() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertThrows(NullPointerException.class,
                () -> signalsRegistry.getBySignalId(null));
    }

    @Test
    public void when_noSignalIsFoundForSignalId_returnsNull() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        UUID clientSignalId = UUID.randomUUID();
        NumberSignal signal = new NumberSignal();

        signalsRegistry.register(clientSignalId, signal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(signal, signalsRegistry.get(clientSignalId));

        assertNull(signalsRegistry.getBySignalId(UUID.randomUUID()));
    }

    @Test
    public void contains_nullClientSignalIdArg_throws() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertThrows(NullPointerException.class,
                () -> signalsRegistry.contains(null));
    }

    @Test
    public void when_signalIsRegistered_contains_returnsTrue() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        UUID clientSignalId = UUID.randomUUID();
        NumberSignal signal = new NumberSignal();

        signalsRegistry.register(clientSignalId, signal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(signal, signalsRegistry.get(clientSignalId));

        assertTrue(signalsRegistry.contains(clientSignalId));
    }

    @Test
    public void when_signalIsNotRegistered_contains_returnsFalse() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());
        assertFalse(signalsRegistry.contains(UUID.randomUUID()));

        UUID clientSignalId = UUID.randomUUID();
        NumberSignal signal = new NumberSignal();

        signalsRegistry.register(clientSignalId, signal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(signal, signalsRegistry.get(clientSignalId));

        assertFalse(signalsRegistry.contains(UUID.randomUUID()));
    }

    @Test
    public void isEmpty_correctly_returns_status() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        UUID clientSignalId = UUID.randomUUID();
        NumberSignal signal = new NumberSignal();

        signalsRegistry.register(clientSignalId, signal);
        assertFalse(signalsRegistry.isEmpty());

        signalsRegistry.unregister(signal.getId());
        assertTrue(signalsRegistry.isEmpty());
    }

    @Test
    public void unregister_nullIdArg_throws() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertThrows(NullPointerException.class,
                () -> signalsRegistry.unregister(null));
    }

    @Test
    public void when_signalIsUnregistered_clientIdToSignalIdMapping_isAlsoRemoved() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        UUID clientSignalId = UUID.randomUUID();
        NumberSignal signal = new NumberSignal();

        signalsRegistry.register(clientSignalId, signal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSignalIdsFor(signal.getId())
                .size());
        assertEquals(signal, signalsRegistry.get(clientSignalId));

        signalsRegistry.unregister(signal.getId());

        assertTrue(signalsRegistry.isEmpty());
        assertNull(signalsRegistry.get(clientSignalId));
        assertEquals(0, signalsRegistry.size());
        assertEquals(0, signalsRegistry.getAllClientSignalIdsFor(signal.getId())
                .size());
    }

    @Test
    public void when_clientIdToSignalIdMappingIsRemoved_signalIsNotRemoved() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        UUID clientSignalId = UUID.randomUUID();
        NumberSignal signal = new NumberSignal();

        signalsRegistry.register(clientSignalId, signal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSignalIdsFor(signal.getId())
                .size());
        assertEquals(signal, signalsRegistry.get(clientSignalId));

        signalsRegistry.removeClientSignalToSignalMapping(clientSignalId);

        assertFalse(signalsRegistry.isEmpty());
        assertNull(signalsRegistry.get(clientSignalId));
        assertNotNull(signalsRegistry.getBySignalId(signal.getId()));
        assertEquals(1, signalsRegistry.size());
        assertEquals(0, signalsRegistry.getAllClientSignalIdsFor(signal.getId())
                .size());
    }
}
