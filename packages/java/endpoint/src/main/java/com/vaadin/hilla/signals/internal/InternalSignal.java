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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import com.vaadin.flow.signals.Id;
import com.vaadin.flow.signals.SignalCommand;
import com.vaadin.flow.signals.function.CleanupCallback;
import com.vaadin.flow.signals.shared.AbstractSignal;
import com.vaadin.flow.signals.shared.SignalUtils;
import com.vaadin.flow.signals.shared.impl.CommandResult;
import com.vaadin.flow.signals.shared.impl.SignalTree;

/**
 * A proxy for a signal instance that allows subscribing to it and submitting
 * commands.
 * <p>
 * This is internal API and should not be used outside the framework.
 */
public class InternalSignal {

    // ClientSignalId -> Subscriber's sink
    private final Map<String, Sinks.Many<JsonNode>> subscribers = new HashMap<>();

    private final AbstractSignal<?> signal;
    private final SignalTree tree;
    private CleanupCallback treeSubscriptionCanceler;

    // Commands in processing, mapping commandId -> clientSignalId
    private final Map<Id, ObjectNode> inProgressCommands = new HashMap<>();
    // Lookup for clientSignalId by commandId
    private final Map<Id, String> commandsOfSubscribers = new HashMap<>();
    private final ObjectMapper objectMapper;

    public InternalSignal(AbstractSignal<?> signal, ObjectMapper objectMapper) {
        this.signal = signal;
        this.tree = SignalUtils.treeOf(signal);
        this.objectMapper = objectMapper;
    }

    public Id id() {
        return tree.id();
    }

    /**
     * Subscribes to the signal.
     *
     * @param clientSignalId
     *            the clientSignalId associated with the signal to update
     * @return a Flux of JSON events
     */
    public Flux<JsonNode> subscribe(String clientSignalId) {
        Sinks.Many<JsonNode> sink = Sinks.many().unicast()
                .onBackpressureBuffer();
        return sink.asFlux().doOnSubscribe(ignore -> {
            tree.getLock().lock();
            try {
                getLogger().debug("New Flux subscription...");
                subscribers.put(clientSignalId, sink);
                if (treeSubscriptionCanceler == null) {
                    treeSubscriptionCanceler = tree
                            .subscribeToProcessed(this::notifySubscribers);
                }
                // TODO: the targetNodeId is ZERO for single-valued signals:
                var setCommand = new SignalCommand.SnapshotCommand(Id.random(),
                        SignalUtils.treeOf(signal).confirmed().nodes());
                sink.tryEmitNext(objectMapper.valueToTree(setCommand));
            } finally {
                tree.getLock().unlock();
            }
        }).doFinally(ignore -> {
            tree.getLock().lock();
            try {
                getLogger().debug("Unsubscribing from Signal...");
                subscribers.remove(clientSignalId);
                if (subscribers.isEmpty()) {
                    getLogger().debug(
                            "No more subscribers, canceling tree subscription");
                    assert treeSubscriptionCanceler != null;
                    treeSubscriptionCanceler.cleanup();
                    treeSubscriptionCanceler = null;
                }
            } finally {
                tree.getLock().unlock();
            }
        });
    }

    private void notifySubscribers(SignalCommand processedCommand,
            CommandResult result) {
        var commandToEmit = inProgressCommands
                .remove(processedCommand.commandId());
        if (result.accepted()) {
            subscribers.entrySet().removeIf(
                    client -> tryEmitCommandToSubscriber(commandToEmit,
                            client.getKey(), client.getValue()));
        } else {
            // only notify the client that issued the failed command
            String clientSignalId = commandsOfSubscribers
                    .get(processedCommand.commandId());
            if (clientSignalId == null) {
                getLogger().debug(
                        "No client signal id found for command id {}, skipping notification.",
                        processedCommand.commandId());
                return;
            }

            boolean failure = tryEmitCommandToSubscriber(commandToEmit,
                    clientSignalId, subscribers.get(clientSignalId));
            if (failure) {
                // remove the subscriber if it failed to emit to:
                subscribers.remove(clientSignalId);
            }
        }
        commandsOfSubscribers.remove(processedCommand.commandId());
    }

    private boolean tryEmitCommandToSubscriber(ObjectNode processedCommand,
            String clientSignalId, Sinks.Many<JsonNode> clientSink) {
        boolean failure = clientSink.tryEmitNext(processedCommand).isFailure();
        if (failure) {
            getLogger().debug(
                    "Failed to emit notification to client with signal id {} and command {}",
                    clientSignalId, processedCommand.get("commandId"));
        }
        return failure;
    }

    /**
     * Submits an commandJson to the signal and notifies subscribers about the
     * change of the signal value.
     *
     * @param clientSignalId
     *            the clientSignalId associated with the signal to update
     * @param commandJson
     *            the command to submit in JSON format
     */
    public void submit(String clientSignalId, ObjectNode commandJson) {
        tree.getLock().lock();
        try {
            SignalCommand command = objectMapper.treeToValue(commandJson,
                    SignalCommand.class);
            inProgressCommands.put(command.commandId(), commandJson);
            commandsOfSubscribers.put(command.commandId(), clientSignalId);
            tree.commitSingleCommand(command);
        } catch (IllegalArgumentException | JacksonException ex) {
            getLogger().error("Failed to process command for signal {}: {}",
                    signal.getClass().getName(), ex.getMessage(), ex);
        } finally {
            tree.getLock().unlock();
        }
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(InternalSignal.class);
    }
}
