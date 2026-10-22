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

/**
 * A generated TypeScript file.
 *
 * @param path
 *            the path of the file relative to the output folder, with {@code /}
 *            as the separator
 */
public record GeneratedFile(String path, String content) {
    public GeneratedFile {
        Objects.requireNonNull(path);
        Objects.requireNonNull(content);
    }

    /**
     * A file of the given module made of its imports, then a blank line, then
     * the body, which is how every generated file is laid out.
     *
     * @param module
     *            the module relative to the output folder, without the
     *            extension
     */
    static GeneratedFile of(String module, ImportRegistry imports,
            String body) {
        var lines = new ArrayList<>(imports.write());

        if (!lines.isEmpty()) {
            lines.add("");
        }

        lines.add(body);

        return new GeneratedFile(module + ".ts",
                String.join("\n", lines) + "\n");
    }

    /**
     * The one statement a file exports what it declares with, which still makes
     * the file a module when there is nothing to export.
     *
     * @param specifiers
     *            what is exported, each as a name or as
     *            <code>local as exported</code>
     */
    static String exportList(List<String> specifiers) {
        return specifiers.isEmpty() ? "export {};"
                : "export { " + String.join(", ", specifiers) + " };";
    }
}
