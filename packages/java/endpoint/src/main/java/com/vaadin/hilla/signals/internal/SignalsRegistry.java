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

package com.vaadin.hilla.signals.internal;

import com.vaadin.signals.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 * A registry for signal instances and their client signal id mappings.
 */
public final class SignalsRegistry {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SignalsRegistry.class);
    private final Map<Id, InternalSignal> signals = new WeakHashMap<>();
    private final Map<String, Id> clientSignalToSignalMapping = new HashMap<>();

    SignalsRegistry() {
    }

    /**
     * Registers a signal instance and creates an association between the
     * provided {@code clientSignalId} and {@code signal}.
     * <p>
     * If the signal is already registered, signal instance registration is
     * skipped. if the mapping between the provided {@code clientSignalId} and
     * {@code signal} is already registered, the mapping is skipped, too.
     *
     * @param clientSignalId
     *                       the client signal id, must not be null
     * @param signal
     *                       the signal instance, must not be null
     * @throws NullPointerException
     *                              if {@code clientSignalId} or {@code signal} is
     *                              null
     */
    public synchronized void register(String clientSignalId,
            InternalSignal signal) {
        Objects.requireNonNull(clientSignalId,
                "Client signal id must not be null");
        Objects.requireNonNull(signal, "Signal must not be null");
        if (!signals.containsKey(signal.id())) {
            signals.put(signal.id(), signal);
        }
        if (!clientSignalToSignalMapping.containsKey(clientSignalId)) {
            clientSignalToSignalMapping.put(clientSignalId, signal.id());
        }
        LOGGER.debug("Registered client-signal: {} => signal: {}",
                clientSignalId, signal.id());
    }

    /**
     * Get a signal instance by the provided {@code clientSignalId}.
     * <p>
     *
     * @param clientSignalId
     *                       the client signal id, must not be null
     *
     * @return the signal instance, or null if no signal is found for the
     *         provided {@code clientSignalId}
     * @throws NullPointerException
     *                              if {@code clientSignalId} is null
     */
    public synchronized InternalSignal get(String clientSignalId) {
        Objects.requireNonNull(clientSignalId,
                "Client signal id must not be null");
        Id signalId = clientSignalToSignalMapping.get(clientSignalId);
        if (signalId == null) {
            LOGGER.debug("No associated signal found for client signal id: {}",
                    clientSignalId);
            return null;
        }
        return signals.get(signalId);
    }

    /**
     * Get a signal instance by the provided {@code signalId}.
     * <p>
     *
     * @param signalId
     *                 the signal id, must not be null
     *
     * @return the signal instance, or null if no signal is found for the
     *         provided {@code signalId}
     * @throws NullPointerException
     *                              if {@code signalId} is null
     */
    public synchronized InternalSignal getBySignalId(Id signalId) {
        Objects.requireNonNull(signalId, "Signal id must not be null");
        return signals.get(signalId);
    }

    /**
     * Checks if a mapping exists between a registered signal instance and the
     * provided {@code clientSignalId}.
     *
     * @param clientSignalId
     *                       the client signal id, must not be null
     * @return true if the signal instance is registered, false otherwise
     * @throws NullPointerException
     *                              if {@code signalId} is null
     */
    public synchronized boolean contains(String clientSignalId) {
        Objects.requireNonNull(clientSignalId,
                "Client signal id must not be null");
        if (!clientSignalToSignalMapping.containsKey(clientSignalId)) {
            return false;
        }
        var signalId = clientSignalToSignalMapping.get(clientSignalId);
        if (!signals.containsKey(signalId)) {
            throw new IllegalStateException(String.format(
                    "A mapping for client Signal exists, but the signal itself is not registered. Client signal id: %s",
                    clientSignalId));
        }
        return true;
    }

    /**
     * Removes a signal instance by the provided {@code signalId}.
     * <p>
     * It also removes all the possible associated client signals, too.
     *
     * @param signalId
     *                 the signal id, must not be null
     * @throws NullPointerException
     *                              if {@code signalId} is null
     */
    public synchronized void unregister(Id signalId) {
        Objects.requireNonNull(signalId,
                "Signal id to remove must not be null");
        signals.remove(signalId);
        clientSignalToSignalMapping.values().removeIf(signalId::equals);
        LOGGER.debug(
                "Removed signal {}, and the possible mappings between for its associated client signals, too.",
                signalId);
    }

    /**
     * Removes only the mapping between a signal instance and the provided
     * {@code clientSignalId}.
     *
     * @param clientSignalId
     *                       the client signal id, must not be null
     * @throws NullPointerException
     *                              if {@code clientSignalId} is null
     */
    public synchronized void removeClientSignalToSignalMapping(
            String clientSignalId) {
        Objects.requireNonNull(clientSignalId,
                "Client signal id to remove must not be null");
        clientSignalToSignalMapping.remove(clientSignalId);
        LOGGER.debug("Removed client signal to signal mapping: {}",
                clientSignalId);
    }

    /**
     * Checks if the registry is empty.
     *
     * @return true if the registry is empty, false otherwise
     */
    public synchronized boolean isEmpty() {
        return signals.isEmpty();
    }

    /**
     * Returns the number of registered signal instances.
     *
     * @return the number of registered signal instances
     */
    public synchronized int size() {
        return signals.size();
    }

    /**
     * Returns the number of registered unique mappings between client signal
     * ids and the signal instances.
     *
     * @return the number of registered client signals
     */
    public synchronized int getAllClientSubscriptionsSize() {
        return clientSignalToSignalMapping.size();
    }

    /**
     * Returns the Set of registered client signal ids for the provided
     * {@code signalId}.
     *
     * @param signalId
     *                 the signal id, must not be null
     * @return the Set of registered client signal ids
     * @throws NullPointerException
     *                              if {@code signalId} is null
     */
    public synchronized Set<String> getAllClientSignalIdsFor(Id signalId) {
        Objects.requireNonNull(signalId, "Signal id must not be null");
        if (!signals.containsKey(signalId)) {
            return Set.of();
        }
        return clientSignalToSignalMapping.entrySet().stream()
                .filter(entry -> entry.getValue().equals(signalId))
                .map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }
}
