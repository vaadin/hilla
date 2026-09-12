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
package com.vaadin.hilla.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.vaadin.hilla.engine.fixtures.TestEndpoint;
import com.vaadin.hilla.engine.fixtures.annotations.Endpoint;
import com.vaadin.hilla.generator.model.EndpointModel;

public class ParserProcessorTest {
    @TempDir
    private Path buildDir;

    @Test
    public void should_FindWhatTheTypeScriptIsWrittenFrom() {
        var processor = new ParserProcessor(
                // A configuration of its own to say the annotations of the
                // fixtures with, which the default one is shared through
                new EngineAutoConfiguration.Builder()
                        .parser(new ParserConfiguration()).buildDir(buildDir)
                        .endpointAnnotations(Endpoint.class).build());

        processor.process(List.of(TestEndpoint.class));

        assertEquals(List.of("TestEndpoint"),
                processor.getGeneration().endpoints().stream()
                        .map(EndpointModel::name).toList(),
                "The classes the parser walked are what the TypeScript is"
                        + " written from");
    }
}
