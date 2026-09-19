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
 * A type which the endpoints refer to and which is generated as a TypeScript
 * declaration of its own, such as the type of a parameter or of a return value.
 *
 * <p>
 * The two kinds are what TypeScript has a different declaration for: a type
 * with properties, which becomes an interface, and an enum, which becomes an
 * enum of the values it is serialized as.
 */
public sealed interface EntityModel {
    /**
     * The fully qualified name of the Java class, which decides both the name
     * and the location of the generated file.
     */
    String javaClass();

    /**
     * A type whose values are objects with properties.
     *
     * @param typeParameters
     *            the names of the type parameters the declaration takes, which
     *            its properties refer to
     * @param superTypes
     *            the types the declaration extends, which hold the properties
     *            this one inherits rather than declares
     */
    record Bean(String javaClass, List<String> typeParameters,
            List<TypeModel.EntityRef> superTypes,
            List<PropertyModel> properties) implements EntityModel {
        public Bean {
            Objects.requireNonNull(javaClass);
            typeParameters = List.copyOf(typeParameters);
            superTypes = List.copyOf(superTypes);
            properties = List.copyOf(properties);
        }
    }

    /**
     * A type whose values are one of a set of constants.
     *
     * @param constants
     *            the names of the constants, which are also the values they are
     *            serialized as
     */
    record Enumeration(String javaClass,
            List<String> constants) implements EntityModel {
        public Enumeration {
            Objects.requireNonNull(javaClass);
            constants = List.copyOf(constants);
        }
    }
}
