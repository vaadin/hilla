/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.hilla.signals.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.EndpointInvocationException;
import com.vaadin.hilla.signals.internal.SecureSignalsRegistry;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Handler Endpoint for Fullstack Signals' subscription and update events.
 */
@AnonymousAllowed
@Component
public class SignalsHandler {

    private static final String FEATURE_FLAG_ERROR_MESSAGE = """
            %n
            ***********************************************************************************************************************
            *  The Hilla Fullstack Signals API is currently considered experimental and may change in the future.                 *
            *  To use it you need to explicitly enable it in Copilot, or by adding com.vaadin.experimental.fullstackSignals=true  *
            *    to src/main/resources/vaadin-featureflags.properties.                                                            *
            ***********************************************************************************************************************
            %n"""
            .stripIndent();

    private final SecureSignalsRegistry registry;

    public SignalsHandler(@Nullable SecureSignalsRegistry registry) {
        this.registry = registry;
    }

    /**
     * Subscribes to a signal.
     *
     * @param providerEndpoint
     *            the endpoint that provides the signal
     * @param providerMethod
     *            the endpoint method that provides the signal
     * @param clientSignalId
     *            the client signal id
     *
     * @return a Flux of JSON events
     */
    public Flux<JsonNode> subscribe(String providerEndpoint,
            String providerMethod, String clientSignalId, ObjectNode body) {
        if (registry == null) {
            throw new IllegalStateException(
                    String.format(FEATURE_FLAG_ERROR_MESSAGE));
        }
        try {
            var signal = registry.get(clientSignalId);
            if (signal != null) {
                return signal.subscribe(clientSignalId).doFinally(
                        (event) -> registry.unsubscribe(clientSignalId));
            }
            registry.register(clientSignalId, providerEndpoint, providerMethod,
                    body);
            return registry.get(clientSignalId).subscribe(clientSignalId)
                    .doFinally((event) -> registry.unsubscribe(clientSignalId));
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
    public void update(String clientSignalId, ObjectNode event)
            throws EndpointInvocationException.EndpointHttpException {
        if (registry == null) {
            throw new IllegalStateException(
                    String.format(FEATURE_FLAG_ERROR_MESSAGE));
        }
        if (registry.get(clientSignalId) == null) {
            throw new IllegalStateException(String.format(
                    "Signal not found for client signal: %s", clientSignalId));
        }
        registry.get(clientSignalId).submit(clientSignalId, event);
    }
}
