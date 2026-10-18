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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.vaadin.hilla.generator.model.AnnotationModel;
import com.vaadin.hilla.generator.model.ConstraintModel;
import com.vaadin.hilla.generator.model.TypeModel;

/**
 * Writes the model of a value: the expression which builds it out of the models
 * and the converters of the model library.
 *
 * <p>
 * The converters wrap the model of the type from the inside out: the model of
 * the values of an array or of a map is wrapped by {@code m.array} or
 * {@code m.record}, then by {@code m.optional} when the value can be absent,
 * then by {@code m.constrained} and last by {@code m.meta}.
 */
final class ModelWriter {
    static final String MODULE = "@vaadin/hilla-models";
    private static final Set<String> PRIMITIVES = Set.of("boolean", "byte",
            "char", "double", "float", "int", "long", "short");
    private static final Pattern IDENTIFIER = Pattern
            .compile("[A-Za-z_$][A-Za-z0-9_$]*");

    private final ImportRegistry imports;
    private final String directory;
    private final String owner;
    private final ModelCycles cycles;

    /**
     * @param imports
     *            the imports of the file being written
     * @param directory
     *            the folder of the file being written, relative to the output
     *            folder, which decides how the other models are referred to
     */
    ModelWriter(ImportRegistry imports, String directory) {
        this(imports, directory, null, ModelCycles.NONE);
    }

    /**
     * @param owner
     *            the fully qualified name of the class whose model is being
     *            written, which a property holding a value of the same class
     *            refers to as itself
     * @param cycles
     *            the references between the models which have to be read
     *            lazily, since the modules of the models import each other
     */
    ModelWriter(ImportRegistry imports, String directory, String owner,
            ModelCycles cycles) {
        this.imports = imports;
        this.directory = directory;
        this.owner = owner;
        this.cycles = cycles;
    }

    /**
     * How the model of a value is built, which a model of an object is given as
     * the model of the property holding the value.
     */
    String model(TypeModel type) {
        var model = switch (type) {
        case TypeModel.Scalar scalar -> scalarModel(scalar.kind());
        case TypeModel.ArrayOf array -> call("array", model(array.items()));
        case TypeModel.MapOf map -> call("record", model(map.values()));
        case TypeModel.EntityRef entity -> reference(entity);
        // A value the framework provides is not bound as properties: it is
        // whatever the module exporting it makes of it
        case TypeModel.Provided provided -> named("ObjectModel");
        // A type variable holds whatever the declaration is used with, which
        // is nothing the model can be told about
        case TypeModel.TypeVariable variable -> named("Model");
        };

        if (type.optional()) {
            model = call("optional", model);
        }

        var constraints = type
                .constraints().stream().filter(constraint -> ModelConstraints
                        .isApplicable(constraint, kind(type)))
                .map(this::constraint).toList();

        if (!constraints.isEmpty()) {
            var arguments = new ArrayList<String>();
            arguments.add(model);
            arguments.addAll(constraints);
            model = call("constrained", arguments.toArray(String[]::new));
        }

        var meta = meta(type);

        return meta.isEmpty() ? model : call("meta", model, meta);
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

    /**
     * The namespace of the model library, which holds the builders and the
     * converters.
     */
    String m() {
        return imports.importDefault(MODULE, "m", false);
    }

    /**
     * A named export of the model library, such as {@code StringModel}.
     */
    String named(String name) {
        return imports.importNamed(MODULE, name, false);
    }

    /**
     * A model referring to the model of a generated type. The model of the
     * class being written is itself, which needs no import, and a model which
     * refers back to the one being written, at any depth, is read only once it
     * is used, since the modules of both import each other.
     */
    private String reference(TypeModel.EntityRef entity) {
        if (entity.javaClass().equals(owner)) {
            return m() + ".self";
        }

        var model = name(entity);

        if (!cycles.isDeferred(owner, entity.javaClass())) {
            return model;
        }

        // The provider says what it returns, since the type of a model cannot
        // be inferred from a model whose type is inferred from it in turn
        var type = imports.importDefault(
                ModulePaths.forEntity(entity.javaClass(), directory),
                ModulePaths.entityName(entity.javaClass()), true);

        return call("lazy",
                "(): " + named("ObjectModel") + "<" + type + "> => " + model);
    }

    private String scalarModel(TypeModel.ScalarKind kind) {
        return switch (kind) {
        case STRING -> named("StringModel");
        case NUMBER -> named("NumberModel");
        case BOOLEAN -> named("BooleanModel");
        case UNKNOWN, VOID -> named("Model");
        };
    }

    /**
     * The kind of model the one of a value is, which decides the constraints
     * the model library accepts on it.
     */
    private static ModelConstraints.Kind kind(TypeModel type) {
        return switch (type) {
        case TypeModel.Scalar scalar -> switch (scalar.kind()) {
        case STRING -> ModelConstraints.Kind.STRING;
        case NUMBER -> ModelConstraints.Kind.NUMBER;
        case BOOLEAN -> ModelConstraints.Kind.BOOLEAN;
        case UNKNOWN, VOID -> ModelConstraints.Kind.UNKNOWN;
        };
        case TypeModel.ArrayOf array -> ModelConstraints.Kind.ARRAY;
        case TypeModel.MapOf map -> ModelConstraints.Kind.RECORD;
        case TypeModel.EntityRef entity -> ModelConstraints.Kind.OBJECT;
        case TypeModel.Provided provided -> ModelConstraints.Kind.OBJECT;
        case TypeModel.TypeVariable variable -> ModelConstraints.Kind.UNKNOWN;
        };
    }

    /**
     * What a model is told about the value it holds: the annotations of the
     * value which a form or a grid built from the model reads, and the Java
     * type it comes in, which TypeScript has no way of telling apart. Nothing
     * at all when the model is told nothing.
     */
    private String meta(TypeModel type) {
        var written = new ArrayList<String>();

        if (!type.annotations().isEmpty()) {
            written.add("annotations: "
                    + type.annotations().stream().map(ModelWriter::annotation)
                            .collect(Collectors.joining(", ", "[", "]")));
        }

        var javaType = switch (type) {
        case TypeModel.Scalar scalar -> scalar.javaType();
        case TypeModel.ArrayOf array -> array.javaType();
        case TypeModel.MapOf map -> map.javaType();
        // A model of a generated type, of a type the framework provides, or
        // of whatever a type variable holds, is what tells which type it is
        case TypeModel.EntityRef entity -> null;
        case TypeModel.Provided provided -> null;
        case TypeModel.TypeVariable variable -> null;
        };

        if (isJavaTypeWritten(javaType)) {
            written.add("jvmType: " + string(javaType));
        }

        return written.isEmpty() ? ""
                : written.stream()
                        .collect(Collectors.joining(", ", "{ ", " }"));
    }

    private static String annotation(AnnotationModel annotation) {
        var written = new ArrayList<String>();
        written.add("jvmType: " + string(annotation.name()));

        if (!annotation.attributes().isEmpty()) {
            written.add("attributes: " + value(annotation.attributes()));
        }

        return written.stream().collect(Collectors.joining(", ", "{ ", " }"));
    }

    /**
     * Whether the Java type of a value is one a model is told about: a
     * primitive or a type of the JDK, or an array of one. A class of the
     * application, or an array of arrays, is left out, so that the client is
     * not told the names of the classes behind it, and a grid falls back to the
     * model it is built from.
     */
    private static boolean isJavaTypeWritten(String javaType) {
        if (javaType == null) {
            return false;
        }

        var type = javaType.endsWith("[]")
                ? javaType.substring(0, javaType.length() - 2)
                : javaType;

        return PRIMITIVES.contains(type) || type.startsWith("java.");
    }

    /**
     * How a constraint is declared, which is the function the model library
     * exports under the name of the annotation: with what the annotation says,
     * as the one value it says when that is all there is to it. An attribute
     * the model library does not declare is left out, since a constraint takes
     * exactly the attributes it declares.
     */
    private String constraint(ConstraintModel constraint) {
        var name = named(constraint.name());
        var attributes = ModelConstraints.declaredAttributes(constraint);

        if (attributes.isEmpty()) {
            return name + "()";
        }

        if (attributes.size() == 1 && attributes.containsKey("value")) {
            return name + "(" + value(attributes.get("value")) + ")";
        }

        return name + "(" + value(attributes) + ")";
    }

    /**
     * A value of an annotation as TypeScript writes it. An enum constant comes
     * as its name.
     */
    private static String value(Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }

        if (value instanceof Collection<?> values) {
            return values.stream().map(ModelWriter::value)
                    .collect(Collectors.joining(", ", "[", "]"));
        }

        if (value instanceof Map<?, ?> attributes) {
            return attributes.entrySet().stream()
                    .map(attribute -> key(String.valueOf(attribute.getKey()))
                            + ": " + value(attribute.getValue()))
                    .collect(Collectors.joining(", ", "{ ", " }"));
        }

        return string(String.valueOf(value));
    }

    private static String key(String name) {
        return IDENTIFIER.matcher(name).matches() ? name : string(name);
    }

    /**
     * A string as TypeScript reads it, with what the language would otherwise
     * read as the end of it, or as another line, written as it says itself.
     */
    static String string(String value) {
        return "'" + value.replace("\\", "\\\\").replace("'", "\\'")
                .replace("\n", "\\n").replace("\r", "\\r") + "'";
    }

    private String call(String converter, String... arguments) {
        return m() + "." + converter + "(" + String.join(", ", arguments) + ")";
    }
}
