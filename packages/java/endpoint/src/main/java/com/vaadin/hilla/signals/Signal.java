package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Objects;
import java.util.UUID;

public abstract class Signal<T> {

    private final UUID id = UUID.randomUUID();

    private final Class<T> valueType;

    private final Sinks.Many<ObjectNode> mainSink = Sinks.many().replay()
            .limit(1);

    public Signal(Class<T> valueType) {
        this.valueType = Objects.requireNonNull(valueType);
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
        return mainSink.asFlux().cache().onBackpressureBuffer();
    }

    /**
     * Subscribes to an internal child signal with a specific signal id.
     *
     * @param signalId
     *            the internal signal id
     * @return a Flux of JSON events
     */
    public Flux<ObjectNode> subscribe(String signalId) {
        return subscribe();
    }

    /**
     * Submits an event to the signal and notifies subscribers about the change
     * of the signal value.
     *
     * @param event
     *            the event to submit
     */
    public synchronized void submit(ObjectNode event) {
        var processedEvent = processEvent(event);
        mainSink.tryEmitNext(processedEvent);
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
}
