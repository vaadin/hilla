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

import com.vaadin.hilla.generator.model.EndpointModel;

/**
 * Writes the file re-exporting every endpoint, which is what an application
 * imports when it does not want to name the file of each of them.
 * <p>
 * <b>Internal API.</b> Not part of the public Hilla API: used only by Hilla
 * itself, and may change or be removed in any release without notice or
 * deprecation. Do not use it in application code or libraries.
 */
public final class BarrelWriter {
    /**
     * The module of the barrel, relative to the output folder.
     */
    public static final String MODULE = "endpoints";

    public GeneratedFile write(List<EndpointModel> endpoints) {
        var imports = new ImportRegistry();
        var names = new ArrayList<String>();

        endpoints.stream().map(EndpointModel::name).sorted().forEach(name -> {
            var local = imports.importAll(ModulePaths.forEndpoint(name, ""),
                    name);
            // An endpoint named like a type of the language is
            // imported under another name, but still exported as
            // itself
            names.add(local.equals(name) ? name : local + " as " + name);
        });

        return GeneratedFile.of(MODULE, imports,
                GeneratedFile.exportList(names));
    }
}
