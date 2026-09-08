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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.hilla.parser.models.ArraySignatureModel;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.models.SignatureModel;
import com.vaadin.hilla.parser.models.SpecializedModel;
import com.vaadin.hilla.parser.models.TypeArgumentModel;
import com.vaadin.hilla.parser.models.TypeParameterModel;
import com.vaadin.hilla.parser.models.TypeVariableModel;

/**
 * Reading the bounds of a type parameter.
 */
final class TypeParameters {
    private TypeParameters() {
    }

    /**
     * Returns the bounds of a type parameter which say something about it.
     *
     * <p>
     * A bound is left out when it leads back to the type parameter it belongs
     * to, which is the shape of an F-bounded type parameter such as the
     * {@code <E extends Enum<E>>} of {@link Enum} itself, and the only way to
     * constrain a type parameter to be an enum. Such a bound describes nothing
     * beyond the type parameter, and following it never ends, so the type
     * parameter is treated as if the bound were not there.
     *
     * @param typeParameter
     *            the type parameter to read the bounds of
     * @return the bounds which do not lead back to the type parameter
     */
    static List<SignatureModel> getEffectiveBounds(
            TypeParameterModel typeParameter) {
        return typeParameter.getBounds().stream().filter(Objects::nonNull)
                .filter(bound -> !leadsTo(bound, typeParameter.getName(),
                        new HashSet<>()))
                .collect(Collectors.toList());
    }

    /**
     * Checks whether a type parameter constrains its type in no way, which is
     * what makes it a type parameter of the generated type.
     *
     * @param typeParameter
     *            the type parameter to check
     * @return {@code true} if the type parameter has no effective bound beyond
     *         {@code Object}
     */
    static boolean isUnbounded(TypeParameterModel typeParameter) {
        return getEffectiveBounds(typeParameter).stream()
                .allMatch(SpecializedModel::isNativeObject);
    }

    /**
     * Checks whether a signature uses the named type parameter, directly or
     * through the bounds of the type parameters it uses on the way.
     *
     * @param signature
     *            the signature to look into
     * @param typeParameterName
     *            the name of the type parameter to look for
     * @param visited
     *            the names of the type parameters already followed, which keeps
     *            a set of type parameters bounded by each other from being
     *            followed forever
     */
    private static boolean leadsTo(SignatureModel signature,
            String typeParameterName, Set<String> visited) {
        if (signature.isTypeVariable()) {
            var name = ((TypeVariableModel) signature).getName();

            if (name.equals(typeParameterName)) {
                return true;
            }

            return visited.add(name) && ((TypeVariableModel) signature)
                    .resolve().getBounds().stream().filter(Objects::nonNull)
                    .anyMatch(bound -> leadsTo(bound, typeParameterName,
                            visited));
        } else if (signature.isTypeArgument()) {
            return ((TypeArgumentModel) signature).getAssociatedTypes().stream()
                    .anyMatch(
                            type -> leadsTo(type, typeParameterName, visited));
        } else if (signature.isClassRef()) {
            return ((ClassRefSignatureModel) signature).getTypeArguments()
                    .stream().anyMatch(typeArgument -> leadsTo(typeArgument,
                            typeParameterName, visited));
        } else if (signature.isArray()) {
            return leadsTo(((ArraySignatureModel) signature).getNestedType(),
                    typeParameterName, visited);
        }

        return false;
    }
}
