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
package com.vaadin.hilla.generator.model;

import java.util.List;
import java.util.Objects;

/**
 * A method of a browser callable class.
 *
 * @param returnType
 *            what a call returns, or the values a subscription sends, or the
 *            signal a value is shared through
 */
public record MethodModel(String name, List<ParameterModel> parameters,
        TypeModel returnType, Kind kind) {
    public MethodModel {
        Objects.requireNonNull(name);
        Objects.requireNonNull(returnType);
        Objects.requireNonNull(kind);
        parameters = List.copyOf(parameters);
    }

    /**
     * The signal the value of a method is shared through, which is the type it
     * returns.
     *
     * @throws IllegalStateException
     *             if the method shares no value through a signal
     */
    public TypeModel.Provided signal() {
        if (returnType instanceof TypeModel.Provided provided
                && kind != Kind.CALLED && kind != Kind.SUBSCRIBED) {
            return provided;
        }

        throw new IllegalStateException(
                "The method " + name + " shares no value through a signal");
    }

    /**
     * How the client reaches a method: it calls most of them, subscribes to the
     * ones sending a series of values, and builds a signal of its own for the
     * ones sharing a value with the server.
     */
    public enum Kind {
        CALLED, SUBSCRIBED, NUMBER_SIGNAL, VALUE_SIGNAL, LIST_SIGNAL
    }
}
