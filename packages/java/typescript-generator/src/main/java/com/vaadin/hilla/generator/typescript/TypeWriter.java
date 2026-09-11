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
    private final boolean declaresTypeParameters;

    /**
     * @param imports
     *            the imports of the file being written
     * @param directory
     *            the folder of the file being written, relative to the output
     *            folder, which decides how entity files are referred to
     */
    TypeWriter(ImportRegistry imports, String directory) {
        this(imports, directory, false, true);
    }

    private TypeWriter(ImportRegistry imports, String directory,
            boolean readOnly, boolean declaresTypeParameters) {
        this.imports = imports;
        this.directory = directory;
        this.readOnly = readOnly;
        this.declaresTypeParameters = declaresTypeParameters;
    }

    /**
     * The same writer, writing an array as one which cannot be changed, which
     * is what a form model says about the values it holds.
     */
    TypeWriter readOnly() {
        return new TypeWriter(imports, directory, true, declaresTypeParameters);
    }

    /**
     * The same writer, for a file which declares no type parameters, and where
     * a value of one is therefore written as an unknown value: an endpoint is
     * written as functions, and what a type parameter of the class or of the
     * method stands for is only known to the server.
     */
    TypeWriter withoutTypeParameters() {
        return new TypeWriter(imports, directory, readOnly, false);
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
        case TypeModel.Provided provided -> write(provided);
        case TypeModel.TypeVariable variable ->
            declaresTypeParameters ? variable.name() : "unknown";
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

    /**
     * Writes a type which is not generated: the name it goes by, imported from
     * the module exporting it unless the browser has it.
     */
    private String write(TypeModel.Provided provided) {
        var name = provided.module().isEmpty() ? provided.name()
                : imports.importNamed(provided.module(), provided.name(), true);

        return name + typeArguments(provided.typeArguments());
    }

    /**
     * Claims the names of the types the browser has, which are written as they
     * are, so that an import of a type of the application does not take one of
     * them and turn it into something else.
     */
    void reserveProvided(TypeModel type) {
        switch (type) {
        case TypeModel.Provided provided -> {
            if (provided.module().isEmpty()) {
                imports.reserve(provided.name());
            }

            provided.typeArguments().forEach(this::reserveProvided);
        }
        case TypeModel.ArrayOf array -> reserveProvided(array.items());
        case TypeModel.MapOf map -> reserveProvided(map.values());
        case TypeModel.EntityRef entity ->
            entity.typeArguments().forEach(this::reserveProvided);
        default -> {
        }
        }
    }

    private String write(TypeModel.EntityRef entity) {
        var name = imports.importDefault(
                ModulePaths.forEntity(entity.javaClass(), directory),
                ModulePaths.entityName(entity.javaClass()), true);

        return name + typeArguments(entity.typeArguments());
    }

    private String typeArguments(java.util.List<TypeModel> arguments) {
        return arguments.isEmpty() ? ""
                : arguments.stream().map(this::write)
                        .collect(Collectors.joining(", ", "<", ">"));
    }
}
