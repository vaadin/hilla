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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.models.FieldInfoModel;
import com.vaadin.hilla.parser.models.SignatureModel;
import com.vaadin.hilla.parser.models.SpecializedModel;
import com.vaadin.hilla.parser.models.TypeParameterModel;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;

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
    /**
     * What has been built below each node the walk is in, the innermost first.
     */
    private final Deque<Frame> frames = new ArrayDeque<>();
    private final List<EndpointModel> endpoints = new ArrayList<>();
    private final List<EntityModel> entities = new ArrayList<>();

    /**
     * What has been built below one node, which the node is built from once the
     * walk leaves it.
     */
    private record Frame(Node<?, ?> node, List<TypeModel> types,
            List<ParameterModel> parameters, Map<String, MethodModel> methods,
            List<PropertyModel> properties) {
        Frame(Node<?, ?> node) {
            this(node, new ArrayList<>(), new ArrayList<>(),
                    new LinkedHashMap<>(), new ArrayList<>());
        }
    }

    /**
     * The endpoints built by the last run of the parser.
     */
    public List<EndpointModel> getEndpoints() {
        return List.copyOf(endpoints);
    }

    /**
     * The types the endpoints of the last run refer to, in the order the walk
     * reached them.
     */
    public List<EntityModel> getEntities() {
        return List.copyOf(entities);
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof RootNode) {
            // The plugin can be handed to more than one run of the parser, and
            // each of them describes the whole set of endpoints
            frames.clear();
            endpoints.clear();
            entities.clear();
        }

        frames.push(new Frame(nodePath.getNode()));
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        var own = frames.pop();
        var parent = frames.peek();

        if (own.node() instanceof TypedNode type) {
            parent.types().add(buildType(type, own.types()));
        } else if (own.node() instanceof MethodParameterNode parameter) {
            parent.parameters().add(new ParameterModel(parameter.getTarget(),
                    only(own.types())));
        } else if (own.node() instanceof MethodNode method) {
            collectMethod(method, own);
        } else if (own.node() instanceof PropertyNode property) {
            parent.properties().add(
                    new PropertyModel(property.getTarget(), only(own.types())));
        } else if (own.node() instanceof EntityNode entity) {
            entities.add(buildEntity(entity, List.copyOf(own.properties())));
        } else if (own.node() instanceof EndpointNode endpoint) {
            endpoints.add(new EndpointModel(endpoint.getTarget().getName(),
                    endpoint.getSource().getName(),
                    List.copyOf(own.methods().values())));
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
    private void collectMethod(MethodNode node, Frame own) {
        var returnType = own.types().stream().findFirst().orElseGet(
                () -> TypeModel.Scalar.of(TypeModel.ScalarKind.VOID, "void"));

        var method = new MethodModel(node.getSource().getName(),
                List.copyOf(own.parameters()), returnType);

        frames.stream().filter(frame -> frame.node() instanceof EndpointNode)
                .findFirst().ifPresent(endpoint -> endpoint.methods()
                        .put(method.name(), method));
    }

    /**
     * Builds the declaration of a type the endpoints refer to. An enum is the
     * constants it is serialized as; anything else is the properties it
     * declares, on top of the ones it inherits from the types it extends.
     */
    private static EntityModel buildEntity(EntityNode node,
            List<PropertyModel> ownProperties) {
        var cls = node.getSource();

        if (cls.isEnum()) {
            return new EntityModel.Enumeration(cls.getName(),
                    cls.getFields().stream().filter(FieldInfoModel::isPublic)
                            .map(FieldInfoModel::getName).toList());
        }

        return new EntityModel.Bean(cls.getName(), typeParameters(cls),
                superTypes(cls), ownProperties);
    }

    /**
     * The type parameters of a declaration, which are the ones the generated
     * TypeScript can express: a parameter bound to something else than an
     * object stands for that bound instead.
     */
    private static List<String> typeParameters(ClassInfoModel cls) {
        return cls.getTypeParameters().stream()
                .filter(parameter -> parameter.getBounds().stream()
                        .filter(Objects::nonNull)
                        .allMatch(SpecializedModel::isNativeObject))
                .map(TypeParameterModel::getName).toList();
    }

    /**
     * The types a declaration extends, which are the ones generated as
     * declarations themselves: the properties of a JDK superclass are the
     * properties of the type itself as far as the generator is concerned.
     */
    private static List<TypeModel.EntityRef> superTypes(ClassInfoModel cls) {
        return cls.getSuperClass().filter(ClassRefSignatureModel::isNonJDKClass)
                .map(ref -> TypeModel.EntityRef
                        .of(ref.getClassInfo().getName()))
                .map(List::of).orElseGet(List::of);
    }

    /**
     * Builds the type of one signature out of the types built for the
     * signatures it refers to, such as the items of an array.
     */
    private TypeModel buildType(TypedNode node, List<TypeModel> referred) {
        var signature = node.getType();
        var schema = node.getTarget();
        var optional = schema != null
                && Boolean.TRUE.equals(schema.getNullable());

        // A type variable or parameter of an entity is declared by it, so it
        // is written as its name, unless it is bound to something else than
        // an object: it then stands for that bound, which the walk visits
        // below it, since the declaration does not keep such a parameter
        if ((signature.isTypeVariable() || signature.isTypeParameter())
                && isInEntity()) {
            return referred.isEmpty()
                    ? new TypeModel.TypeVariable(name(signature), optional)
                    : optional ? only(referred).withOptional(true)
                            : only(referred);
        }

        // A type argument, such as the String of a List<String>, stands for
        // the type it is bound to as well. Its schema is the one of that type,
        // which the parser hands up in place of its own, so whether the value
        // can be absent is read after what the plugins said of either
        if (signature.isTypeArgument() || signature.isTypeVariable()
                || signature.isTypeParameter()) {
            return only(referred).withOptional(optional);
        }

        // An Optional is written as the type it holds, which can be absent:
        // TypeScript has nothing of its own for it
        if (signature.isOptional()) {
            return only(referred).withOptional(true);
        }

        if (signature.isArray() || signature.isIterable()) {
            return new TypeModel.ArrayOf(only(referred), optional);
        }

        if (signature.isMap()) {
            return new TypeModel.MapOf(only(referred), optional);
        }

        var kind = scalarKind(signature);

        // A scalar is checked for first, as the parser does, so that a class
        // of the application extending one, such as a Date, is written as
        // what it is serialized as rather than declared on its own
        if (kind == TypeModel.ScalarKind.UNKNOWN && signature.isClassRef()
                && isEntity(signature)) {
            return new TypeModel.EntityRef(name(signature), referred, optional);
        }

        return new TypeModel.Scalar(kind, optional, name(signature));
    }

    /**
     * Whether the walk is inside an entity, where the parser keeps type
     * variables and parameters as they are rather than resolving them.
     */
    private boolean isInEntity() {
        return frames.stream()
                .anyMatch(frame -> frame.node() instanceof EntityNode);
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
                // An enum of the application is declared on its own instead
                || (signature.isEnum() && !isEntity(signature))) {
            return TypeModel.ScalarKind.STRING;
        }

        return TypeModel.ScalarKind.UNKNOWN;
    }

    /**
     * Whether the type comes from outside the JDK, which is what makes it a
     * declaration of its own unless it is a scalar, such as a class extending a
     * date. An enum of the application is declared too: it becomes a TypeScript
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
}
