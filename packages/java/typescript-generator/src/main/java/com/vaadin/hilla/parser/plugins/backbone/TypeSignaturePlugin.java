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

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.generator.model.TypeModel;
import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.CompositeTypeSignatureNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;
import com.vaadin.hilla.parser.utils.Generics;
import com.vaadin.hilla.parser.utils.Signatures;

public final class TypeSignaturePlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof TypedNode typedNode) {
            var type = typedNode.getType();

            typedNode.setTarget(TypeModel.Builder.of(canBeAbsent(type,
                    // Only deal with generics in entities: endpoint methods are
                    // not allowed to emit generic type parameters and arguments
                    isInEntity(nodePath)), collectsTypeArguments(type)));
        }
    }

    /**
     * Whether a value of the type can be absent, which is what Java says unless
     * an annotation says otherwise.
     *
     * @param handleGenerics
     *            whether a type variable stands for itself, which it does in an
     *            entity and does not in the signature of an endpoint method
     */
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
            // A class of the JDK is a value of a type the browser has one
            // of its own for, which the walk carries below it
            return Signatures.isNonJDKClass(type);
        }

        return handleGenerics && (Signatures.isTypeVariable(type)
                || Signatures.isTypeParameter(type));
    }

    /**
     * Whether the types a type is given belong to it rather than to the value
     * it holds, which they do not for the types the browser has one of its own
     * for: the value of an optional, the items of an iterable and the values of
     * a map.
     */
    private static boolean collectsTypeArguments(AnnotatedElement type) {
        return Signatures.isClassRef(type) && !(Signatures.isIterable(type)
                || Signatures.isMap(type) || Signatures.isOptional(type)
                || Signatures.getTypeArguments(type).isEmpty());
    }

    // Checks if the current node is inside an entity
    private boolean isInEntity(NodePath<?> nodePath) {
        for (var np = nodePath; !(np.getNode() instanceof RootNode); np = np
                .getParentPath()) {
            if (np.getNode() instanceof EntityNode) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        if (!(nodePath.getNode() instanceof TypedNode)) {
            return;
        }

        var node = (TypedNode) nodePath.getNode();
        var type = node.getTarget();
        var parentNode = nodePath.getParentPath().getNode();

        if (parentNode instanceof PropertyNode propertyNode) {
            propertyNode.setValueType(type);
        } else if (parentNode instanceof TypedNode parentTypedNode) {
            attachToNestingParentSignature(type, parentTypedNode);
        }
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();
        if (node instanceof MethodNode) {
            return scanMethodNode((MethodNode) node, nodeDependencies);
        } else if (node instanceof MethodParameterNode) {
            return scanMethodParameter((MethodParameterNode) node,
                    nodeDependencies);
        } else if (node instanceof EntityNode) {
            return scanEntity((EntityNode) node, nodeDependencies);
        } else if (node instanceof PropertyNode) {
            return scanProperty((PropertyNode) node, nodeDependencies);
        } else if (node instanceof CompositeTypeSignatureNode) {
            return scanCompositeTypeSignature((CompositeTypeSignatureNode) node,
                    nodeDependencies);
        } else if (node instanceof TypeSignatureNode) {
            return scanTypeSignature((TypeSignatureNode) node,
                    nodeDependencies);
        }

        return nodeDependencies;
    }

    /**
     * Says what the walk found about a type below one which is written from it:
     * the items of an array and the values of a map are the type itself, the
     * types a class is given belong to it, and a type argument, a type variable
     * or an optional is written as the type it stands for.
     */
    private void attachToNestingParentSignature(TypeModel.Builder type,
            TypedNode parentNode) {
        var parentType = parentNode.getType();

        if (Signatures.isArray(parentType) || Signatures.isIterable(parentType)
                || Signatures.isMap(parentType)) {
            return;
        }

        if (!parentNode.getTarget().collectsTypeArguments()) {
            parentNode.setTarget(type);
        }
    }

    /**
     * If a given type signature refers other types, returns other type
     * signatures that are referred. These include:
     *
     * <ul>
     * <li>Array nested type</li>
     * <li>Type argument of an optional and iterable types</li>
     * <li>Item type for a map</li>
     * <li>Known type parameter for a type variable</li>
     * <li>Known bounds for a type parameter, except the ones leading back to
     * it</li>
     * </ul>
     *
     * <p>
     * Returns empty list if no referred types are found.
     * </p>
     *
     * @param signature
     *            the singature to consider
     * @return list of referred types
     */
    private List<AnnotatedElement> getReferredTypes(
            AnnotatedElement signature) {
        var items = List.<AnnotatedElement> of();
        var typeArguments = Signatures.getTypeArguments(signature);

        if (signature instanceof AnnotatedArrayType array) {
            items = List.of(array.getAnnotatedGenericComponentType());
        } else if (Signatures.isIterable(signature)) {
            if (!typeArguments.isEmpty()) {
                items = List.of(typeArguments.get(0));
            } else {
                // Let's deal with classes extending or implementing an iterator
                var cls = Signatures.getClass(signature).orElseThrow();
                items = Generics.getExactIterableType(cls).map(type -> List
                        .<AnnotatedElement> of(Signatures.of((Class<?>) type)))
                        .orElse(items);
            }
        } else if (Signatures.isOptional(signature)) {
            if (!typeArguments.isEmpty()) {
                items = List.of(typeArguments.get(0));
            }
        } else if (Signatures.isMap(signature)) {
            if (!typeArguments.isEmpty()) {
                items = List.of(typeArguments.get(1));
            }
        } else if (signature instanceof AnnotatedWildcardType wildcard) {
            var bounds = Signatures.getBounds(wildcard);

            if (!bounds.isEmpty()) {
                items = List.of(bounds.get(0));
            }
        } else if (signature instanceof TypeVariable<?> typeParameter) {
            items = List
                    .copyOf(TypeParameters.getEffectiveBounds(typeParameter));
        } else if (signature instanceof AnnotatedTypeVariable typeVariable) {
            items = List.of(Signatures.resolve(typeVariable));
        } else {
            items = List.copyOf(typeArguments);
        }

        return items;
    }

    private List<AnnotatedElement> filterCompatible(
            List<AnnotatedElement> types) {
        if (types.isEmpty()) {
            // Nothing to filter, return the original (empty) list.
            return types;
        }

        var primary = signatureToTypeString(types.get(0));
        return types.stream()
                .filter(type -> primary.equals(signatureToTypeString(type)))
                .collect(Collectors.toList());
    }

    private NodeDependencies scanCompositeTypeSignature(
            CompositeTypeSignatureNode node,
            NodeDependencies nodeDependencies) {
        var types = node.getSource();
        // Find referred types for all composite type items
        var referredTypes = types.stream().map(this::getReferredTypes)
                .flatMap(List::stream).collect(Collectors.toList());
        return scanTypes(referredTypes, nodeDependencies);
    }

    private NodeDependencies scanEntity(EntityNode node,
            NodeDependencies nodeDependencies) {
        var superClass = node.getSource().getAnnotatedSuperclass();
        if (superClass != null && Signatures.isNonJDKClass(superClass)) {
            return nodeDependencies.appendChildNodes(
                    Stream.of(TypeSignatureNode.of(superClass)));
        }

        return nodeDependencies;
    }

    private NodeDependencies scanMethodNode(MethodNode methodNode,
            NodeDependencies nodeDependencies) {
        var resultType = methodNode.getSource().getAnnotatedReturnType();
        if (Signatures.isVoid(resultType)) {
            return nodeDependencies;
        }

        var resultTypeNode = TypeSignatureNode.of(resultType);
        return nodeDependencies.appendChildNodes(Stream.of(resultTypeNode));
    }

    private NodeDependencies scanMethodParameter(
            MethodParameterNode methodParameterNode,
            NodeDependencies nodeDependencies) {
        return nodeDependencies.appendChildNodes(Stream.of(TypeSignatureNode
                .of(methodParameterNode.getSource().getAnnotatedType())));
    }

    private NodeDependencies scanProperty(PropertyNode propertyNode,
            NodeDependencies nodeDependencies) {
        var types = List
                .<AnnotatedElement> copyOf(propertyNode.getAssociatedTypes());
        return scanTypes(types, nodeDependencies);
    }

    private NodeDependencies scanTypes(List<AnnotatedElement> types,
            NodeDependencies nodeDependencies) {
        // Discard type signatures that are different from the primary one
        types = filterCompatible(types);
        if (types.size() > 1) {
            return nodeDependencies.appendChildNodes(
                    Stream.of(CompositeTypeSignatureNode.of(types)));
        } else {
            return nodeDependencies.appendChildNodes(
                    types.stream().map(TypeSignatureNode::of));
        }
    }

    private NodeDependencies scanTypeSignature(TypeSignatureNode node,
            NodeDependencies nodeDependencies) {
        var signature = node.getSource();
        var referredTypes = getReferredTypes(signature);

        for (var i = 0; i < referredTypes.size(); i++) {
            var referredType = referredTypes.get(i);
            nodeDependencies = nodeDependencies.appendChildNodes(
                    Stream.of(TypeSignatureNode.of(referredType, i)));
        }

        return nodeDependencies;
    }

    private String signatureToTypeString(AnnotatedElement type) {
        // This converts to a name without annotations.
        if (type instanceof AnnotatedType annotatedType) {
            return annotatedType.getType().getTypeName();
        } else if (type instanceof Type plainType) {
            return plainType.getTypeName();
        } else {
            return type.toString();
        }
    }
}
