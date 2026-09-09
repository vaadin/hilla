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
    record Scalar(ScalarKind kind, boolean optional,
            String javaType) implements TypeModel {
        public Scalar {
            Objects.requireNonNull(kind);
            Objects.requireNonNull(javaType);
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
    record ArrayOf(TypeModel items, boolean optional,
            String javaType) implements TypeModel {
        public ArrayOf {
            Objects.requireNonNull(items);
            Objects.requireNonNull(javaType);
        }
    }

    /**
     * @param javaType
     *            the fully qualified name of the Java type the values come in,
     *            as for an array
     */
    record MapOf(TypeModel values, boolean optional,
            String javaType) implements TypeModel {
        public MapOf {
            Objects.requireNonNull(values);
            Objects.requireNonNull(javaType);
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
            boolean optional) implements TypeModel {
        public EntityRef {
            Objects.requireNonNull(javaClass);
            typeArguments = List.copyOf(typeArguments);
        }

        public static EntityRef of(String javaClass) {
            return new EntityRef(javaClass, List.of(), false);
        }
    }

    /**
     * A reference to a type parameter of the declaration being written, which
     * is written as it is.
     */
    record TypeVariable(String name, boolean optional) implements TypeModel {
        public TypeVariable {
            Objects.requireNonNull(name);
        }

        public static TypeVariable of(String name) {
            return new TypeVariable(name, false);
        }
    }
}
