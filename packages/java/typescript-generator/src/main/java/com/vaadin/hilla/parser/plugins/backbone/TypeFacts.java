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

import java.lang.reflect.AnnotatedElement;
import java.util.List;

import com.vaadin.hilla.generator.model.AnnotationModel;
import com.vaadin.hilla.generator.model.ConstraintModel;
import com.vaadin.hilla.parser.utils.Signatures;

/**
 * What the walk says about the type of a value: whether a value of it can be
 * absent, the types it is given where it takes parameters, and what its
 * annotations say about it, in the shape the generator model keeps it in.
 */
public final class TypeFacts {
    private final boolean collectsTypeArguments;
    private List<ConstraintModel> constraints = List.of();
    private List<AnnotationModel> annotations = List.of();
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
    public static TypeFacts of(AnnotatedElement type, boolean handleGenerics) {
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
     * What the annotations of the validation API say a value of the type has to
     * satisfy, which is nothing at all where none is written on it.
     */
    public List<ConstraintModel> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<ConstraintModel> constraints) {
        this.constraints = List.copyOf(constraints);
    }

    /**
     * The annotations of the value which a form model is told about, such as
     * the one saying that a value is the id of an entity.
     */
    public List<AnnotationModel> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationModel> annotations) {
        this.annotations = List.copyOf(annotations);
    }

    private static boolean canBeAbsent(AnnotatedElement type,
            boolean handleGenerics) {
        if (Signatures.isCharacter(type) || Signatures.isString(type)
                || Signatures.isBoolean(type) || Signatures.hasIntegerType(type)
                || Signatures.isBigInteger(type)
                || Signatures.hasFloatType(type)
                || Signatures.isBigDecimal(type)) {
            return !Signatures.isPrimitive(type);
        }

        if (Signatures.isArray(type) || Signatures.isIterable(type)
                || Signatures.isMap(type) || Signatures.isDate(type)
                || Signatures.isDateTime(type)) {
            return true;
        }

        if (Signatures.isClassRef(type)) {
            // A class of the JDK is a value of a type the browser has one of
            // its own for, which the walk carries below it
            return Signatures.isNonJDKClass(type);
        }

        return handleGenerics && (Signatures.isTypeVariable(type)
                || Signatures.isTypeParameter(type));
    }

    private static boolean collectsTypeArguments(AnnotatedElement type) {
        return Signatures.isClassRef(type) && !(Signatures.isIterable(type)
                || Signatures.isMap(type) || Signatures.isOptional(type)
                || Signatures.getTypeArguments(type).isEmpty());
    }
}
