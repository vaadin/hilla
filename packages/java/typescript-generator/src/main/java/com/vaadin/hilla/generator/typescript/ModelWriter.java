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
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.vaadin.hilla.generator.model.ConstraintModel;
import com.vaadin.hilla.generator.model.TypeModel;
import com.vaadin.hilla.parser.models.AnnotationParameterEnumValueModel;
import com.vaadin.hilla.parser.models.ClassInfoModel;

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
        // A value the framework provides is not bound as properties: it is
        // whatever the module exporting it makes of it
        case TypeModel.Provided provided -> objectModel();
        case TypeModel.TypeVariable variable -> objectModel();
        };
    }

    /**
     * The class the model of a value is, without what it holds: it is the class
     * itself which knows the empty value to start from.
     */
    String className(TypeModel type) {
        return switch (type) {
        case TypeModel.Scalar scalar -> scalarModel(scalar.kind());
        case TypeModel.ArrayOf array -> arrayModel();
        case TypeModel.MapOf map -> objectModel();
        case TypeModel.EntityRef entity -> name(entity);
        case TypeModel.Provided provided -> objectModel();
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
                scalar.optional(), options(scalar, scalar.javaType()));
        // The model of the items goes on a line of its own, since a model
        // holding a model holding a model is unreadable as one line
        case TypeModel.ArrayOf array -> instance(arrayModel(), array.optional(),
                "\n  (parent, key) => " + instance(array.items()),
                "\n  " + options(array, array.javaType()));
        case TypeModel.MapOf map -> instance(objectModel(), map.optional(),
                options(map, map.javaType()));
        case TypeModel.EntityRef entity ->
            instance(name(entity), entity.optional(), options(entity, null));
        case TypeModel.Provided provided -> instance(objectModel(),
                provided.optional(), options(provided, null));
        // A type variable holds whatever the declaration is used with, which
        // is nothing the model can be told about
        case TypeModel.TypeVariable variable -> instance(objectModel(),
                variable.optional(), options(variable, null));
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
     * What a model is told about the value it holds: what the value has to
     * satisfy, the annotations of the value which a form or a grid built from
     * the model reads, and the Java type it comes in, which TypeScript has no
     * way of telling apart. A model which is told nothing is built without it.
     *
     * @param javaType
     *            the Java type of the value, or {@code null} for a model which
     *            holds whatever the type it is of holds
     */
    private String options(TypeModel type, String javaType) {
        var written = new ArrayList<String>();

        if (!type.constraints().isEmpty()) {
            written.add("validators: [" + type.constraints().stream()
                    .map(this::validator).collect(Collectors.joining(", "))
                    + "]");
        }

        var meta = new ArrayList<String>();

        if (!type.annotations().isEmpty()) {
            meta.add("annotations: [" + type.annotations().stream()
                    .map(annotation -> "{ name: '" + annotation + "' }")
                    .collect(Collectors.joining(", ")) + "]");
        }

        if (javaType != null) {
            meta.add("javaType: '" + javaType + "'");
        }

        if (!meta.isEmpty()) {
            written.add(meta.stream()
                    .collect(Collectors.joining(", ", "meta: { ", " }")));
        }

        return written.isEmpty() ? ""
                : written.stream()
                        .collect(Collectors.joining(", ", "{ ", " }"));
    }

    /**
     * How a constraint is built, which is the validator the form library knows
     * by the name of the annotation: with what the annotation says, as the one
     * value it says when that is all there is to it.
     */
    private String validator(ConstraintModel constraint) {
        var name = imports.importNamed(LIT_FORM, constraint.name(), false);
        var attributes = constraint.attributes();

        if (attributes.isEmpty()) {
            return "new " + name + "()";
        }

        if (attributes.size() == 1 && attributes.containsKey("value")) {
            return "new " + name + "(" + value(attributes.get("value")) + ")";
        }

        return "new " + name + "("
                + attributes.entrySet().stream()
                        .map(attribute -> attribute.getKey() + ": "
                                + value(attribute.getValue()))
                        .collect(Collectors.joining(", ", "{ ", " }"))
                + ")";
    }

    /**
     * A value of an annotation as TypeScript writes it. Anything the language
     * has no literal of goes as the string it reads as, which is what the
     * validators of the form library are given as well: an enum constant by its
     * name, and a class by the name of the class.
     */
    private static String value(Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }

        if (value instanceof Collection<?> values) {
            return values.stream().map(ModelWriter::value)
                    .collect(Collectors.joining(", ", "[", "]"));
        }

        if (value instanceof Object[] values) {
            return Arrays.stream(values).map(ModelWriter::value)
                    .collect(Collectors.joining(", ", "[", "]"));
        }

        if (value instanceof AnnotationParameterEnumValueModel constant) {
            return string(constant.getValueName());
        }

        if (value instanceof ClassInfoModel javaClass) {
            return string(javaClass.getName());
        }

        return string(String.valueOf(value));
    }

    /**
     * A string as TypeScript reads it, with what the language would otherwise
     * read as the end of it, or as another line, written as it says itself.
     */
    private static String string(String value) {
        return "'" + value.replace("\\", "\\\\").replace("'", "\\'")
                .replace("\n", "\\n").replace("\r", "\\r") + "'";
    }

    private static String instance(String model, boolean optional,
            String... arguments) {
        var builder = new StringBuilder("new ").append(model)
                .append("(parent, key, ").append(optional);

        for (var argument : arguments) {
            if (argument.isBlank()) {
                continue;
            }

            // An argument of its own line brings the space with it
            builder.append(argument.startsWith("\n") ? "," : ", ")
                    .append(argument);
        }

        return builder.append(')').toString();
    }
}
