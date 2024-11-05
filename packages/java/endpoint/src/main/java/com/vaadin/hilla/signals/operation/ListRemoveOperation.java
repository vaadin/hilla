package com.vaadin.hilla.signals.operation;

import com.vaadin.hilla.signals.core.event.ListStateEvent;

public record ListRemoveOperation<T>(
    String operationId,
    ListStateEvent.ListEntry<T> entryToRemove)
    implements SignalOperation {}
