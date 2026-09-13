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

import java.util.stream.Collectors;

import com.vaadin.hilla.generator.model.TypeModel;

/**
 * Writes a {@link TypeModel} as a TypeScript type, importing whatever the
 * result refers to.
 */
final class TypeWriter {
    private final ImportRegistry imports;
    private final String directory;

    /**
     * @param imports
     *            the imports of the file being written
     * @param directory
     *            the folder of the file being written, relative to the output
     *            folder, which decides how entity files are referred to
     */
    TypeWriter(ImportRegistry imports, String directory) {
        this.imports = imports;
        this.directory = directory;
    }

    String write(TypeModel type) {
        var written = switch (type) {
        case TypeModel.Scalar scalar -> write(scalar.kind());
        case TypeModel.ArrayOf array -> "Array<" + write(array.items()) + ">";
        case TypeModel.MapOf map ->
            "Record<string, " + write(map.values()) + ">";
        case TypeModel.EntityRef entity -> write(entity);
        case TypeModel.TypeVariable variable -> variable.name();
        };

        return type.optional() ? written + " | undefined" : written;
    }

    private static String write(TypeModel.ScalarKind kind) {
        return switch (kind) {
        case STRING -> "string";
        case NUMBER -> "number";
        case BOOLEAN -> "boolean";
        case UNKNOWN -> "unknown";
        case VOID -> "void";
        };
    }

    private String write(TypeModel.EntityRef entity) {
        var name = imports.importDefault(
                ModulePaths.forEntity(entity.javaClass(), directory),
                ModulePaths.entityName(entity.javaClass()), true);

        if (entity.typeArguments().isEmpty()) {
            return name;
        }

        return name + entity.typeArguments().stream().map(this::write)
                .collect(Collectors.joining(", ", "<", ">"));
    }
}
