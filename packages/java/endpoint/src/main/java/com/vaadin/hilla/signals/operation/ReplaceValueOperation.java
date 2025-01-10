/*
 * Copyright 2000-2024 Vaadin Ltd.
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

package com.vaadin.hilla.signals.operation;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.StateEvent;

public record ReplaceValueOperation<T>(String operationId, T expected, T value) implements ValueOperation<T> {

    public static <T> ReplaceValueOperation<T> of(ObjectNode event, Class<T> valueType) {
        var rawValue = StateEvent.extractValue(event, true);
        var rawExpected = StateEvent.extractExpected(event, true);
        return new ReplaceValueOperation<>(StateEvent.extractId(event),
            StateEvent.convertValue(rawExpected, valueType),
            StateEvent.convertValue(rawValue, valueType));
    }

    public static <T> ReplaceValueOperation<T> of(String operationId, T expected, T value) {
        return new ReplaceValueOperation<>(operationId, expected, value);
    }
}
