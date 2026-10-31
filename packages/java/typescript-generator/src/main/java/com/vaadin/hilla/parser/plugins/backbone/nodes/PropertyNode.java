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
package com.vaadin.hilla.parser.plugins.backbone.nodes;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.introspect.BeanPropertyDefinition;

import com.vaadin.hilla.generator.model.TypeModel;
import com.vaadin.hilla.parser.core.AbstractNode;

public class PropertyNode extends AbstractNode<BeanPropertyDefinition, String>
        implements AnnotatedNode {
    private TypeModel.Builder valueType = TypeModel.Builder.unknown();

    protected PropertyNode(@NonNull BeanPropertyDefinition source,
            @NonNull String target) {
        super(source, target);
    }

    /**
     * What the walk says about the type of the value the property holds, which
     * the walk carries as a node below this one. Until that node is left, it is
     * what is known about a type nothing has been walked for.
     */
    public TypeModel.Builder getValueType() {
        return valueType;
    }

    public void setValueType(TypeModel.Builder valueType) {
        this.valueType = valueType;
    }

    /**
     * The annotations of the field, the getter and the setter parameter of the
     * property, in this order.
     */
    @Override
    public List<Annotation> getAnnotations() {
        var property = getSource();

        return Stream
                .of(field(property).map(Field::getAnnotations),
                        getter(property).map(Method::getAnnotations),
                        setter(property).map(setter -> setter.getParameters()[0]
                                .getAnnotations()))
                .flatMap(Optional::stream).flatMap(Arrays::stream).toList();
    }

    /**
     * The types the getter returns, the setter takes and the field holds, in
     * this order, for those the property has.
     */
    public List<AnnotatedType> getAssociatedTypes() {
        var property = getSource();

        return Stream
                .of(getter(property).map(Method::getAnnotatedReturnType),
                        setter(property).map(setter -> setter.getParameters()[0]
                                .getAnnotatedType()),
                        field(property).map(Field::getAnnotatedType))
                .flatMap(Optional::stream).toList();
    }

    /**
     * The member the property is read through: the getter, or else the setter,
     * or else the field.
     */
    public Member getPrimaryMember() {
        var property = getSource();

        return getter(property).<Member> map(Function.identity())
                .or(() -> setter(property)).or(() -> field(property))
                .orElseThrow();
    }

    @Override
    protected String getSourceName() {
        return getSource().getName();
    }

    @NonNull
    static public PropertyNode of(@NonNull BeanPropertyDefinition source) {
        return new PropertyNode(source, "");
    }

    private static Optional<Field> field(BeanPropertyDefinition property) {
        return property.hasField()
                ? Optional.of(property.getField().getAnnotated())
                : Optional.empty();
    }

    private static Optional<Method> getter(BeanPropertyDefinition property) {
        return property.hasGetter()
                ? Optional.of(property.getGetter().getAnnotated())
                : Optional.empty();
    }

    private static Optional<Method> setter(BeanPropertyDefinition property) {
        return property.hasSetter()
                ? Optional.of(property.getSetter().getAnnotated())
                : Optional.empty();
    }
}
