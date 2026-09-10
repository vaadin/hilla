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
package com.vaadin.hilla.generator.fixtures;

import com.vaadin.flow.signals.shared.SharedListSignal;
import com.vaadin.flow.signals.shared.SharedNumberSignal;
import com.vaadin.flow.signals.shared.SharedValueSignal;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

/**
 * Shares values with the client through signals, which it builds rather than
 * calling the methods.
 */
@Endpoint
public class SignalsEndpoint {
    public SharedNumberSignal counter() {
        return null;
    }

    public SharedValueSignal<String> name() {
        return null;
    }

    public SharedValueSignal<SampleEndpoint.Sample> sample(boolean detailed) {
        return null;
    }

    public SharedListSignal<String> names() {
        return null;
    }

    /**
     * Shares a value which is always there, so that the signal starts from the
     * empty value of the type rather than from nothing.
     */
    @Nonnull
    public SharedValueSignal<@Nonnull String> title() {
        return null;
    }

    @Nonnull
    public SharedValueSignal<SampleEndpoint.@Nonnull Sample> current() {
        return null;
    }

    /**
     * A signal which is always there, holding a value which is not: whether the
     * signal can be absent and whether the value can are two questions.
     */
    @Nonnull
    public SharedValueSignal<String> pending() {
        return null;
    }
}
