package com.vaadin.hilla.signals.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.EndpointRegistry;
import com.vaadin.hilla.signals.NumberSignal;
import com.vaadin.hilla.signals.core.registry.SecureSignalsRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

public class SignalsHandlerTest {

    private static final String CLIENT_SIGNAL_ID_1 = "90000000-9000-9000-9000-900000000000";
    private static final String CLIENT_SIGNAL_ID_2 = "80000000-8000-8000-8000-800000000000";

    private final ObjectMapper mapper = new ObjectMapper();
    private SignalsHandler signalsHandler;
    private SecureSignalsRegistry signalsRegistry;

    @Before
    public void setUp() {
        signalsRegistry = Mockito.mock(SecureSignalsRegistry.class);
        signalsHandler = new SignalsHandler(signalsRegistry);
    }

    @Test
    public void when_signalAlreadyRegistered_subscribe_returnsSubscriptionOfSameInstance()
            throws Exception {

        NumberSignal numberSignal = new NumberSignal();
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_1)).thenReturn(numberSignal);
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_2)).thenReturn(numberSignal);

        assertEquals(signalsRegistry.get(CLIENT_SIGNAL_ID_1).getId(),
                signalsRegistry.get(CLIENT_SIGNAL_ID_2).getId());

        var signalId = numberSignal.getId();
        var expectedSignalEventJson = new ObjectNode(mapper.getNodeFactory())
                .put("value", 0.0).put("id", signalId.toString())
                .put("type", "snapshot");

        // first client subscribe to a signal, it registers the signal:
        Flux<ObjectNode> firstFlux = signalsHandler.subscribe("endpoint.method",
                CLIENT_SIGNAL_ID_1);
        firstFlux.subscribe(next -> {
            assertNotNull(next);
            assertEquals(expectedSignalEventJson, next);
        }, error -> {
            throw new RuntimeException(error);
        });

        // another client subscribes to the same signal:
        Flux<ObjectNode> secondFlux = signalsHandler
                .subscribe("endpoint.method", CLIENT_SIGNAL_ID_2);
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
        var signalId = numberSignal.getId();
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_1)).thenReturn(numberSignal);

        Flux<ObjectNode> firstFlux = signalsHandler.subscribe("endpoint.method",
                CLIENT_SIGNAL_ID_1);

        var setEvent = new ObjectNode(mapper.getNodeFactory()).put("value", 42)
                .put("id", UUID.randomUUID().toString()).put("type", "set");
        signalsHandler.update(CLIENT_SIGNAL_ID_1, setEvent);

        var expectedUpdatedSignalEventJson = new ObjectNode(
                mapper.getNodeFactory()).put("value", 42.0)
                .put("id", signalId.toString()).put("type", "snapshot");
        StepVerifier.create(firstFlux)
                .expectNext(expectedUpdatedSignalEventJson).thenCancel()
                .verify();
    }

    @Test
    public void when_unsubscribedFromSignal_noMoreUpdatesAreReceived()
            throws Exception {
        NumberSignal numberSignal = new NumberSignal(10.0);
        var signalId = numberSignal.getId();
        var endpointInvokerMocked = mockEndpointInvokerThatGrantsAccess(
                numberSignal);
        signalsRegistry = new SecureSignalsRegistry(endpointInvokerMocked);
        signalsHandler = new SignalsHandler(signalsRegistry);

        // verify that updates are received when subscribed:
        Flux<ObjectNode> firstFlux = signalsHandler.subscribe("endpoint.method",
                CLIENT_SIGNAL_ID_1);
        Flux<ObjectNode> secondFlux = signalsHandler
                .subscribe("endpoint.method", CLIENT_SIGNAL_ID_2);

        var firstFluxEmittedEvents = new ArrayList<>();
        firstFlux.subscribe(firstFluxEmittedEvents::add);
        var secondFluxEmittedEvents = new ArrayList<>();
        secondFlux.subscribe(secondFluxEmittedEvents::add);

        var setEvent = new ObjectNode(mapper.getNodeFactory()).put("value", 42)
                .put("id", UUID.randomUUID().toString()).put("type", "set");

        var expectedUpdateEventJson1 = new ObjectNode(mapper.getNodeFactory())
                .put("value", 10.0).put("id", signalId.toString())
                .put("type", "snapshot");
        var expectedUpdateEventJson2 = new ObjectNode(mapper.getNodeFactory())
                .put("value", 42.0).put("id", signalId.toString())
                .put("type", "snapshot");

        // first client update the signal:
        signalsHandler.update(CLIENT_SIGNAL_ID_1, setEvent);

        // second client unsubscribes:
        signalsHandler.unsubscribe(CLIENT_SIGNAL_ID_2);

        // first client update the signal again:
        signalsHandler.update(CLIENT_SIGNAL_ID_1, setEvent);

        StepVerifier.create(Flux.fromIterable(firstFluxEmittedEvents))
                .expectNext(expectedUpdateEventJson1)
                .expectNext(expectedUpdateEventJson2).thenCancel().verify();
        StepVerifier.create(Flux.fromIterable(secondFluxEmittedEvents))
                .expectNext(expectedUpdateEventJson1)
                .expectNoEvent(Duration.ofSeconds(5)).thenCancel().verify();

        assertThrows(IllegalStateException.class,
                () -> signalsHandler.update(CLIENT_SIGNAL_ID_2, null));

        // first client also unsubscribes:
        signalsHandler.unsubscribe(CLIENT_SIGNAL_ID_1);

        assertThrows(IllegalStateException.class,
                () -> signalsHandler.update(CLIENT_SIGNAL_ID_1, null));
    }

    private EndpointInvoker mockEndpointInvokerThatGrantsAccess(
            NumberSignal signal) throws Exception {
        EndpointInvoker invoker = Mockito.mock(EndpointInvoker.class);
        when(invoker.invoke(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(signal);
        fakeMethodExistenceOn(invoker);
        return invoker;
    }

    private void fakeMethodExistenceOn(EndpointInvoker invoker)
            throws Exception {
        EndpointRegistry.VaadinEndpointData mockVaadinEndpointData = Mockito
                .mock(EndpointRegistry.VaadinEndpointData.class);
        Method mockMethod = this.getClass().getMethod("aFakeMethod");
        when(mockVaadinEndpointData.getMethod(Mockito.anyString()))
                .thenReturn(Optional.of(mockMethod));
        when(invoker.getVaadinEndpointData(Mockito.anyString()))
                .thenReturn(mockVaadinEndpointData);
    }

    public void aFakeMethod() {
    }
}
