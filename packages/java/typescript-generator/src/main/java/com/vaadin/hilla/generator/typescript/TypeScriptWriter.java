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
package com.vaadin.hilla.generator.typescript;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.hilla.generator.model.Generation;

/**
 * Writes every file of one run of the generator: the client the endpoints call
 * the server with, a file per endpoint, the barrel naming them, and for each of
 * the types they refer to a declaration and the model a form binds it through,
 * along with a union for each polymorphic one.
 *
 * <p>
 * The files are what the run produces rather than what is on disk: writing them
 * out, and removing what a previous run left behind, is the work of whoever
 * asks for them.
 */
public final class TypeScriptWriter {
    private final String clientModule;

    /**
     * Writes the files against the generated client.
     */
    public TypeScriptWriter() {
        this(ClientWriter.MODULE_SPECIFIER);
    }

    /**
     * @param clientModule
     *            the module exporting the client the endpoints call the server
     *            with, which is the generated one unless the application has
     *            one of its own, and then no client is generated
     */
    public TypeScriptWriter(String clientModule) {
        this.clientModule = Objects.requireNonNull(clientModule);
    }

    public List<GeneratedFile> write(Generation generation) {
        var files = new ArrayList<GeneratedFile>();
        var endpoints = new EndpointWriter(clientModule);
        var entities = new EntityWriter();
        var models = new FormModelWriter();
        var unions = new UnionWriter();

        generation.entities().forEach(entity -> {
            files.add(entities.write(entity));
            files.add(models.write(entity));
        });
        generation.unions().forEach(union -> files.add(unions.write(union)));
        generation.endpoints()
                .forEach(endpoint -> files.add(endpoints.write(endpoint)));

        if (clientModule.equals(ClientWriter.MODULE_SPECIFIER)) {
            files.add(new ClientWriter().write());
        }

        files.add(new BarrelWriter().write(generation.endpoints()));

        return List.copyOf(files);
    }
}
