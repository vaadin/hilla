package com.vaadin.hilla.signals.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.EndpointControllerMockBuilder;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.signals.Signal;

import org.junit.*;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.vaadin.hilla.signals.internal.InternalSignal;
import com.vaadin.hilla.signals.internal.SecureSignalsRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.signals.Id;

public class SignalsHandlerTest {

    private static final String CLIENT_SIGNAL_ID_1 = "90000000-9000-9000-9000-900000000000";
    private static final String CLIENT_SIGNAL_ID_2 = "80000000-8000-8000-8000-800000000000";

    private final ObjectMapper mapper = new ObjectMapper();
    private SignalsHandler signalsHandler;
    private SecureSignalsRegistry signalsRegistry;

    @BeforeClass
    public static void setup() {
        var appCtx = Mockito.mock(ApplicationContext.class);
        var endpointObjectMapper = EndpointControllerMockBuilder
                .createEndpointObjectMapper(appCtx,
                        new JacksonObjectMapperFactory.Json());
        Signal.setMapper(endpointObjectMapper);
    }

    @AfterClass
    public static void tearDown() {
        Signal.setMapper(null);
    }

    @Before
    public void setUp() {
        signalsRegistry = Mockito.mock(SecureSignalsRegistry.class);
        signalsHandler = new SignalsHandler(signalsRegistry);
    }

    @Test
    public void when_signalAlreadyRegistered_subscribe_returnsSubscriptionOfSameInstance()
            throws Exception {
        InternalSignal internalSignal = Mockito.mock(InternalSignal.class);
        Mockito.when(internalSignal.id()).thenReturn(Id.random());
        Mockito.when(signalsRegistry.get(CLIENT_SIGNAL_ID_1))
                .thenReturn(internalSignal);
        Mockito.when(signalsRegistry.get(CLIENT_SIGNAL_ID_2))
                .thenReturn(internalSignal);

        assertEquals(signalsRegistry.get(CLIENT_SIGNAL_ID_1).id(),
                signalsRegistry.get(CLIENT_SIGNAL_ID_2).id());

        var signalId = internalSignal.id();
        var expectedSignalEventJson = new ObjectNode(mapper.getNodeFactory())
                .put("value", 0.0).put("id", signalId.toString())
                .put("type", "snapshot");

        // Mock subscribe to emit expected event
        Flux<JsonNode> mockFlux = Flux.just(expectedSignalEventJson);
        Mockito.when(internalSignal.subscribe(CLIENT_SIGNAL_ID_1))
                .thenReturn(mockFlux);
        Mockito.when(internalSignal.subscribe(CLIENT_SIGNAL_ID_2))
                .thenReturn(mockFlux);

        Flux<JsonNode> firstFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_1, null);
        StepVerifier.create(firstFlux).assertNext(next -> {
            assertNotNull(next);
            assertEquals(expectedSignalEventJson, next);
        }).thenCancel().verify();

        Flux<JsonNode> secondFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_2, null);
        StepVerifier.create(secondFlux).assertNext(next -> {
            assertNotNull(next);
            assertEquals(expectedSignalEventJson, next);
        }).thenCancel().verify();
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
        InternalSignal internalSignal = Mockito.mock(InternalSignal.class);
        var signalId = Mockito.mock(Id.class);
        Mockito.when(internalSignal.id()).thenReturn(signalId);
        Mockito.when(signalsRegistry.get(CLIENT_SIGNAL_ID_1))
                .thenReturn(internalSignal);

        var expectedUpdatedSignalEventJson = new ObjectNode(
                mapper.getNodeFactory()).put("value", 42.0)
                .put("id", signalId.toString()).put("type", "snapshot")
                .put("accepted", true);
        Flux<JsonNode> mockFlux = Flux.just(expectedUpdatedSignalEventJson);
        Mockito.when(internalSignal.subscribe(CLIENT_SIGNAL_ID_1))
                .thenReturn(mockFlux);

        Flux<JsonNode> firstFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_1, null);

        var setEvent = new ObjectNode(mapper.getNodeFactory()).put("value", 42)
                .put("id", UUID.randomUUID().toString()).put("type", "set");
        signalsHandler.update(CLIENT_SIGNAL_ID_1, setEvent);

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
