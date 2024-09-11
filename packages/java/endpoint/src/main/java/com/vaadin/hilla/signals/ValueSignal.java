package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.StateEvent;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class ValueSignal<T> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ValueSignal.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final UUID id = UUID.randomUUID();

    private final Class<T> valueType;

    private final Set<Sinks.Many<ObjectNode>> subscribers = new HashSet<>();

    private T value;

    /**
     * Creates a new ValueSignal with the provided default value.
     *
     * @param defaultValue
     *            the default value, not <code>null</code>
     * @param valueType
     *            the value type class, not <code>null</code>
     * @throws NullPointerException
     *             if the default defaultValue or the valueType is
     *             <code>null</code>
     */
    public ValueSignal(T defaultValue, Class<T> valueType) {
        this(valueType);
        Objects.requireNonNull(defaultValue);
        this.value = defaultValue;
    }

    /**
     * Creates a new ValueSignal with provided valueType and <code>null</code>
     * as the default value.
     *
     * @param valueType
     *            the value type class, not <code>null</code>
     * @throws NullPointerException
     *             if the default defaultValue or the valueType is
     *             <code>null</code>
     */
    public ValueSignal(Class<T> valueType) {
        Objects.requireNonNull(valueType);
        this.valueType = valueType;
    }

    /**
     * Subscribes to the signal.
     *
     * @return a Flux of JSON events
     */
    public Flux<ObjectNode> subscribe() {
        Sinks.Many<ObjectNode> sink = Sinks.many().unicast()
                .onBackpressureBuffer();

        return sink.asFlux().doOnSubscribe(ignore -> {
            LOGGER.debug("New Flux subscription...");
            lock.lock();
            try {
                var currentValue = createStatusUpdateEvent(this.id.toString(),
                        StateEvent.EventType.SNAPSHOT);
                sink.tryEmitNext(currentValue);
                subscribers.add(sink);
            } finally {
                lock.unlock();
            }
        }).doFinally(ignore -> {
            lock.lock();
            try {
                LOGGER.debug("Unsubscribing from Signal...");
                subscribers.remove(sink);
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * Submits an event to the signal and notifies subscribers about the change
     * of the signal value.
     *
     * @param event
     *            the event to submit
     */
    public void submit(ObjectNode event) {
        lock.lock();
        try {
            boolean success = processEvent(event);
            // Notify subscribers
            subscribers.removeIf(sink -> {
                var updatedValue = createStatusUpdateEvent(
                        event.get("id").asText(),
                        success ? StateEvent.EventType.SNAPSHOT
                                : StateEvent.EventType.REJECT);
                boolean failure = sink.tryEmitNext(updatedValue).isFailure();
                if (failure) {
                    LOGGER.debug("Failed push");
                }
                return failure;
            });
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the signal UUID.
     *
     * @return the id
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Returns the signal's current value.
     *
     * @return the value
     */
    @Nullable
    public T getValue() {
        return this.value;
    }

    private ObjectNode createStatusUpdateEvent(String eventId,
            StateEvent.EventType eventType) {
        var snapshot = new StateEvent<>(eventId, eventType, this.value);
        return snapshot.toJson();
    }

    /**
     * Processes the event and updates the signal value if needed. Note that
     * this method is not thread-safe and should be called from a synchronized
     * context.
     *
     * @param event
     *            the event to process
     * @return <code>true</code> if the event was successfully processed and the
     *         signal value was updated, <code>false</code> otherwise.
     */
    protected boolean processEvent(ObjectNode event) {
        try {
            var stateEvent = new StateEvent<>(event, valueType);
            return switch (stateEvent.getEventType()) {
                case SET -> {
                    this.value = stateEvent.getValue();
                    yield true;
                }
                case REPLACE -> compareAndSet(stateEvent.getValue(),
                    stateEvent.getExpected());
                default -> throw new UnsupportedOperationException(
                    "Unsupported event: " + stateEvent.getEventType());
            };
        } catch (StateEvent.InvalidEventTypeException e) {
            throw new UnsupportedOperationException(
                    "Unsupported JSON: " + event, e);
        }
    }

    /**
     * Compares the current value with the expected value and updates the signal
     * value if they match. Note that this method is not thread-safe and should
     * be called from a synchronized context.
     *
     * @param newValue
     *            the new value to set
     * @param expectedValue
     *            the expected value
     * @return <code>true</code> if the value was successfully updated,
     *         <code>false</code> otherwise
     */
    protected boolean compareAndSet(T newValue, T expectedValue) {
        if (Objects.equals(this.value, expectedValue)) {
            this.value = newValue;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValueSignal<?> signal)) {
            return false;
        }
        return Objects.equals(getId(), signal.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
