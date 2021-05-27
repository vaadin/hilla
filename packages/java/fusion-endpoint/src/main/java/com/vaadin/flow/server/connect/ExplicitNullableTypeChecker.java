/*
 * Copyright 2000-2021 Vaadin Ltd.
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

package com.vaadin.flow.server.connect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

/**
 * A checker for TypeScript null compatibility in Vaadin endpoint methods
 * parameter and return types.
 */
public class ExplicitNullableTypeChecker {
    /**
     * Checks if the element should be required (not nullable) in the generated
     * Typescript code.
     * 
     * @param element
     *            an element to be required
     * @return a result of check
     */
    public static boolean isRequired(AnnotatedElement element) {
        return Stream.of(element.getAnnotations())
                .anyMatch(annotation -> "nonnull".equalsIgnoreCase(
                        annotation.annotationType().getSimpleName()));
    }

    /**
     * Checks if the element should be required (not nullable) in the generated
     * Typescript code.
     *
     * @param node
     *            a node to be required
     * @return a result of check
     */
    public static boolean isRequired(NodeWithAnnotations<?> node) {
        return node.getAnnotations().stream().anyMatch(annotation -> "nonnull"
                .equalsIgnoreCase(annotation.getName().getIdentifier()));
    }

    /**
     * Validates the given value for the given expected method return value
     * type.
     *
     * @param value
     *            the value to validate
     * @param annotatedElement
     *            the entity to be type checked
     * @return error message when the value is null while the expected type does
     *         not explicitly allow null, or null meaning the value is OK.
     */
    public String checkValueForAnnotatedElement(Object value,
            AnnotatedElement annotatedElement) {
        if (!isRequired(annotatedElement)) {
            return null;
        }
        if (annotatedElement instanceof Method) {
            return checkValueForType(value,
                    ((Method) annotatedElement).getGenericReturnType());
        }
        return null;
    }

    String checkValueForType(Object value, Type expectedType) {
        return new ExplicitNullableTypeCheckerHelper().checkValueForType(value,
                expectedType);
    }
}
