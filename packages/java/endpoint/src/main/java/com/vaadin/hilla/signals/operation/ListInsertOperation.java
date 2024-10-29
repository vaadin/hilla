package com.vaadin.hilla.signals.operation;

import com.vaadin.hilla.signals.core.event.ListStateEvent;

public record ListInsertOperation<T>(
    String operationId,
    ListStateEvent.InsertPosition position,
    T value) {}
