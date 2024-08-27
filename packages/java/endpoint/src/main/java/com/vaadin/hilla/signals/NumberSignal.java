package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.vaadin.hilla.signals.core.StateEvent;
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

/**
 * A signal that holds a number value.
 */
public class NumberSignal {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NumberSignal.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final UUID id = UUID.randomUUID();

    private Double value;

    private final Set<Sinks.Many<ObjectNode>> subscribers = new HashSet<>();

    /**
     * Creates a new NumberSignal with the provided default value.
     *
     * @param defaultValue
     *            the default value
     */
    public NumberSignal(@Nullable Double defaultValue) {
        this.value = defaultValue;
    }

    /**
     * Creates a new NumberSignal with the default value of 0.
     */
    public NumberSignal() {
        this(0.0);
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
                var currentValue = createSnapshot();
                sink.tryEmitNext(currentValue);
                subscribers.add(sink);
            } finally {
                lock.unlock();
            }
        }).doFinally(ignore -> {
            lock.lock();
            try {
                LOGGER.debug("Unsubscribing from NumberSignal...");
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
            processEvent(event);
            // Notify subscribers
            subscribers.removeIf(sink -> {
                var updatedValue = createSnapshot();
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
    public Double getValue() {
        return this.value;
    }

    private ObjectNode createSnapshot() {
        var snapshot = new StateEvent<>(this.id.toString(),
                StateEvent.EventType.SNAPSHOT, this.value);
        return snapshot.toJson();
    }

    private void processEvent(ObjectNode event) {
        try {
            var stateEvent = new StateEvent<Double>(event);
            if (isSetEvent(stateEvent)) {
                this.value = stateEvent.getValue();
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported event: " + event);
            }
        } catch (StateEvent.InvalidEventTypeException e) {
            throw new UnsupportedOperationException(
                    "Unsupported JSON: " + event, e);
        }
    }

    private boolean isSetEvent(StateEvent<?> event) {
        return StateEvent.EventType.SET.equals(event.getEventType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NumberSignal signal))
            return false;
        return Objects.equals(getId(), signal.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
