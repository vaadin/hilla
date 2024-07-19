package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.vaadin.hilla.signals.core.JsonEvent;

/**
 * A signal that holds a number value.
 */
public class NumberSignal {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NumberSignal.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final ObjectMapper mapper;

    private final UUID id = UUID.randomUUID();

    private Double value;

    private final Set<Sinks.Many<JsonEvent>> subscribers = new HashSet<>();

    /**
     * Creates a new NumberSignal with the provided default value.
     *
     * @param defaultValue
     *            the default value
     */
    public NumberSignal(@Nullable Double defaultValue) {
        this.value = defaultValue;
        this.mapper = new ObjectMapper();
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
    public Flux<JsonEvent> subscribe() {
        Sinks.Many<JsonEvent> sink = Sinks.many().multicast()
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
    public void submit(JsonEvent event) {
        lock.lock();
        try {
            processEvent(event);
            // Notify subscribers
            subscribers.removeIf(sink -> {
                JsonEvent updatedValue = createSnapshot();
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

    private JsonEvent createSnapshot() {
        ObjectNode entryNode = mapper.createObjectNode();
        entryNode.set("value", new DoubleNode(value));
        return new JsonEvent(id, entryNode);
    }

    private void processEvent(JsonEvent event) {
        ObjectNode json = event.getJson();

        handleCommand(json);
    }

    private void handleCommand(ObjectNode json) {
        if (isSetEvent(json)) {
            this.value = Double.valueOf(json.get("value").asText());
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported JSON: " + json);
        }
    }

    private boolean isSetEvent(ObjectNode json) {
        return json.has("type") && json.get("type").asText().equals("set");
    }
}
