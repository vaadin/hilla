package com.vaadin.hilla.signals.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.signals.NumberSignal;
import com.vaadin.hilla.signals.core.SignalsRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

public class SignalsHandlerTest {

    private static final UUID CLIENT_SIGNAL_ID_1 = UUID
            .fromString("90000000-9000-9000-9000-900000000000");
    private static final UUID CLIENT_SIGNAL_ID_2 = UUID
            .fromString("80000000-8000-8000-8000-800000000000");

    private final ObjectMapper mapper = new ObjectMapper();
    private SignalsHandler signalsHandler;
    private SignalsRegistry signalsRegistry;
    private EndpointInvoker endpointInvoker;

    @Before
    public void setUp() {
        signalsRegistry = new SignalsRegistry();
        endpointInvoker = Mockito.mock(EndpointInvoker.class);
        signalsHandler = new SignalsHandler(signalsRegistry, endpointInvoker,
                mapper);
    }

    @Test
    public void when_signalAlreadyRegistered_subscribe_returnsSubscriptionOfSameInstance()
            throws Exception {

        NumberSignal numberSignal = new NumberSignal();
        var signalId = numberSignal.getId();
        when(endpointInvoker.invoke("endpoint", "method", null, null, null))
                .thenReturn(numberSignal);

        var expectedSignalEventJson = String
                .format("{\"value\":0.0,\"id\":\"%s\"}", signalId);

        // first client subscribe to a signal, it registers the signal:
        Flux<String> firstFlux = signalsHandler.subscribe("endpoint.method",
                CLIENT_SIGNAL_ID_1);
        firstFlux.subscribe(next -> {
            assertFalse(next.isEmpty());
            assertEquals(expectedSignalEventJson, next);
        }, error -> {
            throw new RuntimeException(error);
        });

        // another client subscribes to the same signal:
        Flux<String> secondFlux = signalsHandler.subscribe("endpoint.method",
                CLIENT_SIGNAL_ID_2);
        secondFlux.subscribe(next -> {
            assertFalse(next.isEmpty());
            assertEquals(expectedSignalEventJson, next);
        }, error -> {
            throw new RuntimeException(error);
        });

        assertEquals(signalsRegistry.get(CLIENT_SIGNAL_ID_1).getId(),
                signalsRegistry.get(CLIENT_SIGNAL_ID_2).getId());
    }

    @Test
    public void when_signalIsNotRegistered_update_throwsException() {
        assertThrows(IllegalStateException.class,
                () -> signalsHandler.update(CLIENT_SIGNAL_ID_1, "event"));
    }

    @Test
    public void when_signalIsRegistered_update_notifiesTheSubscribers()
            throws Exception {
        NumberSignal numberSignal = new NumberSignal(10.0);
        var signalId = numberSignal.getId();
        when(endpointInvoker.invoke("endpoint", "method", null, null, null))
                .thenReturn(numberSignal);

        Flux<String> firstFlux = signalsHandler.subscribe("endpoint.method",
                CLIENT_SIGNAL_ID_1);

        var event = String.format("""
                {
                    "id":"%s",
                    "type":"set",
                    "value":"42"
                }
                """, UUID.randomUUID());
        signalsHandler.update(CLIENT_SIGNAL_ID_1, event);

        var expectedUpdatedSignalEventJson = String.format("""
                {"value":42.0,"id":"%s"}""", signalId);
        StepVerifier.create(firstFlux)
                .expectNext(expectedUpdatedSignalEventJson).thenCancel()
                .verify();
    }
}
