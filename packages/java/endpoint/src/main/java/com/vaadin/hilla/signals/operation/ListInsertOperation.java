/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.signals.operation;

import com.vaadin.hilla.signals.core.event.ListStateEvent;

public record ListInsertOperation<T>(
    String operationId,
    ListStateEvent.InsertPosition position,
    T value
) implements ValueOperation<T> {}
