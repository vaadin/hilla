package com.vaadin.hilla.signals.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.Nullable;
import com.vaadin.hilla.signals.core.JsonEventMapper;
import com.vaadin.hilla.signals.core.SignalsRegistry;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Handler Endpoint for Fullstack Signals' subscription and update events.
 */
@AnonymousAllowed
@BrowserCallable
public class SignalsHandler {

    private final SignalsRegistry registry;
    private final JsonEventMapper jsonEventMapper;

    public SignalsHandler(SignalsRegistry registry, ObjectMapper mapper) {
        this.registry = registry;
        this.jsonEventMapper = new JsonEventMapper(mapper);
    }

    /**
     * Subscribes to a signal.
     *
     * @param signalId
     *            the signal to subscribe to
     * @param continueFrom
     *            the event to continue from
     * @return a Flux of JSON events
     */
    public Flux<String> subscribe(UUID signalId, @Nullable UUID continueFrom) {
        if (!registry.contains(signalId)) {
            throw new IllegalStateException("Signal not found: " + signalId);
        }
        return registry.get(signalId).subscribe().map(jsonEventMapper::toJson);
    }

    /**
     * Updates a signal with an event.
     *
     * @param signalId
     *            the signal to update
     * @param event
     *            the event to update with
     */
    public void update(UUID signalId, String event) {
        if (!registry.contains(signalId)) {
            throw new IllegalStateException("Signal not found: " + signalId);
        }
        registry.get(signalId).submit(jsonEventMapper.fromJson(event));
    }
}
