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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointExposedNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointNonExposedNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointSignatureNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;
import com.vaadin.hilla.parser.utils.Signatures;

public final class EndpointExposedPlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    @NonNull
    @Override
    public Node<?, ?> resolve(@NonNull Node<?, ?> node,
            @NonNull NodePath<?> parentPath) {
        if (node instanceof MethodNode
                && parentPath.getNode() instanceof EndpointExposedNode) {
            return MethodNode.of(((MethodNode) node).getSource());
        }
        if (!(node instanceof TypeSignatureNode)) {
            return node;
        }

        if (!(node.getSource() instanceof TypeVariable<?> typeParameter)) {
            return node;
        }

        return TypeSignatureNode
                .of(resolveTypeParameter(typeParameter, parentPath));
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();

        if (node instanceof EndpointNode || node instanceof EndpointExposedNode
                || node instanceof EndpointNonExposedNode) {
            // Attach type signature nodes for hierarchy parent superclass and
            // implemented interfaces, if any
            var cls = (Class<?>) node.getSource();
            return nodeDependencies
                    .appendChildNodes(scanEndpointClassSignature(cls));
        }

        if (node instanceof EndpointSignatureNode) {
            // Attach the referenced class from the endpoint hierarchy type
            // signature
            var classRef = ((EndpointSignatureNode) node).getSource();
            var cls = Signatures.getClass(classRef).orElseThrow();
            return nodeDependencies.appendChildNodes(
                    Stream.of(createEndpointHierarchyClassNode(cls)));
        }

        return nodeDependencies;
    }

    /**
     * Creates a node that wraps the given endpoint hierarchy class as a source.
     * If the class is annotated with the configured {@code @EndpointExposed}
     * annotation, uses {@code EndpointExposedNode}, otherwise uses
     * {@code EndpointNonExposedNode}.
     *
     * @param classInfo
     *            The class from the endpoint hierarchy.
     * @return The node for the class.
     */
    private Node<?, ?> createEndpointHierarchyClassNode(Class<?> classInfo) {
        // annotations are compared by name as Gradle can proxy them
        var endpointExposedAnnotations = getParserConfig()
                .getEndpointExposedAnnotations().stream().map(Class::getName)
                .toList();
        var exposed = Arrays.stream(classInfo.getAnnotations())
                .map(Annotation::annotationType).map(Class::getName)
                .anyMatch(endpointExposedAnnotations::contains);
        var classInfoNode = exposed ? EndpointExposedNode.of(classInfo)
                : EndpointNonExposedNode.of(classInfo);
        return classInfoNode;
    }

    /**
     * Replaces generic type parameters used in {@code @EndpointExposed} with
     * their arguments defined in type signatures of endpoint class hierarchy
     * descendants.
     *
     * @param typeParameter
     * @param path
     * @return
     */
    private AnnotatedElement resolveTypeParameter(TypeVariable<?> typeParameter,
            NodePath<?> path) {
        var closestEndpointSignaturePath = path.stream()
                .filter(p -> p.getNode() instanceof EndpointSignatureNode)
                .findFirst();
        if (closestEndpointSignaturePath.isEmpty()) {
            return typeParameter;
        }

        var endpointSignaturePath = closestEndpointSignaturePath.get();
        var classRef = ((EndpointSignatureNode) endpointSignaturePath.getNode())
                .getSource();

        var paramIndex = List.of(
                Signatures.getClass(classRef).orElseThrow().getTypeParameters())
                .indexOf(typeParameter);
        if (paramIndex < 0) {
            // A type parameter declared elsewhere, such as by a method
            return typeParameter;
        }

        var typeArg = Signatures.getTypeArguments(classRef).get(paramIndex);
        if (typeArg instanceof AnnotatedWildcardType) {
            // TODO: add resolving for wildcard type arguments
            return typeParameter;
        }

        // Recursively resolve type variables
        if (typeArg instanceof AnnotatedTypeVariable typeVariable) {
            return resolveTypeParameter(Signatures.resolve(typeVariable),
                    endpointSignaturePath.getParentPath());
        }

        return typeArg;
    }

    /**
     * Creates and returns nodes for the type signatures of the superclass and
     * implemented interfaces of the endpoint or endpoint hierarchy ancestor
     * class.
     *
     * <p>
     * The signatures are used later for type arguments lookup to resolve
     * generic type variables in exposed method parameter and result types, see
     * the {@code resolve(Node, NodePath)} lifecycle hook.
     *
     * @param endpointClass
     *            The endpoint or endpoint hierarchy class.
     * @return The stream of nodes.
     */
    private Stream<Node<?, ?>> scanEndpointClassSignature(
            Class<?> endpointClass) {
        var superClass = Optional
                .ofNullable(endpointClass.getAnnotatedSuperclass())
                .filter(Signatures::isNonJDKClass);
        return Stream
                .concat(superClass.stream(),
                        Arrays.stream(endpointClass.getAnnotatedInterfaces()))
                .map(EndpointSignatureNode::of);
    }
}
