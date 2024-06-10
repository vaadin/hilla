package com.vaadin.hilla.signals.core;

import com.vaadin.hilla.signals.NumberSignal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

@Component
public class SignalsRegistry {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SignalsRegistry.class);
    private final WeakHashMap<UUID, NumberSignal> signals = new WeakHashMap<>();
    private final HashMap<UUID, UUID> clientSignalToSignalMapping = new HashMap<>();

    /**
     * Registers a signal instance and creates an association between the
     * provided {@code clientSignalId} and {@code signal}.
     * <p>
     * If the signal is already registered, signal instance registration is
     * skipped. if the mapping between the provided {@code clientSignalId} and
     * {@code signal} is already registered, the mapping is skipped, too.
     *
     * @param clientSignalId
     *            the client signal id, must not be null
     * @param signal
     *            the signal instance, must not be null
     * @throws NullPointerException
     *             if {@code clientSignalId} or {@code signal} is null
     */
    public synchronized void register(UUID clientSignalId,
            NumberSignal signal) {
        Objects.requireNonNull(clientSignalId,
                "Client signal id must not be null");
        Objects.requireNonNull(signal, "Signal must not be null");
        if (!signals.containsKey(signal.getId())) {
            signals.put(signal.getId(), signal);
        }
        if (!clientSignalToSignalMapping.containsKey(clientSignalId)) {
            clientSignalToSignalMapping.put(clientSignalId, signal.getId());
        }
        LOGGER.debug("Registered client-signal: {} => signal: {}",
                clientSignalId, signal.getId());
    }

    /**
     * Get a signal instance by the provided {@code clientSignalId}.
     * <p>
     *
     * @param clientSignalId
     *            the client signal id, must not be null
     *
     * @return the signal instance, or null if no signal is found for the
     *         provided {@code clientSignalId}
     * @throws NullPointerException
     *             if {@code clientSignalId} is null
     */
    public synchronized NumberSignal get(UUID clientSignalId) {
        Objects.requireNonNull(clientSignalId,
                "Client signal id must not be null");
        UUID signalId = clientSignalToSignalMapping.get(clientSignalId);
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
     *            the signal id, must not be null
     *
     * @return the signal instance, or null if no signal is found for the
     *         provided {@code signalId}
     * @throws NullPointerException
     *             if {@code signalId} is null
     */
    public synchronized NumberSignal getBySignalId(UUID signalId) {
        Objects.requireNonNull(signalId, "Signal id must not be null");
        return signals.get(signalId);
    }

    /**
     * Checks if a mapping exists between a registered signal instance and the
     * provided {@code clientSignalId}.
     *
     * @param clientSignalId
     *            the client signal id, must not be null
     * @return true if the signal instance is registered, false otherwise
     * @throws NullPointerException
     *             if {@code signalId} is null
     */
    public synchronized boolean contains(UUID clientSignalId) {
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
     * Checks if a signal instance is registered.
     *
     * @param signalId
     *            the signal id, must not be null
     * @return true if the signal instance is registered, false otherwise
     * @throws NullPointerException
     *             if {@code signalId} is null
     */
    public synchronized boolean containsSignal(UUID signalId) {
        Objects.requireNonNull(signalId, "Client signal id must not be null");
        return signals.containsKey(signalId);
    }

    /**
     * Removes a signal instance by the provided {@code signalId}.
     * <p>
     * It also removes all the possible associated client signals, too.
     *
     * @param signalId
     *            the signal id, must not be null
     * @throws NullPointerException
     *             if {@code signalId} is null
     */
    public synchronized void removeSignal(UUID signalId) {
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
     *            the client signal id, must not be null
     * @throws NullPointerException
     *             if {@code clientSignalId} is null
     */
    public synchronized void removeClientSignalToSignalMapping(
            UUID clientSignalId) {
        Objects.requireNonNull(clientSignalId,
                "Signal id to remove must not be null");
        clientSignalToSignalMapping.remove(clientSignalId);
        LOGGER.debug("Removed client signal to signal mapping: {}",
                clientSignalId);
    }

    /**
     * Removes all signal instances and all possible associated client signals,
     * too.
     */
    public synchronized void clear() {
        signals.clear();
        clientSignalToSignalMapping.clear();
        LOGGER.debug("Cleared all signal instances");
    }

    /**
     * Checks if the registry is empty.
     *
     * @return true if the registry is empty, false otherwise
     */
    public synchronized boolean isEmpty() {
        return signals.isEmpty();
    }

}
