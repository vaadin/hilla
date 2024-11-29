package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.StateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Signal<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Signal.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final UUID id = UUID.randomUUID();

    private final Class<T> valueType;

    private final Set<Sinks.Many<ObjectNode>> subscribers = new HashSet<>();

    private final Signal<T> delegate;

    private Signal(Class<T> valueType, Signal<T> delegate) {
        this.valueType = Objects.requireNonNull(valueType);
        this.delegate = delegate;
    }

    public Signal(Class<T> valueType) {
        this(valueType, null);
    }

    protected Signal(Signal<T> delegate) {
        this(Objects.requireNonNull(delegate).getValueType(), delegate);
    }

    protected Signal<T> getDelegate() {
        return delegate;
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
     * Returns the signal value type.
     *
     * @return the value type
     */
    public Class<T> getValueType() {
        return valueType;
    }

    /**
     * Subscribes to the signal.
     *
     * @return a Flux of JSON events
     */
    public Flux<ObjectNode> subscribe() {
        if (delegate != null) {
            return delegate.subscribe();
        }
        Sinks.Many<ObjectNode> sink = Sinks.many().unicast()
                .onBackpressureBuffer();

        return sink.asFlux().doOnSubscribe(ignore -> {
            LOGGER.debug("New Flux subscription...");
            lock.lock();
            try {
                var snapshot = createSnapshotEvent();
                sink.tryEmitNext(snapshot);
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
     * Subscribes to an internal child signal with a specific signal id.
     *
     * @param signalId
     *            the internal signal id
     * @return a Flux of JSON events
     */
    public Flux<ObjectNode> subscribe(String signalId) {
        if (delegate != null) {
            return delegate.subscribe(signalId);
        }
        return subscribe();
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
            var processedEvent = StateEvent.isRejected(event) ? event
                    : processEvent(event);
            notifySubscribers(processedEvent);
        } finally {
            lock.unlock();
        }
    }

    private void notifySubscribers(ObjectNode processedEvent) {
        if (delegate != null) {
            delegate.notifySubscribers(processedEvent);
            return;
        }
        if (StateEvent.isRejected(processedEvent)) {
            LOGGER.warn(
                    "Operation with id '{}' is rejected with validator message: '{}'",
                    StateEvent.extractId(processedEvent),
                    StateEvent.extractValidationError(processedEvent));
            StateEvent.clearValidationError(processedEvent);
        }
        subscribers.removeIf(sink -> {
            boolean failure = sink.tryEmitNext(processedEvent).isFailure();
            if (failure) {
                LOGGER.debug("Failed push");
            }
            return failure;
        });
    }

    /**
     * Creates a snapshot event reflecting the current state of the signal.
     *
     * @return the snapshot event
     */
    protected abstract ObjectNode createSnapshotEvent();

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
    protected abstract ObjectNode processEvent(ObjectNode event);

    /**
     * Returns a read-only instance of the signal that rejects any attempt to
     * modify the signal value. The read-only signal, however, receives the same
     * updates as the original signal does.
     *
     * @return the read-only signal
     */
    public abstract Signal<T> asReadonly();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Signal<?> signal)) {
            return false;
        }
        return Objects.equals(getId(), signal.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /**
     * Sets the object mapper to be used for JSON serialization in Signals. This
     * is helpful for testing purposes. If not set, the default Hilla endpoint
     * object mapper is used.
     * <p>
     * <strong>Note:</strong> If a custom endpointMapperFactory bean defined
     * using the
     * {@code EndpointController.ENDPOINT_MAPPER_FACTORY_BEAN_QUALIFIER}
     * qualifier, the mapper from that factory is used also in Signals, and
     * there is no need to set it manually here.
     *
     * @param mapper
     *            the object mapper to be used in Signals
     */
    public static void setMapper(ObjectMapper mapper) {
        StateEvent.setMapper(mapper);
    }
}
