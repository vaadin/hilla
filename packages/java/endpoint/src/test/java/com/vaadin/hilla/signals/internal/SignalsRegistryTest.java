/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.signals.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.signals.Id;

public class SignalsRegistryTest {

    @Test
    public void when_inputsAreNull_throws() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        InternalSignal mockSignal = Mockito.mock(InternalSignal.class);
        Id mockId = Mockito.mock(Id.class);
        Mockito.when(mockSignal.id()).thenReturn(mockId);

        assertThrows(NullPointerException.class,
                () -> signalsRegistry.register(null, mockSignal));
        assertThrows(NullPointerException.class,
                () -> signalsRegistry.register("clientId", null));
        assertThrows(NullPointerException.class,
                () -> signalsRegistry.register(null, null));
    }

    @Test
    public void when_signalIsRegistered_clientIdToSignalIdMapping_isAlsoCreated() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        String clientSignalId = "clientId";
        InternalSignal mockSignal = Mockito.mock(InternalSignal.class);
        Id mockId = Mockito.mock(Id.class);
        Mockito.when(mockSignal.id()).thenReturn(mockId);

        signalsRegistry.register(clientSignalId, mockSignal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(mockSignal, signalsRegistry.getBySignalId(mockId));
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(1,
                signalsRegistry.getAllClientSignalIdsFor(mockId).size());
        assertEquals(mockSignal, signalsRegistry.get(clientSignalId));
    }

    @Test
    public void when_signalIsAlreadyRegistered_signalIsNotRegisteredAgain() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        String clientSignalId = "clientId";
        InternalSignal mockSignal = Mockito.mock(InternalSignal.class);
        Id mockId = Mockito.mock(Id.class);
        Mockito.when(mockSignal.id()).thenReturn(mockId);

        signalsRegistry.register(clientSignalId, mockSignal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(mockSignal, signalsRegistry.get(clientSignalId));

        String anotherClientSignalId = "anotherClientId";
        signalsRegistry.register(anotherClientSignalId, mockSignal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(2, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(2,
                signalsRegistry.getAllClientSignalIdsFor(mockId).size());
        assertEquals(mockSignal, signalsRegistry.get(anotherClientSignalId));
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

        String clientSignalId = "clientId";
        InternalSignal mockSignal = Mockito.mock(InternalSignal.class);
        Id mockId = Mockito.mock(Id.class);
        Mockito.when(mockSignal.id()).thenReturn(mockId);

        signalsRegistry.register(clientSignalId, mockSignal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(mockSignal, signalsRegistry.get(clientSignalId));

        String anotherClientSignalId = "anotherClientId";
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

        String clientSignalId = "clientId";
        InternalSignal mockSignal = Mockito.mock(InternalSignal.class);
        Id mockId = Mockito.mock(Id.class);
        Mockito.when(mockSignal.id()).thenReturn(mockId);

        signalsRegistry.register(clientSignalId, mockSignal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(mockSignal, signalsRegistry.get(clientSignalId));

        Id anotherId = Mockito.mock(Id.class);
        assertNull(signalsRegistry.getBySignalId(anotherId));
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

        String clientSignalId = "clientId";
        InternalSignal mockSignal = Mockito.mock(InternalSignal.class);
        Id mockId = Mockito.mock(Id.class);
        Mockito.when(mockSignal.id()).thenReturn(mockId);

        signalsRegistry.register(clientSignalId, mockSignal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(mockSignal, signalsRegistry.get(clientSignalId));

        assertTrue(signalsRegistry.contains(clientSignalId));
    }

    @Test
    public void when_signalIsNotRegistered_contains_returnsFalse() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());
        assertFalse(signalsRegistry.contains("randomId"));

        String clientSignalId = "clientId";
        InternalSignal mockSignal = Mockito.mock(InternalSignal.class);
        Id mockId = Mockito.mock(Id.class);
        Mockito.when(mockSignal.id()).thenReturn(mockId);

        signalsRegistry.register(clientSignalId, mockSignal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1, signalsRegistry.getAllClientSubscriptionsSize());
        assertEquals(mockSignal, signalsRegistry.get(clientSignalId));

        assertFalse(signalsRegistry.contains("anotherRandomId"));
    }

    @Test
    public void isEmpty_correctly_returns_status() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        String clientSignalId = "clientId";
        InternalSignal mockSignal = Mockito.mock(InternalSignal.class);
        Id mockId = Mockito.mock(Id.class);
        Mockito.when(mockSignal.id()).thenReturn(mockId);

        signalsRegistry.register(clientSignalId, mockSignal);
        assertFalse(signalsRegistry.isEmpty());

        signalsRegistry.unregister(mockId);
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

        String clientSignalId = "clientId";
        InternalSignal mockSignal = Mockito.mock(InternalSignal.class);
        Id mockId = Mockito.mock(Id.class);
        Mockito.when(mockSignal.id()).thenReturn(mockId);

        signalsRegistry.register(clientSignalId, mockSignal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1,
                signalsRegistry.getAllClientSignalIdsFor(mockId).size());
        assertEquals(mockSignal, signalsRegistry.get(clientSignalId));

        signalsRegistry.unregister(mockId);

        assertTrue(signalsRegistry.isEmpty());
        assertNull(signalsRegistry.get(clientSignalId));
        assertEquals(0, signalsRegistry.size());
        assertEquals(0,
                signalsRegistry.getAllClientSignalIdsFor(mockId).size());
    }

    @Test
    public void when_clientIdToSignalIdMappingIsRemoved_signalIsNotRemoved() {
        SignalsRegistry signalsRegistry = new SignalsRegistry();
        assertTrue(signalsRegistry.isEmpty());

        String clientSignalId = "clientId";
        InternalSignal mockSignal = Mockito.mock(InternalSignal.class);
        Id mockId = Mockito.mock(Id.class);
        Mockito.when(mockSignal.id()).thenReturn(mockId);

        signalsRegistry.register(clientSignalId, mockSignal);

        assertEquals(1, signalsRegistry.size());
        assertEquals(1,
                signalsRegistry.getAllClientSignalIdsFor(mockId).size());
        assertEquals(mockSignal, signalsRegistry.get(clientSignalId));

        signalsRegistry.removeClientSignalToSignalMapping(clientSignalId);

        assertFalse(signalsRegistry.isEmpty());
        assertNull(signalsRegistry.get(clientSignalId));
        assertNotNull(signalsRegistry.getBySignalId(mockId));
        assertEquals(1, signalsRegistry.size());
        assertEquals(0,
                signalsRegistry.getAllClientSignalIdsFor(mockId).size());
    }
}
