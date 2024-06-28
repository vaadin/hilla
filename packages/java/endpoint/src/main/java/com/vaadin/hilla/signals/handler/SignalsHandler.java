package com.vaadin.hilla.signals.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.signals.NumberSignal;
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
    private final EndpointInvoker invoker;

    public SignalsHandler(SignalsRegistry registry, EndpointInvoker invoker,
            ObjectMapper mapper) {
        this.registry = registry;
        this.jsonEventMapper = new JsonEventMapper(mapper);
        this.invoker = invoker;
    }

    /**
     * Subscribes to a signal.
     *
     * @param signalProviderEndpointMethod
     *            the endpoint method that provides the signal
     * @param clientSignalId
     *            the client signal id
     *
     * @return a Flux of JSON events
     */
    public Flux<String> subscribe(String signalProviderEndpointMethod,
            UUID clientSignalId) {
        String[] endpointMethodParts = signalProviderEndpointMethod
                .split("\\.");
        try {
            if (registry.contains(clientSignalId)) {
                return registry.get(clientSignalId).subscribe()
                        .map(jsonEventMapper::toJson);
            }

            NumberSignal signal = (NumberSignal) invoker.invoke(
                    endpointMethodParts[0], endpointMethodParts[1], null, null,
                    null);
            registry.register(clientSignalId, signal);
            return registry.get(clientSignalId).subscribe()
                    .map(jsonEventMapper::toJson);
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    /**
     * Updates a signal with an event.
     *
     * @param clientSignalId
     *            the clientSignalId associated with the signal to update
     * @param event
     *            the event to update with
     */
    public void update(UUID clientSignalId, String event) {
        if (!registry.contains(clientSignalId)) {
            throw new IllegalStateException(String.format(
                    "Signal not found for client signal: %s", clientSignalId));
        }
        registry.get(clientSignalId).submit(jsonEventMapper.fromJson(event));
    }
}
