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
package com.vaadin.hilla.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.generator.fixtures.SampleEndpoint;
import com.vaadin.hilla.generator.openapi.OpenAPIToModel;
import com.vaadin.hilla.generator.typescript.BarrelWriter;
import com.vaadin.hilla.generator.typescript.ClientWriter;
import com.vaadin.hilla.generator.typescript.EndpointWriter;
import com.vaadin.hilla.parser.testutils.FullStackGenerator;

/**
 * Verifies the TypeScript written for a browser callable class, all the way
 * from the class itself.
 */
public class GeneratedTypeScriptTest {
    private final List<com.vaadin.hilla.generator.model.EndpointModel> endpoints = OpenAPIToModel
            .endpoints(new FullStackGenerator(GeneratedTypeScriptTest.class,
                    SampleEndpoint.class).parse());

    @Test
    public void should_WriteTheEndpoint() {
        var file = new EndpointWriter(ClientWriter.MODULE_SPECIFIER)
                .write(endpoints.get(0));

        assertEquals("SampleEndpoint.ts", file.path());
        assertEquals(
                """
                        import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
                        import type Sample from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Sample.js';
                        import client from './connect-client.default.js';

                        export async function count(init?: EndpointRequestInit): Promise<number> {
                          return client.call('SampleEndpoint', 'count', {}, init);
                        }

                        export async function counts(init?: EndpointRequestInit): Promise<Record<string, number | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'counts', {}, init);
                        }

                        export async function find(id: string | undefined, init?: EndpointRequestInit): Promise<Sample | undefined> {
                          return client.call('SampleEndpoint', 'find', { id }, init);
                        }

                        export async function greet(name: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('SampleEndpoint', 'greet', { name }, init);
                        }

                        export async function names(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'names', {}, init);
                        }

                        export async function ping(init?: EndpointRequestInit): Promise<void> {
                          return client.call('SampleEndpoint', 'ping', {}, init);
                        }

                        export async function shadow(init: string | undefined, _init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('SampleEndpoint', 'shadow', { init }, _init);
                        }
                        """,
                file.content());
    }

    @Test
    public void should_WriteTheClient() {
        assertEquals("""
                import { ConnectClient } from '@vaadin/hilla-frontend';

                const client = new ConnectClient({ prefix: 'connect' });

                export default client;
                """, new ClientWriter().write().content());
    }

    @Test
    public void should_WriteTheBarrel() {
        assertEquals("""
                import * as SampleEndpoint from './SampleEndpoint.js';

                export { SampleEndpoint };
                """, new BarrelWriter().write(endpoints).content());
    }
}
