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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.models.SignatureModel;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;

/**
 * Builds the model the TypeScript generator works from out of the browser
 * callable classes, while the parser walks them.
 *
 * <p>
 * The model is assembled from the Java signatures the walk carries, so it keeps
 * the type each value comes from, which the OpenAPI representation of the same
 * walk cannot: a date, an instant and a plain string are all a string there.
 * Whether a value can be absent is the one thing taken from that
 * representation, since resolving it from the annotations, the Kotlin metadata
 * and the defaults of the project is the work of the other plugins.
 *
 * <p>
 * That is why the plugin belongs <em>first</em> in the chain, although it reads
 * what the others write: a composite plugin enters its plugins in order and
 * exits them in the opposite one, so the first plugin is the last to see a node
 * it is done with. Anywhere else, the nullability of a value would be read
 * before the plugins which decide it have run, and an annotated value would be
 * written as one which can be absent.
 *
 * <p>
 * The plugin only collects: it changes nothing the other plugins produce, so
 * the OpenAPI definition is exactly what it would be without it.
 */
public final class EndpointModelPlugin
        extends AbstractPlugin<PluginConfiguration> {
    private final Map<Node<?, ?>, List<TypeModel>> types = new IdentityHashMap<>();
    private final Map<Node<?, ?>, List<ParameterModel>> parameters = new IdentityHashMap<>();
    private final Map<Node<?, ?>, Map<String, MethodModel>> methods = new IdentityHashMap<>();
    private final List<EndpointModel> endpoints = new ArrayList<>();

    /**
     * The endpoints built by the last run of the parser.
     */
    public List<EndpointModel> getEndpoints() {
        return List.copyOf(endpoints);
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof RootNode) {
            // The plugin can be handed to more than one run of the parser, and
            // each of them describes the whole set of endpoints
            types.clear();
            parameters.clear();
            methods.clear();
            endpoints.clear();
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        var node = nodePath.getNode();

        if (node instanceof TypeSignatureNode type) {
            collect(parentOf(nodePath), buildType(type, taken(node)));
        } else if (node instanceof MethodParameterNode parameter) {
            collect(parentOf(nodePath), parameter, taken(node));
        } else if (node instanceof MethodNode method) {
            collectMethod(nodePath, method);
        } else if (node instanceof EndpointNode endpoint) {
            endpoints.add(new EndpointModel(endpoint.getTarget().getName(),
                    endpoint.getSource().getName(), List.copyOf(
                            methods.getOrDefault(node, Map.of()).values())));
        }
    }

    /**
     * A method of an endpoint, or of a class the endpoint exposes, in which
     * case it belongs to the endpoint below which the walk found it.
     *
     * <p>
     * A name is only there once, although the walk can carry it more than once:
     * a method overriding one of an exposed superclass is visited for each
     * declaration of it, and Java allows two methods to share a name while
     * TypeScript does not. The last one wins, which is what the endpoint is
     * called with, since a call names the method rather than its signature.
     */
    private void collectMethod(NodePath<?> nodePath, MethodNode node) {
        var returnType = taken(node).stream().findFirst().orElseGet(
                () -> TypeModel.Scalar.of(TypeModel.ScalarKind.VOID, "void"));

        var method = new MethodModel(node.getSource().getName(),
                parameters.getOrDefault(node, List.of()), returnType);

        findEndpoint(nodePath).ifPresent(endpoint -> methods
                .computeIfAbsent(endpoint, key -> new LinkedHashMap<>())
                .put(method.name(), method));
    }

    private void collect(Node<?, ?> parent, TypeModel type) {
        types.computeIfAbsent(parent, key -> new ArrayList<>()).add(type);
    }

    private void collect(Node<?, ?> parent, MethodParameterNode node,
            List<TypeModel> ownTypes) {
        var type = only(ownTypes);

        parameters.computeIfAbsent(parent, key -> new ArrayList<>())
                .add(new ParameterModel(node.getTarget(), type));
    }

    /**
     * Builds the type of one signature out of the types built for the
     * signatures it refers to, such as the items of an array.
     */
    private TypeModel buildType(TypeSignatureNode node,
            List<TypeModel> referred) {
        var signature = node.getType();
        var schema = node.getTarget();
        var optional = schema != null
                && Boolean.TRUE.equals(schema.getNullable());

        if (signature.isTypeVariable() || signature.isTypeParameter()) {
            return new TypeModel.TypeVariable(name(signature));
        }

        // A type argument, such as the String of a List<String>, stands for the
        // type it is bound to, which the walk visits below it
        if (signature.isTypeArgument()) {
            return optional ? asOptional(only(referred)) : only(referred);
        }

        // An Optional is written as the type it holds, which can be absent:
        // TypeScript has nothing of its own for it
        if (signature.isOptional()) {
            return asOptional(only(referred));
        }

        if (signature.isArray() || signature.isIterable()) {
            return new TypeModel.ArrayOf(only(referred), optional);
        }

        if (signature.isMap()) {
            return new TypeModel.MapOf(only(referred), optional);
        }

        if (signature.isClassRef() && isEntity(signature)) {
            return new TypeModel.EntityRef(name(signature), referred, optional);
        }

        return new TypeModel.Scalar(scalarKind(signature), optional,
                name(signature));
    }

    private static TypeModel asOptional(TypeModel type) {
        return switch (type) {
        case TypeModel.Scalar scalar ->
            new TypeModel.Scalar(scalar.kind(), true, scalar.javaType());
        case TypeModel.ArrayOf array ->
            new TypeModel.ArrayOf(array.items(), true);
        case TypeModel.MapOf map -> new TypeModel.MapOf(map.values(), true);
        case TypeModel.EntityRef entity -> new TypeModel.EntityRef(
                entity.javaClass(), entity.typeArguments(), true);
        case TypeModel.TypeVariable variable -> variable;
        };
    }

    private static TypeModel.ScalarKind scalarKind(SignatureModel signature) {
        if (signature.isBoolean()) {
            return TypeModel.ScalarKind.BOOLEAN;
        }

        if (signature.isInteger() || signature.isFloat() || signature.isLong()
                || signature.isShort() || signature.isByte()
                || signature.isDouble() || signature.isBigDecimal()
                || signature.isBigInteger()) {
            return TypeModel.ScalarKind.NUMBER;
        }

        if (signature.isString() || signature.isCharacter()
                || signature.isDate() || signature.isDateTime()
                || signature.isEnum()) {
            return TypeModel.ScalarKind.STRING;
        }

        return TypeModel.ScalarKind.UNKNOWN;
    }

    /**
     * Whether the type is generated as a declaration of its own, which every
     * type outside the JDK is, an enum included: an enum becomes a TypeScript
     * enum of its own, not the string it is serialized as.
     */
    private static boolean isEntity(SignatureModel signature) {
        return signature.isNonJDKClass();
    }

    private static String name(SignatureModel signature) {
        if (signature instanceof ClassRefSignatureModel classRef) {
            return classRef.getClassInfo().getName();
        }

        return signature.get() == null ? Object.class.getName()
                : String.valueOf(signature.get());
    }

    private static TypeModel only(List<TypeModel> types) {
        return types.stream().findFirst().orElseGet(() -> TypeModel.Scalar
                .of(TypeModel.ScalarKind.UNKNOWN, Object.class.getName()));
    }

    private List<TypeModel> taken(Node<?, ?> node) {
        var own = types.remove(node);
        return own == null ? List.of() : own;
    }

    private static Node<?, ?> parentOf(NodePath<?> nodePath) {
        return nodePath.getParentPath().getNode();
    }

    private static Optional<Node<?, ?>> findEndpoint(NodePath<?> nodePath) {
        return nodePath.getParentPath().stream()
                .<Node<?, ?>> map(NodePath::getNode)
                .filter(node -> node instanceof EndpointNode).findFirst();
    }

}
