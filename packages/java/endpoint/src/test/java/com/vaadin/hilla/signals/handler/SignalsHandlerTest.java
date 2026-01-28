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
package com.vaadin.hilla.signals.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import com.vaadin.hilla.signals.internal.InternalSignal;
import com.vaadin.hilla.signals.internal.SecureSignalsRegistry;
import com.vaadin.signals.Id;
import com.vaadin.signals.shared.SharedNumberSignal;

public class SignalsHandlerTest {

    private static final String CLIENT_SIGNAL_ID_1 = "90000000-9000-9000-9000-900000000000";
    private static final String CLIENT_SIGNAL_ID_2 = "80000000-8000-8000-8000-800000000000";

    private final ObjectMapper mapper = new ObjectMapper();
    private SignalsHandler signalsHandler;
    private SecureSignalsRegistry signalsRegistry;

    @BeforeClass
    public static void setup() {
    }

    @AfterClass
    public static void tearDown() {
    }

    @Before
    public void setUp() {
        signalsRegistry = Mockito.mock(SecureSignalsRegistry.class);
        signalsHandler = new SignalsHandler(signalsRegistry);
    }

    @Test
    public void when_signalAlreadyRegistered_subscribe_returnsSubscriptionOfSameInstance()
            throws Exception {

        SharedNumberSignal numberSignal = new SharedNumberSignal();
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_1))
                .thenAnswer(invocation -> new InternalSignal(numberSignal,
                        new ObjectMapper()));
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_2))
                .thenAnswer(invocation -> new InternalSignal(numberSignal,
                        new ObjectMapper()));

        assertEquals(signalsRegistry.get(CLIENT_SIGNAL_ID_1).id(),
                signalsRegistry.get(CLIENT_SIGNAL_ID_2).id());

        var signalId = numberSignal.id();

        // first client subscribe to a signal, it registers the signal:
        Flux<JsonNode> firstFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_1, null);
        firstFlux.subscribe(next -> {
            assertNotNull(next);
            // Check the new format structure
            assertTrue(next.has("@type"));
            assertEquals("snapshot", next.get("@type").asText());
            assertTrue(next.has("commandId"));
            assertTrue(next.has("nodes"));
            assertTrue(next.get("nodes").has(""));
            assertTrue(next.get("nodes").get("").has("value"));
            assertEquals(0.0, next.get("nodes").get("").get("value").asDouble(),
                    0.0);
        }, error -> {
            throw new RuntimeException(error);
        });

        // another client subscribes to the same signal:
        Flux<JsonNode> secondFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_2, null);
        secondFlux.subscribe(next -> {
            assertNotNull(next);
            // Check the new format structure
            assertTrue(next.has("@type"));
            assertEquals("snapshot", next.get("@type").asText());
            assertTrue(next.has("commandId"));
            assertTrue(next.has("nodes"));
            assertTrue(next.get("nodes").has(""));
            assertTrue(next.get("nodes").get("").has("value"));
            assertEquals(0.0, next.get("nodes").get("").get("value").asDouble(),
                    0.0);
        }, error -> {
            throw new RuntimeException(error);
        });
    }

    @Test
    public void when_signalIsNotRegistered_update_throwsException() {
        var setCommand = new ObjectNode(mapper.getNodeFactory())
                .put("commandId", Id.random().asBase64())
                .put("targetNodeId", Id.random().asBase64()).put("@type", "set")
                .put("value", 0.0);
        assertThrows(IllegalStateException.class,
                () -> signalsHandler.update(CLIENT_SIGNAL_ID_1, setCommand));
    }

    @Test
    public void when_signalIsRegistered_update_notifiesTheSubscribers()
            throws Exception {
        SharedNumberSignal numberSignal = new SharedNumberSignal(10.0);
        var signalId = numberSignal.id();
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_1))
                .thenAnswer(invocation -> new InternalSignal(numberSignal,
                        new ObjectMapper()));
        Flux<JsonNode> firstFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_1, null);

        var setCommand = new ObjectNode(mapper.getNodeFactory())
                .put("commandId", Id.random().asBase64())
                .put("targetNodeId", signalId.asBase64()).put("@type", "set")
                .put("value", 42);
        signalsHandler.update(CLIENT_SIGNAL_ID_1, setCommand);

        var expectedUpdatedSignalEventJson = new ObjectNode(
                mapper.getNodeFactory()).put("@type", "snapshot");
        // Add the commandId field (we can't predict it since it's random)
        expectedUpdatedSignalEventJson.put("commandId", "");

        // Create the nodes structure
        var nodesObject = mapper.createObjectNode();
        var rootNodeObject = mapper.createObjectNode();
        rootNodeObject.put("@type", "d");
        rootNodeObject.putNull("parent");
        rootNodeObject.put("lastUpdate", "");
        rootNodeObject.putNull("scopeOwner");
        rootNodeObject.put("value", 42);
        rootNodeObject.set("listChildren", mapper.createArrayNode());
        rootNodeObject.set("mapChildren", mapper.createObjectNode());
        nodesObject.set("", rootNodeObject);
        expectedUpdatedSignalEventJson.set("nodes", nodesObject);

        StepVerifier.create(firstFlux).expectNextMatches(jsonNode -> {
            // Check the structure matches what we expect, ignoring dynamic
            // fields
            return jsonNode.has("@type")
                    && jsonNode.get("@type").asText().equals("snapshot")
                    && jsonNode.has("commandId") && jsonNode.has("nodes")
                    && jsonNode.get("nodes").has("")
                    && jsonNode.get("nodes").get("").has("value")
                    && jsonNode.get("nodes").get("").get("value")
                            .asDouble() == 42.0;
        }).thenCancel().verify();
    }

    @Test
    public void when_signalRegistryIsNull_anyInteraction_throwsException() {
        signalsHandler = new SignalsHandler(null);
        var exception = assertThrows(IllegalStateException.class,
                () -> signalsHandler.subscribe("endpoint", "method",
                        CLIENT_SIGNAL_ID_1, null));
        assertTrue(exception.getMessage().contains(
                "The Hilla Fullstack Signals API is currently considered experimental"));

        exception = assertThrows(IllegalStateException.class,
                () -> signalsHandler.update(CLIENT_SIGNAL_ID_1, null));
        assertTrue(exception.getMessage().contains(
                "The Hilla Fullstack Signals API is currently considered experimental"));
    }
}
