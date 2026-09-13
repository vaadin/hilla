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
package com.vaadin.hilla.springnative;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.engine.EngineAutoConfiguration;

/**
 * Verifies that a native image is told about the classes the browser reaches,
 * which are found by walking the browser callable classes rather than by
 * reading what a build wrote about them.
 */
public class HillaHintsRegistrarTest {
    @BrowserCallable
    public static class HintedEndpoint {
        public Hinted get() {
            return null;
        }
    }

    public static class Hinted {
        private String name;

        public String getName() {
            return name;
        }
    }

    @Test
    public void should_RegisterTheTypesAnEndpointExposes() {
        var hints = new RuntimeHints();

        new HillaHintsRegistrar().registerEndpointTypes(
                hints, new EngineAutoConfiguration.Builder()
                        .withDefaultAnnotations().build(),
                List.of(HintedEndpoint.class));

        var registered = registeredTypes(hints);

        assertTrue(registered.contains(HintedEndpoint.class.getName()),
                "The browser calls the endpoint: " + registered);
        assertTrue(registered.contains(Hinted.class.getName()),
                "The endpoint sends a value of the type: " + registered);
    }

    private static List<String> registeredTypes(RuntimeHints hints) {
        return hints.reflection().typeHints()
                .map(hint -> hint.getType().getName())
                .collect(Collectors.toList());
    }
}
