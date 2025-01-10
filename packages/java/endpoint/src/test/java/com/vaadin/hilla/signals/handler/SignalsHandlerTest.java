package com.vaadin.hilla.signals.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.EndpointControllerMockBuilder;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.signals.NumberSignal;
import com.vaadin.hilla.signals.Signal;
import com.vaadin.hilla.signals.core.event.ListStateEvent;
import com.vaadin.hilla.signals.core.registry.SecureSignalsRegistry;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
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

        NumberSignal numberSignal = new NumberSignal();
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_1))
                .thenAnswer(invocation -> numberSignal);
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_2))
                .thenAnswer(invocation -> numberSignal);

        assertEquals(signalsRegistry.get(CLIENT_SIGNAL_ID_1).getId(),
                signalsRegistry.get(CLIENT_SIGNAL_ID_2).getId());

        var signalId = numberSignal.getId();
        var expectedSignalEventJson = new ObjectNode(mapper.getNodeFactory())
                .put("value", 0.0).put("id", signalId.toString())
                .put("type", "snapshot");

        // first client subscribe to a signal, it registers the signal:
        Flux<ObjectNode> firstFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_1, null, null);
        firstFlux.subscribe(next -> {
            assertNotNull(next);
            assertEquals(expectedSignalEventJson, next);
        }, error -> {
            throw new RuntimeException(error);
        });

        // another client subscribes to the same signal:
        Flux<ObjectNode> secondFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_2, null, null);
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
        when(signalsRegistry.get(CLIENT_SIGNAL_ID_1))
                .thenAnswer(invocation -> numberSignal);

        Flux<ObjectNode> firstFlux = signalsHandler.subscribe("endpoint",
                "method", CLIENT_SIGNAL_ID_1, null, null);

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
    public void when_parentClientSignalIdIsNotNull_andParentSignalExists_subscribe_returnsSubscription()
            throws Exception {
        String parentClientSignalId = "parent-signal-id";
        String clientSignalId = CLIENT_SIGNAL_ID_1;

        // Mock parent signal
        Signal parentSignal = Mockito.mock(Signal.class);

        // Mock registry.get(parentClientSignalId) to return parentSignal
        when(signalsRegistry.get(parentClientSignalId))
                .thenReturn(parentSignal);

        // Mock parentSignal.subscribe(clientSignalId) to return a
        // Flux<ObjectNode>
        ObjectNode expectedNode = mapper.createObjectNode();
        expectedNode.put("key", "value");
        Flux<ObjectNode> expectedFlux = Flux.just(expectedNode);
        when(parentSignal.subscribe(clientSignalId)).thenReturn(expectedFlux);

        // Call subscribe with parentClientSignalId not null
        Flux<ObjectNode> resultFlux = signalsHandler.subscribe("endpoint",
                "method", clientSignalId, null, parentClientSignalId);

        // Verify that unsubscribe is called upon completion
        StepVerifier.create(resultFlux).expectNext(expectedNode).thenCancel()
                .verify();

        // Verify that registry.unsubscribe(clientSignalId) is called
        Mockito.verify(signalsRegistry).unsubscribe(clientSignalId);
    }

    @Test
    public void when_parentClientSignalIdIsNotNull_andParentSignalDoesNotExist_subscribe_returnsErrorFlux()
            throws Exception {
        String parentClientSignalId = "parent-signal-id";
        String clientSignalId = CLIENT_SIGNAL_ID_1;

        // Mock registry.get(parentClientSignalId) to return null
        when(signalsRegistry.get(parentClientSignalId)).thenReturn(null);

        // Call subscribe with parentClientSignalId not null
        Flux<ObjectNode> resultFlux = signalsHandler.subscribe("endpoint",
                "method", clientSignalId, null, parentClientSignalId);

        // Verify that the resultFlux emits an error
        StepVerifier.create(resultFlux).expectErrorMatches(
                throwable -> throwable instanceof IllegalStateException
                        && throwable.getMessage().contains(
                                "Parent Signal not found for parent client signal id: "
                                        + parentClientSignalId))
                .verify();

        // Ensure that unsubscribe is not called since subscription did not
        // succeed
        Mockito.verify(signalsRegistry, Mockito.never())
                .unsubscribe(clientSignalId);
    }

    @Test
    public void when_parentSignalIdIsNotNull_andParentSignalExists_update_callsSubmitOnParentSignal()
            throws Exception {
        String parentSignalId = "parent-signal-id";

        // Create an event ObjectNode with parentSignalId
        ObjectNode event = mapper.createObjectNode();
        event.put(ListStateEvent.Field.PARENT_SIGNAL_ID, parentSignalId);
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "someType");

        // Mock registry.get(parentSignalId) to return a mock parentSignal
        Signal parentSignal = Mockito.mock(Signal.class);
        when(signalsRegistry.get(parentSignalId)).thenReturn(parentSignal);

        // Call update
        signalsHandler.update(CLIENT_SIGNAL_ID_1, event);

        // Verify that parentSignal.submit(event) was called
        Mockito.verify(parentSignal).submit(event);
    }

    @Test
    public void when_parentSignalIdIsNotNull_andParentSignalDoesNotExist_update_throwsException()
            throws Exception {
        String parentSignalId = "parent-signal-id";

        // Create an event ObjectNode with parentSignalId
        ObjectNode event = mapper.createObjectNode();
        event.put(ListStateEvent.Field.PARENT_SIGNAL_ID, parentSignalId);
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "someType");

        // Mock registry.get(parentSignalId) to return null
        when(signalsRegistry.get(parentSignalId)).thenReturn(null);

        // Call update and expect an IllegalStateException
        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> {
                    signalsHandler.update(CLIENT_SIGNAL_ID_1, event);
                });

        // Verify the exception message
        String expectedMessage = "Parent Signal not found for signal id: "
                + parentSignalId;
        assertEquals(expectedMessage, exception.getMessage());

        // Ensure that no interaction with clientSignalId's signal occurred
        Mockito.verify(signalsRegistry, Mockito.never())
                .get(CLIENT_SIGNAL_ID_1);
    }

    @Test
    public void when_signalRegistryIsNull_anyInteraction_throwsException() {
        signalsHandler = new SignalsHandler(null);
        var exception = assertThrows(IllegalStateException.class,
                () -> signalsHandler.subscribe("endpoint", "method",
                        CLIENT_SIGNAL_ID_1, null, null));
        assertTrue(exception.getMessage().contains(
                "The Hilla Fullstack Signals API is currently considered experimental"));

        exception = assertThrows(IllegalStateException.class,
                () -> signalsHandler.update(CLIENT_SIGNAL_ID_1, null));
        assertTrue(exception.getMessage().contains(
                "The Hilla Fullstack Signals API is currently considered experimental"));
    }
}
