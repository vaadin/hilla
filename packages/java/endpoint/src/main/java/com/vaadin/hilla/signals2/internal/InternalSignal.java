package com.vaadin.hilla.signals2.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.signals.Id;
import com.vaadin.signals.Signal;
import com.vaadin.signals.SignalCommand;
import com.vaadin.signals.SignalUtils;
import com.vaadin.signals.impl.SignalTree;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InternalSignal {

    private final Set<Sinks.Many<JsonNode>> subscribers = ConcurrentHashMap
            .newKeySet();

    private final Signal<?> signal;
    private final SignalTree tree;
    private Runnable treeSubscriptionCanceler;

    public InternalSignal(Signal<?> signal) {
        this.signal = signal;
        this.tree = SignalUtils.treeOf(signal);
    }

    public Id id() {
        return SignalUtils.treeOf(signal).id();
    }

    /**
     * Subscribes to the signal.
     *
     * @return a Flux of JSON events
     */
    public Flux<JsonNode> subscribe() {
        Sinks.Many<JsonNode> sink = Sinks.many().unicast()
                .onBackpressureBuffer();
        return sink.asFlux().doOnSubscribe(ignore -> {
            getLogger().debug("New Flux subscription...");
            subscribers.add(sink);
            treeSubscriptionCanceler = tree
                    .subscribeToProcessed(this::notifySubscribers);
            var snapshot = signal.peekConfirmed();
            // TODO: the targetNodeId is ZERO for single-valued signals:
            var setCommand = new SignalCommand.SetCommand(Id.random(), Id.ZERO, CommandUtils.toJson(snapshot));
            sink.tryEmitNext(CommandUtils.toJson(setCommand));
        }).doFinally(ignore -> {
            getLogger().debug("Unsubscribing from Signal...");
            subscribers.remove(sink);
            if (subscribers.isEmpty()) {
                getLogger().debug(
                        "No more subscribers, canceling tree subscription");
                assert treeSubscriptionCanceler != null;
                treeSubscriptionCanceler.run();
                treeSubscriptionCanceler = null;
            }
        });
    }

    private void notifySubscribers(SignalCommand command) {
        subscribers.removeIf(sink -> {
            JsonNode processedEvent = CommandUtils.toJson(command);
            boolean failure = sink.tryEmitNext(processedEvent).isFailure();
            if (failure) {
                getLogger().debug("Failed push");
            }
            return failure;
        });
    }

    /**
     * Submits an event to the signal and notifies subscribers about the change
     * of the signal value.
     *
     * @param event
     *            the event to submit
     */
    public void submit(ObjectNode event) {
        SignalCommand command = CommandUtils.fromJson(event);
        tree.commitSingleCommand(command);
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(InternalSignal.class);
    }
}
