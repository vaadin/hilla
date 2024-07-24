package com.vaadin.hilla.signals.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.signals.NumberSignal;
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
    private final EndpointInvoker invoker;

    public SignalsHandler(SignalsRegistry registry, EndpointInvoker invoker) {
        this.registry = registry;
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
    public Flux<ObjectNode> subscribe(String signalProviderEndpointMethod,
            UUID clientSignalId) {
        try {
            if (registry.contains(clientSignalId)) {
                return signalAsFlux(clientSignalId);
            }

            String[] endpointMethodParts = signalProviderEndpointMethod
                    .split("\\.");
            NumberSignal signal = (NumberSignal) invoker.invoke(
                    endpointMethodParts[0], endpointMethodParts[1], null, null,
                    null);
            registry.register(clientSignalId, signal);
            return signalAsFlux(clientSignalId);
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    private Flux<ObjectNode> signalAsFlux(UUID clientSignalId) {
        return registry.get(clientSignalId).subscribe();
    }

    /**
     * Updates a signal with an event.
     *
     * @param clientSignalId
     *            the clientSignalId associated with the signal to update
     * @param event
     *            the event to update with
     */
    public void update(UUID clientSignalId, ObjectNode event) {
        if (!registry.contains(clientSignalId)) {
            throw new IllegalStateException(String.format(
                    "Signal not found for client signal: %s", clientSignalId));
        }
        registry.get(clientSignalId).submit(event);
    }
}
