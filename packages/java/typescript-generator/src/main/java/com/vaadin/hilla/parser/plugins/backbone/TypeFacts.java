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
package com.vaadin.hilla.parser.plugins.backbone;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.models.SignatureModel;

/**
 * What the walk says about the type of a value: whether a value of it can be
 * absent, the types it is given where it takes parameters, and whatever else a
 * plugin has to say about it, which the plugin reading it back knows the shape
 * of.
 */
public final class TypeFacts {
    private final List<TypeFacts> typeArguments = new ArrayList<>();
    private final Map<String, Object> notes = new LinkedHashMap<>();
    private final boolean collectsTypeArguments;
    private boolean optional;

    private TypeFacts(boolean optional, boolean collectsTypeArguments) {
        this.optional = optional;
        this.collectsTypeArguments = collectsTypeArguments;
    }

    /**
     * What is known about a type before any plugin has said anything: whether a
     * value of it can be absent, which is what Java says unless an annotation
     * says otherwise, and whether the types it is given belong to it rather
     * than to the value it holds.
     *
     * @param type
     *            the type a value is of
     * @param handleGenerics
     *            whether a type variable stands for itself, which it does in an
     *            entity and does not in the signature of an endpoint method
     */
    public static TypeFacts of(SignatureModel type, boolean handleGenerics) {
        return new TypeFacts(canBeAbsent(type, handleGenerics),
                collectsTypeArguments(type));
    }

    /**
     * What is known about a type nothing has been walked for.
     */
    public static TypeFacts unknown() {
        return new TypeFacts(false, false);
    }

    /**
     * Whether the types this one is given are its own, which they are not for
     * the types the browser has one of its own for: the value of an optional,
     * the items of an iterable and the values of a map are the type the walk
     * carries below it instead.
     */
    public boolean collectsTypeArguments() {
        return collectsTypeArguments;
    }

    /**
     * The types this one is given, in the order it takes them.
     */
    public List<TypeFacts> getTypeArguments() {
        return typeArguments;
    }

    /**
     * Whether a value of the type can be absent, which is what the generated
     * TypeScript says with a union with undefined.
     */
    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * Says something about the type under the given name, which the plugin
     * reading it back knows the shape of. Saying it again replaces what was
     * said before.
     */
    public void note(String name, Object value) {
        notes.put(name, value);
    }

    /**
     * What a plugin has said about the type under the given name as values of
     * the given type, which is nothing at all where nothing was said.
     */
    public <T> Stream<T> notes(String name, Class<T> type) {
        return notes.get(name) instanceof List<?> values
                ? values.stream().filter(type::isInstance).map(type::cast)
                : Stream.of();
    }

    private static boolean canBeAbsent(SignatureModel type,
            boolean handleGenerics) {
        if (type.isCharacter() || type.isString() || type.isBoolean()
                || type.hasIntegerType() || type.isBigInteger()
                || type.hasFloatType() || type.isBigDecimal()) {
            return !type.isPrimitive();
        }

        if (type.isArray() || type.isIterable() || type.isMap() || type.isDate()
                || type.isDateTime()) {
            return true;
        }

        if (type.isClassRef()) {
            // A class of the JDK is a value of a type the browser has one of
            // its own for, which the walk carries below it
            return type.isNonJDKClass();
        }

        return handleGenerics
                && (type.isTypeVariable() || type.isTypeParameter());
    }

    private static boolean collectsTypeArguments(SignatureModel type) {
        return type instanceof ClassRefSignatureModel ref
                && !(ref.isIterable() || ref.isMap() || ref.isOptional()
                        || ref.getTypeArguments().isEmpty());
    }
}
