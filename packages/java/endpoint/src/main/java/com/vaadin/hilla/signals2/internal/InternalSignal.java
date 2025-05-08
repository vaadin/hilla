package com.vaadin.hilla.signals2.internal;

import com.vaadin.signals.Id;
import com.vaadin.signals.Signal;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.signals.SignalCommand;
import com.vaadin.signals.SignalUtils;
import com.vaadin.signals.impl.CommandResult;
import com.vaadin.signals.impl.SignalTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class InternalSignal<T> {

    private final ReentrantLock lock = new ReentrantLock();

    private final Set<Sinks.Many<ObjectNode>> subscribers = new HashSet<>();

    private final Signal<T> signal;
    private final SignalTree tree;
    private Runnable treeSubscriptionCanceler;

    public InternalSignal(Signal<T> signal) {
        this.signal = signal;
        this.tree = SignalUtils.treeOf(signal);
    }

    public Id id() {
        return signal.id();
    }

    /**
     * Subscribes to the signal.
     *
     * @return a Flux of JSON events
     */
    public Flux<ObjectNode> subscribe() {
        Sinks.Many<ObjectNode> sink = Sinks.many().unicast()
            .onBackpressureBuffer();
        return sink.asFlux().doOnSubscribe(ignore -> {
            getLogger().debug("New Flux subscription...");
            lock.lock();
            try {
                treeSubscriptionCanceler = tree.subscribeToPublished(this::notifySubscribers);
                var snapshot = signal.peekConfirmed();
                // sink.tryEmitNext(snapshot); TODO: serialize snapshot to JSON
                subscribers.add(sink);
            } finally {
                lock.unlock();
            }
        }).doFinally(ignore -> {
            lock.lock();
            try {
                getLogger().debug("Unsubscribing from Signal...");
                subscribers.remove(sink);
                if (subscribers.isEmpty()) {
                    getLogger().debug("No more subscribers, canceling tree subscription");
                    assert treeSubscriptionCanceler != null;
                    treeSubscriptionCanceler.run();
                    treeSubscriptionCanceler = null;
                }
            } finally {
                lock.unlock();
            }
        });
    }

    private void notifySubscribers(SignalCommand command, CommandResult result) {
        subscribers.removeIf(sink -> {
            ObjectNode processedEvent = null; // TODO: serialize to JSON
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
        lock.lock();
        try {
            SignalCommand command = null; // TODO: deserialize event JSON to SignalCommand
            tree.commitSingleCommand(command);
        } finally {
            lock.unlock();
        }
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(InternalSignal.class);
    }
}
