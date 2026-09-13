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
import java.util.Optional;

/**
 * The subtypes a polymorphic type is one of, which TypeScript can say as a
 * union while Java says it as a hierarchy.
 *
 * <p>
 * It is generated next to the type itself rather than instead of it: the type
 * says what every one of the subtypes has, and the union says which of them a
 * value can be, so that reading the discriminator of a value tells TypeScript
 * which subtype it is looking at.
 *
 * @param javaClass
 *            the fully qualified name of the Java class declaring the subtypes
 * @param subTypes
 *            the types a value can be, in the order they are declared in
 */
public record UnionModel(String javaClass, List<Member> subTypes) {
    public UnionModel {
        Objects.requireNonNull(javaClass);
        subTypes = List.copyOf(subTypes);
    }

    /**
     * One of the types a value can be.
     *
     * @param narrowedTo
     *            the value of the discriminator which tells this subtype from
     *            the ones below it, which is needed when it accepts theirs as
     *            well: a value of it would otherwise be a value of any of them
     */
    public record Member(TypeModel.EntityRef type,
            Optional<EntityModel.Discriminator> narrowedTo) {
        public Member {
            Objects.requireNonNull(type);
            Objects.requireNonNull(narrowedTo);
        }

        public static Member of(TypeModel.EntityRef type) {
            return new Member(type, Optional.empty());
        }
    }
}
