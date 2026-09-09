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
package com.vaadin.hilla.generator.model;

import java.util.List;
import java.util.Objects;

/**
 * A type as the TypeScript generator needs it: what to write, with the
 * questions a Java type raises already answered.
 *
 * <p>
 * Notably, whether a value can be absent is decided here, from the Java type,
 * the nullability annotations and the Kotlin metadata, rather than being
 * rediscovered by whatever writes the TypeScript.
 */
public sealed interface TypeModel {
    /**
     * Whether the value may be absent, which TypeScript spells as a union with
     * {@code undefined}.
     */
    boolean optional();

    /**
     * What a value of the type has to satisfy, which a form model binds as the
     * validators of the property holding it. The constraints belong to the type
     * rather than to the property, since the values of a collection can be
     * constrained as well as the collection itself.
     */
    List<ConstraintModel> constraints();

    /**
     * The annotations of a value which a form model is told about, by their
     * fully qualified name: whether a value is the id of an entity is nothing
     * TypeScript can see, and a form or a grid built from the model decides
     * with it what to do with the value.
     */
    List<String> annotations();

    /**
     * The types which TypeScript expresses without a declaration of their own.
     */
    enum ScalarKind {
        STRING, NUMBER, BOOLEAN, UNKNOWN, VOID
    }

    /**
     * @param javaType
     *            the fully qualified name of the Java type the value comes
     *            from, which TypeScript has no way of telling apart: a date and
     *            an instant are both written as a string
     */
    record Scalar(ScalarKind kind, boolean optional, String javaType,
            List<ConstraintModel> constraints,
            List<String> annotations) implements TypeModel {
        public Scalar {
            Objects.requireNonNull(kind);
            Objects.requireNonNull(javaType);
            constraints = List.copyOf(constraints);
            annotations = List.copyOf(annotations);
        }

        public Scalar(ScalarKind kind, boolean optional, String javaType) {
            this(kind, optional, javaType, List.of(), List.of());
        }

        public static Scalar of(ScalarKind kind, String javaType) {
            return new Scalar(kind, false, javaType);
        }
    }

    /**
     * @param javaType
     *            the fully qualified name of the Java type the values come in,
     *            which a form model tells its bindings about: an array, a list
     *            and a set are all an array in TypeScript
     */
    record ArrayOf(TypeModel items, boolean optional, String javaType,
            List<ConstraintModel> constraints,
            List<String> annotations) implements TypeModel {
        public ArrayOf {
            Objects.requireNonNull(items);
            Objects.requireNonNull(javaType);
            constraints = List.copyOf(constraints);
            annotations = List.copyOf(annotations);
        }

        public ArrayOf(TypeModel items, boolean optional, String javaType) {
            this(items, optional, javaType, List.of(), List.of());
        }
    }

    /**
     * @param javaType
     *            the fully qualified name of the Java type the values come in,
     *            as for an array
     */
    record MapOf(TypeModel values, boolean optional, String javaType,
            List<ConstraintModel> constraints,
            List<String> annotations) implements TypeModel {
        public MapOf {
            Objects.requireNonNull(values);
            Objects.requireNonNull(javaType);
            constraints = List.copyOf(constraints);
            annotations = List.copyOf(annotations);
        }

        public MapOf(TypeModel values, boolean optional, String javaType) {
            this(values, optional, javaType, List.of(), List.of());
        }
    }

    /**
     * A reference to a type generated as a TypeScript declaration of its own.
     *
     * @param javaClass
     *            the fully qualified name of the Java class, which decides both
     *            the name and the location of the generated file
     */
    record EntityRef(String javaClass, List<TypeModel> typeArguments,
            boolean optional, List<ConstraintModel> constraints,
            List<String> annotations) implements TypeModel {
        public EntityRef {
            Objects.requireNonNull(javaClass);
            typeArguments = List.copyOf(typeArguments);
            constraints = List.copyOf(constraints);
            annotations = List.copyOf(annotations);
        }

        public EntityRef(String javaClass, List<TypeModel> typeArguments,
                boolean optional) {
            this(javaClass, typeArguments, optional, List.of(), List.of());
        }

        public static EntityRef of(String javaClass) {
            return new EntityRef(javaClass, List.of(), false);
        }
    }

    /**
     * A type the generated TypeScript refers to by name rather than by
     * declaring it: one the browser has, such as a file, or one a module of the
     * framework exports, such as a signal.
     *
     * @param name
     *            the name the type goes by in TypeScript
     * @param module
     *            the module exporting it under that name, or empty for one the
     *            browser has
     */
    record Provided(String name, String module, List<TypeModel> typeArguments,
            boolean optional, List<ConstraintModel> constraints,
            List<String> annotations) implements TypeModel {
        public Provided {
            Objects.requireNonNull(name);
            Objects.requireNonNull(module);
            typeArguments = List.copyOf(typeArguments);
            constraints = List.copyOf(constraints);
            annotations = List.copyOf(annotations);
        }

        public Provided(String name, String module,
                List<TypeModel> typeArguments, boolean optional) {
            this(name, module, typeArguments, optional, List.of(), List.of());
        }
    }

    /**
     * A reference to a type parameter of the declaration being written, which
     * is written as it is.
     */
    record TypeVariable(String name, boolean optional,
            List<ConstraintModel> constraints,
            List<String> annotations) implements TypeModel {
        public TypeVariable {
            Objects.requireNonNull(name);
            constraints = List.copyOf(constraints);
            annotations = List.copyOf(annotations);
        }

        public TypeVariable(String name, boolean optional) {
            this(name, optional, List.of(), List.of());
        }

        public static TypeVariable of(String name) {
            return new TypeVariable(name, false);
        }
    }
}
