package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.EndpointControllerMockBuilder;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.signals.core.event.StateEvent;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.vaadin.hilla.signals.internal.InternalSignal;
import com.vaadin.signals.Id;
import com.vaadin.signals.SignalEnvironment;
import com.vaadin.signals.ValueSignal;

public class NumberSignalTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void setup() {
        var appCtx = Mockito.mock(ApplicationContext.class);
        var endpointObjectMapper = EndpointControllerMockBuilder
                .createEndpointObjectMapper(appCtx,
                        new JacksonObjectMapperFactory.Json());
        SignalEnvironment.tryInitialize(endpointObjectMapper, Runnable::run);
    }

    @AfterClass
    public static void tearDown() {
    }

    @Test
    public void constructor_withValueArg_usesValueAsDefaultValue() {
        var signal = new ValueSignal<>(42.0);

        assertEquals(42.0, signal.peek(), 0.0);
    }

    @Test
    public void constructor_withoutValueArg_usesZeroAsDefaultValue() {
        var signal = new ValueSignal<>(Double.class);

        assertNull(signal.peek());
    }

    @Test
    public void constructor_withValueArg_doesNotAcceptNull() {
        assertThrows(NullPointerException.class,
                () -> new ValueSignal<>((Double) null));
    }

    @Test
    public void getId_returns_not_null() {
        var signal1 = new ValueSignal<>(Double.class);
        assertNotNull(signal1.id());

        var signal3 = new ValueSignal<>(42.0);
        assertNotNull(signal3.id());
    }

    @Test
    public void subscribe_returns_flux() {
        var signal = new ValueSignal<>(Double.class);
        var clientSignalId = Id.random().toString();
        var internalSignal = new InternalSignal(signal);

        Flux<JsonNode> flux = internalSignal.subscribe(clientSignalId);

        assertNotNull(flux);
    }

    @Test
    public void subscribe_returns_flux_withJsonEvents() {
        var signal = new ValueSignal<>(Double.class);
        var clientSignalId = Id.random().toString();
        var internalSignal = new InternalSignal(signal);

        Flux<JsonNode> flux = internalSignal.subscribe(clientSignalId);

        flux.subscribe(Assert::assertNotNull);
    }

    @Test
    public void submit_notifies_subscribers() {
        var signal = new ValueSignal<>(0.0);
        var clientSignalId = Id.random().toString();
        var internalSignal = new InternalSignal(signal);

        Flux<JsonNode> flux = internalSignal.subscribe(clientSignalId);

        var counter = new AtomicInteger(0);
        flux.subscribe(json -> {
            assertNotNull(json);
            var eventJson = (ObjectNode) json;
            var stateEvent = new StateEvent<>(eventJson, Double.class);
            if (counter.get() == 0) {
                // notification for the initial value
                assertEquals(0.0, stateEvent.getValue(), 0.0);
                assertTrue(StateEvent.isAccepted(eventJson));
            } else if (counter.get() == 1) {
                assertEquals(42.0, stateEvent.getValue(), 0.0);
            }
            counter.incrementAndGet();
        });

        internalSignal.submit(clientSignalId, createSetEvent("42"));
    }

    @Test
    public void submit_eventWithUnknownCommand_throws() {
        var signal = new ValueSignal<>(0.0);
        var clientSignalId = Id.random().toString();
        var internalSignal = new InternalSignal(signal);

        var exception = assertThrows(UnsupportedOperationException.class,
                () -> internalSignal.submit(clientSignalId,
                        createUnknownCommandEvent()));
        assertTrue(exception.getMessage().startsWith("Unsupported JSON: "));
    }

    @Test
    public void submit_eventWithIncrementCommand_incrementsValue() {
        var signal = new ValueSignal<>(42.0);
        var clientSignalId = Id.random().toString();
        var internalSignal = new InternalSignal(signal);

        internalSignal.submit(clientSignalId, createIncrementEvent("2"));
        assertEquals(44.0, signal.peek(), 0.0);

        internalSignal.submit(clientSignalId, createIncrementEvent("-5.5"));
        assertEquals(38.5, signal.peek(), 0.0);
    }

    // TODO: These validation tests are disabled because the external
    // ValueSignal library
    // doesn't include the validation API (IncrementOperation, ValidationResult,
    // etc.)

    /*
     * @Test public void
     * incrementOperationValidated_originalInstanceIsNotRestricted() {
     * NumberSignal counter = new NumberSignal(42.0); NumberSignal
     * limitedCounter = counter.withOperationValidator(op -> { if (op instanceof
     * IncrementOperation increment && increment.value() > 1) { return
     * ValidationResult .reject("Only increment by 1 is allowed"); } return
     * ValidationResult.allow(); });
     *
     * counter.submit(createIncrementEvent("2")); assertEquals(44.0,
     * counter.getValue(), 0.0);
     *
     * // the restricted instance sees the same value as the original one:
     * assertEquals(44.0, limitedCounter.getValue(), 0.0);
     *
     * // the restricted instance doesn't allow replace operation:
     * limitedCounter.submit(createIncrementEvent("2")); assertEquals(44.0,
     * limitedCounter.getValue(), 0.0); }
     *
     * @Test public void incrementOperationValidated_subscriptionWorks() {
     * NumberSignal number = new NumberSignal(42.0); NumberSignal limitedNumber
     * = number .withOperationValidator(operation -> { if (operation instanceof
     * IncrementOperation increment && increment.value() > 1) { return
     * ValidationResult .reject("Only increment by 1 is allowed"); } return
     * ValidationResult.allow(); });
     *
     * Flux<ObjectNode> numberFlux = number.subscribe(); AtomicInteger
     * numberCounter = new AtomicInteger(0); numberFlux.subscribe(eventJson ->
     * numberCounter.incrementAndGet()); assertEquals(1, numberCounter.get());
     * // initial state
     *
     * Flux<ObjectNode> limitedNumberFlux = limitedNumber.subscribe();
     * AtomicInteger limitedNumberCounter = new AtomicInteger(0);
     * limitedNumberFlux .subscribe(eventJson ->
     * limitedNumberCounter.incrementAndGet()); assertEquals(1,
     * limitedNumberCounter.get()); // initial state
     *
     * number.submit(createIncrementEvent("5")); assertEquals(2,
     * numberCounter.get()); assertEquals(2, limitedNumberCounter.get());
     *
     * number.submit(createIncrementEvent("3")); assertEquals(3,
     * numberCounter.get()); assertEquals(3, limitedNumberCounter.get()); }
     *
     * @Test public void multipleValidators_allValidatorsAreApplied() {
     * NumberSignal partiallyRestrictedSignal1 = new NumberSignal(42.0)
     * .withOperationValidator(op -> { if (op instanceof IncrementOperation
     * increment && increment.value() % 2 == 0) { return
     * ValidationResult.reject( "Increment by multiples of 2 is not allowed"); }
     * return ValidationResult.allow(); }); NumberSignal
     * partiallyRestrictedSignal2 = partiallyRestrictedSignal1
     * .withOperationValidator(op -> { if (op instanceof IncrementOperation
     * increment && increment.value() % 3 == 0) { return
     * ValidationResult.reject( "Increment by multiples of 3 is not allowed"); }
     * return ValidationResult.allow(); }); NumberSignal fullyRestrictedSignal =
     * partiallyRestrictedSignal2 .withOperationValidator(op -> { if (op
     * instanceof IncrementOperation) { return ValidationResult
     * .reject("No increment is allowed"); } return ValidationResult.allow();
     * });
     *
     * // allowed: partiallyRestrictedSignal1.submit(createIncrementEvent("1"));
     * // allowed: partiallyRestrictedSignal2.submit(createIncrementEvent("5"));
     * // not allowed:
     * partiallyRestrictedSignal2.submit(createIncrementEvent("6"));
     *
     * assertEquals(48.0, fullyRestrictedSignal.getValue(), 0.0);
     * assertEquals(48.0, partiallyRestrictedSignal1.getValue(), 0.0);
     *
     * fullyRestrictedSignal.submit(createIncrementEvent("1"));
     * assertEquals(48.0, fullyRestrictedSignal.getValue(), 0.0);
     * assertEquals(48.0, partiallyRestrictedSignal1.getValue(), 0.0); }
     *
     * @Test public void withOperationValidator_throws_whenValidatorIsNull() {
     * assertThrows(NullPointerException.class, () -> new
     * NumberSignal(42.0).withOperationValidator(null)); }
     *
     * @Test public void readonlyInstance_doesNotAllowAnyModifications() {
     * NumberSignal signal = new NumberSignal(42.0); NumberSignal readonlySignal
     * = signal.asReadonly();
     *
     * readonlySignal.submit(createIncrementEvent("2")); assertEquals(42.0,
     * readonlySignal.getValue(), 0.0);
     *
     * readonlySignal.submit(createSetEvent("5")); assertEquals(42.0,
     * readonlySignal.getValue(), 0.0); }
     */

    private ObjectNode createIncrementEvent(String value) {
        var setEvent = new StateEvent<>(UUID.randomUUID().toString(),
                StateEvent.EventType.INCREMENT, Double.parseDouble(value));
        return setEvent.toJson();
    }

    private ObjectNode createSetEvent(String value) {
        var setEvent = new StateEvent<>(UUID.randomUUID().toString(),
                StateEvent.EventType.SET, Double.parseDouble(value));
        return setEvent.toJson();
    }

    private ObjectNode createUnknownCommandEvent() {
        var unknown = mapper.createObjectNode();
        unknown.put(StateEvent.Field.ID, UUID.randomUUID().toString());
        unknown.put("increase", "2");
        unknown.put(StateEvent.Field.VALUE, "42");
        return unknown;
    }
}
