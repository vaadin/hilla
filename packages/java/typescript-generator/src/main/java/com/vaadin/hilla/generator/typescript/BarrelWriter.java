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
 */
public final class BarrelWriter {
    /**
     * The module of the barrel, relative to the output folder.
     */
    public static final String MODULE = "endpoints";

    public GeneratedFile write(List<EndpointModel> endpoints) {
        var imports = new ImportRegistry();
        var names = new ArrayList<String>();

        endpoints.stream().map(EndpointModel::name).sorted()
                .forEach(name -> names.add(imports
                        .importAll(ModulePaths.forEndpoint(name, ""), name)));

        var lines = new ArrayList<>(imports.write());

        if (!names.isEmpty()) {
            lines.add("");
        }

        lines.add(names.isEmpty() ? "export {};"
                : "export { " + String.join(", ", names) + " };");

        return new GeneratedFile(MODULE + ".ts",
                String.join("\n", lines) + "\n");
    }
}
