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
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.ArraySignatureModel;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.models.ReflectionSignatureModel;
import com.vaadin.hilla.parser.models.SignatureModel;
import com.vaadin.hilla.parser.models.SpecializedModel;
import com.vaadin.hilla.parser.models.TypeArgumentModel;
import com.vaadin.hilla.parser.models.TypeParameterModel;
import com.vaadin.hilla.parser.models.TypeVariableModel;
import com.vaadin.hilla.parser.plugins.backbone.nodes.CompositeTypeSignatureNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;
import com.vaadin.hilla.parser.utils.Generics;

public final class TypeSignaturePlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof TypedNode typedNode) {
            typedNode.setTarget(TypeFacts.of(typedNode.getType(),
                    // Only deal with generics in entities: endpoint methods are
                    // not allowed to emit generic type parameters and arguments
                    isInEntity(nodePath)));
        }
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
     * types a class is given are the ones it takes, and a type argument, a type
     * variable or an optional is written as the type it stands for.
     */
    private void attachToNestingParentSignature(TypeFacts type,
            TypedNode parentNode) {
        var parentType = parentNode.getType();

        if (parentType.isArray() || parentType.isIterable()
                || parentType.isMap()) {
            return;
        }

        if (parentNode.getTarget().collectsTypeArguments()) {
            parentNode.getTarget().getTypeArguments().add(type);
        } else {
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
     * <li>Known bounds for a type parameter</li>
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
    private List<SignatureModel> getReferredTypes(SignatureModel signature) {
        var items = List.<SignatureModel> of();

        if (signature.isArray()) {
            items = List.of(((ArraySignatureModel) signature).getNestedType());
        } else if (signature.isIterable()) {
            var typeArguments = ((ClassRefSignatureModel) signature)
                    .getTypeArguments();

            if (!typeArguments.isEmpty()) {
                items = List.of(typeArguments.get(0));
            } else {
                // Let's deal with classes extending or implementing an iterator
                var cls = (Class<?>) ((ClassRefSignatureModel) signature)
                        .getClassInfo().get();
                items = Generics.getExactIterableType(cls)
                        .map(type -> List
                                .of(SignatureModel.of((AnnotatedElement) type)))
                        .orElse(items);
            }
        } else if (signature.isOptional()) {
            var typeArguments = ((ClassRefSignatureModel) signature)
                    .getTypeArguments();

            if (!typeArguments.isEmpty()) {
                items = List.of(typeArguments.get(0));
            }
        } else if (signature.isMap()) {
            var typeArguments = ((ClassRefSignatureModel) signature)
                    .getTypeArguments();

            if (!typeArguments.isEmpty()) {
                items = List.of(typeArguments.get(1));
            }
        } else if (signature.isTypeArgument()) {
            var associatedTypes = ((TypeArgumentModel) signature)
                    .getAssociatedTypes();

            if (!associatedTypes.isEmpty()) {
                items = List.of(associatedTypes.get(0));
            }
        } else if (signature.isTypeParameter()) {
            var bounds = ((TypeParameterModel) signature).getBounds();

            if (!bounds.isEmpty()) {
                items = bounds.stream().filter(Objects::nonNull)
                        .filter(Predicate.not(SpecializedModel::isNativeObject))
                        .collect(Collectors.toList());
            }
        } else if (signature.isTypeVariable()) {
            items = List.of(((TypeVariableModel) signature).resolve());
        } else if (signature.isClassRef()) {
            items = ((ClassRefSignatureModel) signature).getTypeArguments()
                    .stream().map(SignatureModel.class::cast).toList();
        }

        return items;
    }

    private List<SignatureModel> filterCompatible(List<SignatureModel> types) {
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
        var cls = node.getSource();
        if (cls.getSuperClass().isPresent()
                && cls.getSuperClass().get().isNonJDKClass()) {
            return nodeDependencies.appendChildNodes(
                    Stream.of(TypeSignatureNode.of(cls.getSuperClass().get())));
        }

        return nodeDependencies;
    }

    private NodeDependencies scanMethodNode(MethodNode methodNode,
            NodeDependencies nodeDependencies) {
        if (methodNode.getSource().getResultType().isVoid()) {
            return nodeDependencies;
        }

        var resultTypeNode = TypeSignatureNode
                .of(methodNode.getSource().getResultType());
        return nodeDependencies.appendChildNodes(Stream.of(resultTypeNode));
    }

    private NodeDependencies scanMethodParameter(
            MethodParameterNode methodParameterNode,
            NodeDependencies nodeDependencies) {
        return nodeDependencies.appendChildNodes(Stream.of(TypeSignatureNode
                .of(methodParameterNode.getSource().getType())));
    }

    private NodeDependencies scanProperty(PropertyNode propertyNode,
            NodeDependencies nodeDependencies) {
        var property = propertyNode.getSource();
        var types = property.getAssociatedTypes();
        return scanTypes(types, nodeDependencies);
    }

    private NodeDependencies scanTypes(List<SignatureModel> types,
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

    private String signatureToTypeString(SignatureModel type) {
        // Only reflection types are supported.
        // This converts to a name without annotations.
        var annotatedElement = ((ReflectionSignatureModel) type).get();
        if (annotatedElement instanceof AnnotatedType) {
            return ((AnnotatedType) annotatedElement).getType().getTypeName();
        } else if (annotatedElement instanceof Type) {
            return ((Type) annotatedElement).getTypeName();
        } else {
            return annotatedElement.toString();
        }
    }
}
