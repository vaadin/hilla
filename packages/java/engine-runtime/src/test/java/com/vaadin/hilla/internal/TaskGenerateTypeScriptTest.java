/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.hilla.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.engine.EngineAutoConfiguration;
import com.vaadin.hilla.internal.fixtures.CustomEndpoint;
import com.vaadin.hilla.internal.fixtures.EndpointNoValue;
import com.vaadin.hilla.internal.fixtures.MyEndpoint;

/**
 * Verifies the tasks writing the TypeScript of the endpoints, which needs no
 * Node at all: the task which parses the classes writes it, because only that
 * run has what the writers need, and the task which would run the Node
 * generator does nothing.
 */
// The same classes as the other task tests, which is what has them share one
// application context: the provider of it is a static one
@SpringBootTest(classes = { CustomEndpoint.class, EndpointNoValue.class,
        MyEndpoint.class, ApplicationContextProvider.class })
public class TaskGenerateTypeScriptTest extends TaskTest {
    @Test
    public void should_WriteTheTypeScriptOfTheEndpointsFromTheClasses()
            throws Exception {
        new TaskGenerateOpenAPIImpl(getEngineConfiguration()).execute();

        assertEquals(
                """
                        import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
                        import client from './connect-client.default.js';

                        export async function bar(baz: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('MyEndpoint', 'bar', { baz }, init);
                        }

                        export async function foo(bar: string | undefined, init?: EndpointRequestInit): Promise<void> {
                          return client.call('MyEndpoint', 'foo', { bar }, init);
                        }
                        """,
                read("MyEndpoint.ts"));
        assertEquals("""
                import * as CustomEndpointName from './CustomEndpointName.js';
                import * as MyEndpoint from './MyEndpoint.js';
                import * as WithoutValueEqual from './WithoutValueEqual.js';

                export { CustomEndpointName, MyEndpoint, WithoutValueEqual };
                """, read("endpoints.ts"));
        assertTrue(
                Files.isRegularFile(
                        output().resolve("connect-client.default.ts")),
                "The endpoints call the server through the generated client");
    }

    @Test
    public void should_WriteTheTypeScriptOfAProductionBuildAsWell()
            throws Exception {
        // A production build has no application context to ask, so the
        // browser callable classes are found by the configuration instead
        var configuration = new EngineAutoConfiguration.Builder(
                getEngineConfiguration()).productionMode(true)
                .browserCallableFinder(conf -> List.of(MyEndpoint.class))
                .build();

        new TaskGenerateOpenAPIImpl(configuration).execute();

        assertTrue(Files.isRegularFile(output().resolve("MyEndpoint.ts")),
                "The endpoint the configuration found is written");
        assertFalse(Files.exists(output().resolve("CustomEndpointName.ts")),
                "The application context is not what a production build asks");
        assertFalse(Files.exists(output().resolve("WithoutValueEqual.ts")),
                "The application context is not what a production build asks");
    }

    @Test
    public void should_LeaveTheTaskRunningTheNodeGeneratorWithNothingToDo()
            throws Exception {
        new TaskGenerateOpenAPIImpl(getEngineConfiguration()).execute();
        var written = read("MyEndpoint.ts");

        new TaskGenerateEndpointImpl(getEngineConfiguration()).execute();

        assertEquals(written, read("MyEndpoint.ts"),
                "The TypeScript was written once, in Java");
    }

    private Path output() {
        return getTemporaryDirectory().resolve(getOutputDirectory());
    }

    private String read(String path) throws IOException {
        return Files.readString(output().resolve(path));
    }
}
