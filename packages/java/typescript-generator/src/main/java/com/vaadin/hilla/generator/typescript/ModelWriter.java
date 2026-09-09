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

import com.vaadin.hilla.generator.model.TypeModel;

/**
 * Writes the model of a type: the class a form binds a value of that type
 * through, and the way one of them is built.
 *
 * <p>
 * A model is named after the type it is of, so the model of a generated type is
 * a generated class of its own, while the model of everything else is one of
 * the classes the form library provides.
 */
final class ModelWriter {
    private static final String LIT_FORM = "@vaadin/hilla-lit-form";
    private static final String OBJECT_MODEL = "ObjectModel";

    private final ImportRegistry imports;
    private final TypeWriter types;
    private final String directory;

    /**
     * @param imports
     *            the imports of the file being written
     * @param directory
     *            the folder of the file being written, relative to the output
     *            folder, which decides how the other models are referred to
     */
    ModelWriter(ImportRegistry imports, String directory) {
        this.imports = imports;
        // A model holds values which cannot be changed through it, so an array
        // it holds is written as a read only one
        this.types = new TypeWriter(imports, directory).readOnly();
        this.directory = directory;
    }

    /**
     * The type of the model of a value, which is what the property of a form
     * model is declared as.
     */
    String type(TypeModel type) {
        return switch (type) {
        case TypeModel.Scalar scalar -> scalarModel(scalar.kind());
        case TypeModel.ArrayOf array ->
            arrayModel() + "<" + type(array.items()) + ">";
        // A map is an object whose properties are the keys, so the model of it
        // says which values those properties hold
        case TypeModel.MapOf map -> objectModel() + "<Record<string, "
                + types.write(map.values()) + ">>";
        case TypeModel.EntityRef entity -> name(entity);
        case TypeModel.TypeVariable variable -> objectModel();
        };
    }

    /**
     * How a model of a value is built, which a form model hands to the binding
     * of the property holding it.
     */
    String instance(TypeModel type) {
        return switch (type) {
        case TypeModel.Scalar scalar -> instance(scalarModel(scalar.kind()),
                scalar.optional(), meta(scalar.javaType()));
        // The model of the items goes on a line of its own, since a model
        // holding a model holding a model is unreadable as one line
        case TypeModel.ArrayOf array -> instance(arrayModel(), array.optional(),
                "\n  (parent, key) => " + instance(array.items()),
                "\n  " + meta(array.javaType()));
        case TypeModel.MapOf map ->
            instance(objectModel(), map.optional(), meta(map.javaType()));
        case TypeModel.EntityRef entity ->
            instance(name(entity), entity.optional());
        // A type variable holds whatever the declaration is used with, which
        // is nothing the model can be told about
        case TypeModel.TypeVariable variable ->
            instance(objectModel(), variable.optional());
        };
    }

    /**
     * The name the model of a generated type goes by, importing it as the value
     * it is used as.
     */
    String name(TypeModel.EntityRef entity) {
        var name = ModulePaths.entityName(entity.javaClass())
                + FormModelWriter.SUFFIX;

        return imports.importDefault(
                ModulePaths.forEntity(
                        entity.javaClass() + FormModelWriter.SUFFIX, directory),
                name, false);
    }

    String objectModel() {
        return imports.importNamed(LIT_FORM, OBJECT_MODEL, false);
    }

    /**
     * The model of an enum, which binds the constants it accepts rather than
     * properties, and what tells it which those are.
     */
    String enumModel() {
        return imports.importNamed(LIT_FORM, "EnumModel", false);
    }

    String enumConstants() {
        return imports.importNamed(LIT_FORM, "_enum", false);
    }

    /**
     * What a model asks for the model of one of its properties by, which is
     * what keeps one model per property rather than one per read.
     */
    String propertyModel() {
        return imports.importNamed(LIT_FORM, "_getPropertyModel", false);
    }

    /**
     * What builds the empty value a form starts from, which differs between a
     * type with properties and an enum.
     */
    String emptyValueCreator(boolean enumeration) {
        return imports.importNamed(LIT_FORM,
                enumeration ? "makeEnumEmptyValueCreator"
                        : "makeObjectEmptyValueCreator",
                false);
    }

    private String arrayModel() {
        return imports.importNamed(LIT_FORM, "ArrayModel", false);
    }

    private String scalarModel(TypeModel.ScalarKind kind) {
        return switch (kind) {
        case STRING -> imports.importNamed(LIT_FORM, "StringModel", false);
        case NUMBER -> imports.importNamed(LIT_FORM, "NumberModel", false);
        case BOOLEAN -> imports.importNamed(LIT_FORM, "BooleanModel", false);
        case UNKNOWN, VOID -> objectModel();
        };
    }

    /**
     * What a model is told about the Java type a value comes in, which
     * TypeScript has no way of telling apart.
     */
    private static String meta(String javaType) {
        return "{ meta: { javaType: '" + javaType + "' } }";
    }

    private static String instance(String model, boolean optional,
            String... arguments) {
        var builder = new StringBuilder("new ").append(model)
                .append("(parent, key, ").append(optional);

        for (var argument : arguments) {
            // An argument of its own line brings the space with it
            builder.append(argument.startsWith("\n") ? "," : ", ")
                    .append(argument);
        }

        return builder.append(')').toString();
    }
}
