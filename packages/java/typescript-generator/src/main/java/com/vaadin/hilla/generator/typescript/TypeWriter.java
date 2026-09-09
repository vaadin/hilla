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
    private final boolean readOnly;

    /**
     * @param imports
     *            the imports of the file being written
     * @param directory
     *            the folder of the file being written, relative to the output
     *            folder, which decides how entity files are referred to
     */
    TypeWriter(ImportRegistry imports, String directory) {
        this(imports, directory, false);
    }

    private TypeWriter(ImportRegistry imports, String directory,
            boolean readOnly) {
        this.imports = imports;
        this.directory = directory;
        this.readOnly = readOnly;
    }

    /**
     * The same writer, writing an array as one which cannot be changed, which
     * is what a form model says about the values it holds.
     */
    TypeWriter readOnly() {
        return new TypeWriter(imports, directory, true);
    }

    String write(TypeModel type) {
        var written = writeRequired(type);

        // A type variable stands for whatever the declaration is used with,
        // which says for itself whether it can be absent
        var union = type.optional()
                && !(type instanceof TypeModel.TypeVariable);

        return union ? written + " | undefined" : written;
    }

    /**
     * Writes the type without the union with {@code undefined} which an
     * optional value is written as, for the places saying the same thing
     * another way, such as the {@code ?} marker of an optional property.
     */
    String writeRequired(TypeModel type) {
        return switch (type) {
        case TypeModel.Scalar scalar -> write(scalar.kind());
        case TypeModel.ArrayOf array -> (readOnly ? "ReadonlyArray<" : "Array<")
                + write(array.items()) + ">";
        case TypeModel.MapOf map ->
            "Record<string, " + write(map.values()) + ">";
        case TypeModel.EntityRef entity -> write(entity);
        case TypeModel.TypeVariable variable -> variable.name();
        };
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
