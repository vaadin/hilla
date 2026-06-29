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
package com.vaadin.hilla.test.signals.service;

import org.springframework.context.annotation.DependsOn;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.signals.shared.SharedListSignal;
import com.vaadin.hilla.BrowserCallable;

@AnonymousAllowed
@BrowserCallable
@DependsOn("signalsConfiguration")
public class ListSignalService {

    public record Item(String text, boolean completed) {
    }

    private final SharedListSignal<Item> items = new SharedListSignal<>(
            Item.class);

    public SharedListSignal<Item> items() {
        return items;
    }
}
