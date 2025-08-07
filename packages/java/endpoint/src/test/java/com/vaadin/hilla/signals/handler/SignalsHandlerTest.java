package com.vaadin.hilla.signals.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.internal.InternalSignal;
import com.vaadin.hilla.signals.internal.SecureSignalsRegistry;
import com.vaadin.signals.NumberSignal;
import com.vaadin.signals.SignalEnvironment;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class SignalsHandlerTest {

    private static final String CLIENT_SIGNAL_ID_1 = "90000000-9000-9000-9000-900000000000";
    private static final String CLIENT_SIGNAL_ID_2 = "80000000-8000-8000-8000-800000000000";

    private final ObjectMapper mapper = new ObjectMapper();
    private SignalsHandler signalsHandler;
    private SecureSignalsRegistry signalsRegistry;

    @BeforeClass
    public static void setup() {
        SignalEnvironment.tryInitialize(new ObjectMapper(), Runnable::run);
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

        NumberSignal numberSignal = new NumberSignal();
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_1))
                .thenAnswer(invocation -> new InternalSignal(numberSignal));
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_2))
                .thenAnswer(invocation -> new InternalSignal(numberSignal));

        assertEquals(signalsRegistry.get(CLIENT_SIGNAL_ID_1).id(),
                signalsRegistry.get(CLIENT_SIGNAL_ID_2).id());

        var signalId = numberSignal.id();
        var expectedSignalEventJson = new ObjectNode(mapper.getNodeFactory())
                .put("value", 0.0).put("id", signalId.toString())
                .put("type", "snapshot");

        // first client subscribe to a signal, it registers the signal:
        Flux<JsonNode> firstFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_1, null);
        firstFlux.subscribe(next -> {
            assertNotNull(next);
            assertEquals(expectedSignalEventJson, next);
        }, error -> {
            throw new RuntimeException(error);
        });

        // another client subscribes to the same signal:
        Flux<JsonNode> secondFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_2, null);
        secondFlux.subscribe(next -> {
            assertNotNull(next);
            assertEquals(expectedSignalEventJson, next);
        }, error -> {
            throw new RuntimeException(error);
        });
    }

    @Test
    public void when_signalIsNotRegistered_update_throwsException() {
        var setEvent = new ObjectNode(mapper.getNodeFactory()).put("value", 0.0)
                .put("id", UUID.randomUUID().toString()).put("type", "set");
        assertThrows(IllegalStateException.class,
                () -> signalsHandler.update(CLIENT_SIGNAL_ID_1, setEvent));
    }

    @Test
    public void when_signalIsRegistered_update_notifiesTheSubscribers()
            throws Exception {
        NumberSignal numberSignal = new NumberSignal(10.0);
        var signalId = numberSignal.id();
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_1))
                .thenAnswer(invocation -> new InternalSignal(numberSignal));
        Flux<JsonNode> firstFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_1, null);

        var setEvent = new ObjectNode(mapper.getNodeFactory()).put("value", 42)
                .put("id", UUID.randomUUID().toString()).put("type", "set");
        signalsHandler.update(CLIENT_SIGNAL_ID_1, setEvent);

        var expectedUpdatedSignalEventJson = new ObjectNode(
                mapper.getNodeFactory()).put("value", 42.0)
                .put("id", signalId.toString()).put("type", "snapshot")
                .put("accepted", true);
        StepVerifier.create(firstFlux)
                .expectNext(expectedUpdatedSignalEventJson).thenCancel()
                .verify();
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
