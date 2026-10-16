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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.vaadin.hilla.generator.model.ConstraintModel;

/**
 * The constraints the model library declares, by the name of the annotation of
 * the validation API they stand for: which kinds of model each one accepts, and
 * which attributes it takes.
 *
 * <p>
 * A constraint the model library does not accept on a model throws when the
 * generated module is evaluated, and one it does not export does not compile,
 * so a constraint is only written where the model library takes it. What the
 * server validates is not affected.
 */
final class ModelConstraints {
    /**
     * The kinds of model the one of a value can be.
     */
    enum Kind {
        STRING, NUMBER, BOOLEAN, ARRAY, RECORD, OBJECT, UNKNOWN
    }

    private record Declaration(Set<Kind> kinds, Set<String> attributes) {
    }

    private static final Map<String, Declaration> DECLARATIONS = Map.ofEntries(
            any("Null"), any("NotNull"),
            declare("AssertTrue", EnumSet.of(Kind.BOOLEAN)),
            declare("AssertFalse", EnumSet.of(Kind.BOOLEAN)),
            declare("Min", EnumSet.of(Kind.NUMBER), "value"),
            declare("Max", EnumSet.of(Kind.NUMBER), "value"),
            declare("DecimalMin", EnumSet.of(Kind.NUMBER, Kind.STRING), "value",
                    "inclusive"),
            declare("DecimalMax", EnumSet.of(Kind.NUMBER, Kind.STRING), "value",
                    "inclusive"),
            declare("Negative", EnumSet.of(Kind.NUMBER)),
            declare("NegativeOrZero", EnumSet.of(Kind.NUMBER)),
            declare("Positive", EnumSet.of(Kind.NUMBER)),
            declare("PositiveOrZero", EnumSet.of(Kind.NUMBER)),
            declare("Size", EnumSet.of(Kind.STRING, Kind.ARRAY), "min", "max"),
            declare("Digits", EnumSet.of(Kind.STRING, Kind.NUMBER), "integer",
                    "fraction"),
            declare("Past", EnumSet.of(Kind.STRING)),
            declare("Future", EnumSet.of(Kind.STRING)),
            declare("Pattern", EnumSet.of(Kind.STRING), "regexp"),
            declare("NotEmpty",
                    EnumSet.of(Kind.STRING, Kind.ARRAY, Kind.RECORD)),
            declare("NotBlank", EnumSet.of(Kind.STRING)),
            declare("Email", EnumSet.of(Kind.STRING)));

    private ModelConstraints() {
    }

    /**
     * Whether a constraint can be applied to a model of the given kind. The
     * model library also declares {@code PastOrPresent} and
     * {@code FutureOrPresent}, but the form binder has no validator for either
     * and fails to build the form when given one, so they are never applicable.
     */
    static boolean isApplicable(ConstraintModel constraint, Kind kind) {
        var declaration = DECLARATIONS.get(constraint.name());

        return declaration != null && declaration.kinds().contains(kind);
    }

    /**
     * The attributes of a constraint the model library declares, in the order
     * the constraint gives them.
     */
    static Map<String, Object> declaredAttributes(ConstraintModel constraint) {
        var declaration = DECLARATIONS.get(constraint.name());
        var attributes = new LinkedHashMap<String, Object>();

        constraint.attributes().forEach((name, value) -> {
            if (declaration.attributes().contains(name)) {
                attributes.put(name, value);
            }
        });

        return attributes;
    }

    private static Map.Entry<String, Declaration> any(String name) {
        return declare(name, EnumSet.allOf(Kind.class));
    }

    /**
     * Every constraint takes the message to show as well as what it declares
     * itself.
     */
    private static Map.Entry<String, Declaration> declare(String name,
            Set<Kind> kinds, String... attributes) {
        var declared = new HashSet<>(Set.of(attributes));
        declared.add("message");

        return Map.entry(name,
                new Declaration(Set.copyOf(kinds), Set.copyOf(declared)));
    }
}
