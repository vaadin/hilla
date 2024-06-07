package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashSet;
import java.util.Objects;
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

    public NumberSignal(Double defaultValue) {
        this.value = defaultValue;
        this.mapper = new ObjectMapper();
    }

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

    public UUID getId() {
        return this.id;
    }

    public Double getValue() {
        return this.value;
    }

    private JsonEvent createSnapshot() {
        ObjectNode entryNode =  mapper.createObjectNode();
        entryNode.set("value", new DoubleNode(value));
        return new JsonEvent(id, entryNode);
    }

    private void processEvent(JsonEvent event) {
        ObjectNode json = event.getJson();

        if (!checkConditions(json)) {
            return;
        }

        handleCommand(json);
    }

    private boolean checkConditions(ObjectNode json) {
        if (json.has("conditions")) {
            ArrayNode conditions = (ArrayNode) json.get("conditions");
            for (int i = 0; i < conditions.size(); i++) {
                JsonNode condition = conditions.get(i);

                if (condition.has("value") && !Objects.equals(
                        Double.valueOf(condition.get("value").asText()),
                        this.value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleCommand(ObjectNode json) {
        if (json.has("set")) {
            this.value = Double.valueOf(json.get("value").asText());
        } else {
            throw new RuntimeException("Unsupported JSON: " + json.toString());
        }
    }
}
