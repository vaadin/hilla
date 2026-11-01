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

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.plugins.backbone.TypeParameters;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;
import com.vaadin.hilla.parser.plugins.subtypes.SubTypesPlugin;
import com.vaadin.hilla.parser.utils.Signatures;
import com.vaadin.hilla.transfertypes.annotations.FromModule;

/**
 * Builds the model the TypeScript generator works from out of the browser
 * callable classes, while the parser walks them.
 *
 * <p>
 * The model is assembled from the Java signatures the walk carries, so it keeps
 * the type each value comes from. Whether a value can be absent is the one
 * thing taken from what the other plugins say about it, since resolving it from
 * the annotations, the Kotlin metadata and the defaults of the project is their
 * work.
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
 * The plugin only collects: every other plugin sees the walk as it would
 * without it.
 */
public final class EndpointModelPlugin
        extends AbstractPlugin<PluginConfiguration> {
    /**
     * What has been built below each node the walk is in, the innermost first.
     */
    private final Deque<Frame> frames = new ArrayDeque<>();
    /**
     * The endpoints by the name the client calls them by, which is what tells
     * one from another: two classes of the same name, in packages of their own,
     * are one endpoint as far as the browser is concerned, and the last of them
     * is the one it reaches, as it is the one the server answers with.
     */
    private final Map<String, EndpointModel> endpoints = new LinkedHashMap<>();
    private final List<EntityModel> entities = new ArrayList<>();
    private final Map<String, List<String>> unions = new LinkedHashMap<>();

    /**
     * The Java types the generated TypeScript refers to by name rather than by
     * declaring them, which the plugin handling the transfer types has already
     * mapped the ones of the application to. The browser knows a file, and the
     * signals are exported by a module of the framework, which the class says
     * itself.
     */
    private static final Map<String, TypeModel.Provided> PROVIDED_TYPES = Stream
            .of(com.vaadin.hilla.runtime.transfertypes.File.class,
                    com.vaadin.hilla.runtime.transfertypes.Signal.class,
                    com.vaadin.hilla.runtime.transfertypes.NumberSignal.class,
                    com.vaadin.hilla.runtime.transfertypes.ValueSignal.class,
                    com.vaadin.hilla.runtime.transfertypes.ListSignal.class)
            .collect(java.util.stream.Collectors.toMap(Class::getName,
                    EndpointModelPlugin::providedType));

    /**
     * The signals a value is shared through, by the name the client knows them
     * by, since each of them is built in a way of its own.
     */
    private static final Map<String, MethodModel.Kind> SIGNAL_KINDS = Map.of(
            "NumberSignal", MethodModel.Kind.NUMBER_SIGNAL, "ValueSignal",
            MethodModel.Kind.VALUE_SIGNAL, "ListSignal",
            MethodModel.Kind.LIST_SIGNAL);

    /**
     * The Java types an endpoint sends a series of values through, which the
     * plugin handling the transfer types has already mapped the ones of the
     * application to.
     */
    private static final Set<String> PUSHED_TYPES = Set.of(
            com.vaadin.hilla.runtime.transfertypes.Flux.class.getName(),
            com.vaadin.hilla.runtime.transfertypes.EndpointSubscription.class
                    .getName());

    /**
     * Everything the last run of the parser found, which is what one generation
     * of TypeScript is written from.
     */
    public Generation getGeneration() {
        return new Generation(getEndpoints(), getEntities(), getUnions());
    }

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
        return List.copyOf(endpoints.values());
    }

    /**
     * The types the endpoints of the last run refer to, in the order the walk
     * reached them.
     */
    public List<EntityModel> getEntities() {
        return List.copyOf(entities);
    }

    /**
     * The polymorphic types of the last run, each with the subtypes a value of
     * it can be.
     *
     * <p>
     * A subtype accepting the discriminator of the subtypes below it is
     * narrowed to its own value here, since a value of it would otherwise be a
     * value of any of them as far as TypeScript is concerned. That is why the
     * unions are built once the walk is over rather than while it runs: it is
     * the subtypes which say what they accept.
     */
    public List<UnionModel> getUnions() {
        return unions.entrySet().stream()
                // A type can say that its values are of a subtype without
                // saying which subtypes there are, and then there is no union
                .filter(union -> !union.getValue().isEmpty())
                .map(union -> new UnionModel(union.getKey(),
                        union.getValue().stream().map(this::member).toList()))
                .toList();
    }

    private UnionModel.Member member(String javaClass) {
        var accepted = entities.stream()
                .filter(EntityModel.Bean.class::isInstance)
                .map(EntityModel.Bean.class::cast)
                .filter(bean -> bean.javaClass().equals(javaClass))
                .flatMap(bean -> bean.discriminator().stream()).findFirst();

        // The value of the subtype itself comes first, followed by the ones of
        // the subtypes below it
        return accepted.filter(
                discriminator -> discriminator.acceptedValues().size() > 1)
                .map(discriminator -> new EntityModel.Discriminator(
                        discriminator.name(),
                        List.of(discriminator.acceptedValues().get(0))))
                .map(narrowedTo -> new UnionModel.Member(
                        TypeModel.EntityRef.of(javaClass),
                        Optional.of(narrowedTo)))
                .orElseGet(() -> UnionModel.Member
                        .of(TypeModel.EntityRef.of(javaClass)));
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof RootNode) {
            // The plugin can be handed to more than one run of the parser, and
            // each of them describes the whole set of endpoints
            frames.clear();
            endpoints.clear();
            entities.clear();
            unions.clear();
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
            parent.properties().add(buildProperty(property, own.types()));
        } else if (own.node() instanceof EntityNode entity
                && !isProvided(entity.getSource().getName())) {
            var declaration = buildEntity(entity,
                    List.copyOf(own.properties()));

            entities.add(declaration);
            entity.getTarget().setDeclaration(declaration);
        } else if (own.node() instanceof SubTypesPlugin.UnionNode union) {
            unions.put(union.getSource().getName(), union.getTarget());
        } else if (own.node() instanceof EndpointNode endpoint) {
            var name = endpoint.getTarget();

            endpoints.put(name,
                    new EndpointModel(name, endpoint.getSource().getName(),
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

        var pushed = pushedValue(returnType);

        var method = new MethodModel(node.getSource().getName(),
                List.copyOf(own.parameters()), pushed.orElse(returnType),
                pushed.map(values -> MethodModel.Kind.SUBSCRIBED)
                        .orElseGet(() -> kindOf(returnType)));

        frames.stream().filter(frame -> frame.node() instanceof EndpointNode)
                .findFirst().ifPresent(endpoint -> endpoint.methods()
                        .put(method.name(), method));
    }

    /**
     * The type of the values a method pushes, if it pushes them rather than
     * returning one: the types the endpoint sends values through are carried as
     * the collection of what they hold, and the name of the Java type is what
     * tells one of them from an ordinary collection.
     */
    private static Optional<TypeModel> pushedValue(TypeModel returnType) {
        return returnType instanceof TypeModel.ArrayOf array
                && PUSHED_TYPES.contains(array.javaType())
                        ? Optional.of(array.items())
                        : Optional.empty();
    }

    /**
     * How the client reaches a method, which is what the type it returns says:
     * a signal is shared with the server rather than returned.
     */
    private static MethodModel.Kind kindOf(TypeModel returnType) {
        return returnType instanceof TypeModel.Provided provided
                ? SIGNAL_KINDS.getOrDefault(provided.name(),
                        MethodModel.Kind.CALLED)
                : MethodModel.Kind.CALLED;
    }

    private static PropertyModel buildProperty(PropertyNode node,
            List<TypeModel> ownTypes) {
        var type = only(ownTypes);

        // Whether the value of a property can be absent is not settled until
        // the property itself is left: the nullability a Kotlin declaration
        // keeps on the member it is written as is decided there. A property
        // holding an optional is one which can be absent whatever such a
        // declaration says, since an empty optional is what the server leaves
        // out. A value of a type variable is left as absent as the type it
        // stands for says, since a declaration extending this one gives that
        // type and is free to say that the value is always there
        var absent = isOptional(node) || node.getValueType().isOptional();

        return new PropertyModel(node.getTarget(),
                type instanceof TypeModel.TypeVariable && !absent ? type
                        : type.withOptional(absent));
    }

    /**
     * Whether the value a property holds is an optional, which the type of the
     * member it is written as says.
     */
    private static boolean isOptional(PropertyNode property) {
        return property.getAssociatedTypes().stream().findFirst()
                .filter(Signatures::isOptional).isPresent();
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
                    Arrays.stream(cls.getDeclaredFields()).filter(
                            field -> Modifier.isPublic(field.getModifiers()))
                            .map(Field::getName).toList());
        }

        return node.getTarget().bean(cls.getName(), typeParameters(cls),
                superTypes(cls), ownProperties);
    }

    /**
     * The type parameters of a declaration, which are the ones the generated
     * TypeScript can express: a parameter bound to something else than an
     * object stands for that bound instead, unless the bound only leads back to
     * the parameter itself, as in {@code <E extends Enum<E>>}.
     */
    private static List<String> typeParameters(Class<?> cls) {
        return Arrays.stream(cls.getTypeParameters())
                .filter(TypeParameters::isUnbounded).map(TypeVariable::getName)
                .toList();
    }

    /**
     * The types a declaration extends, which are the ones generated as
     * declarations themselves: the properties of a JDK superclass are the
     * properties of the type itself as far as the generator is concerned.
     */
    private static List<TypeModel.EntityRef> superTypes(Class<?> cls) {
        return Optional.ofNullable(cls.getAnnotatedSuperclass())
                .filter(Signatures::isNonJDKClass)
                .map(ref -> TypeModel.EntityRef
                        .of(Signatures.getClass(ref).orElseThrow().getName()))
                .map(List::of).orElseGet(List::of);
    }

    /**
     * Builds the type of one signature out of the types built for the
     * signatures it refers to, such as the items of an array.
     */
    private TypeModel buildType(TypedNode node, List<TypeModel> referred) {
        var signature = node.getType();
        var type = Optional.ofNullable(node.getTarget())
                .orElseGet(TypeModel.Builder::unknown);
        var optional = type.isOptional();

        // A type variable or parameter of an entity is declared by it, so it
        // is written as its name, unless it is bound to something else than
        // an object and not only to itself, as in <E extends Enum<E>>: it then
        // stands for that bound, which the walk visits below it, since the
        // declaration does not keep such a parameter
        if ((Signatures.isTypeVariable(signature)
                || Signatures.isTypeParameter(signature)) && isInEntity()) {
            return referred.isEmpty() ? type.typeVariable(name(signature))
                    : optional ? only(referred).withOptional(true)
                            : only(referred);
        }

        // A type argument, such as the String of a List<String>, stands for
        // the type it is bound to as well. What the walk says of it is what it
        // says of that type, which the parser hands up in place of its own, so
        // whether the value can be absent is read after the plugins said it
        if (Signatures.isTypeArgument(signature)
                || Signatures.isTypeVariable(signature)
                || Signatures.isTypeParameter(signature)) {
            return only(referred).withOptional(optional);
        }

        // An Optional is written as the type it holds, which can be absent:
        // TypeScript has nothing of its own for it
        if (Signatures.isOptional(signature)) {
            return only(referred).withOptional(true);
        }

        if (Signatures.isArray(signature) || Signatures.isIterable(signature)) {
            return type.arrayOf(only(referred), name(signature));
        }

        if (Signatures.isMap(signature)) {
            return type.mapOf(only(referred), name(signature));
        }

        if (Signatures.isClassRef(signature) && isProvided(name(signature))) {
            var provided = PROVIDED_TYPES.get(name(signature));

            return type.provided(provided.name(), provided.module(), referred);
        }

        if (Signatures.isClassRef(signature) && isEntity(signature)
                && !isValue(signature)) {
            return type.entityRef(name(signature), referred);
        }

        return type.scalar(scalarKind(signature), name(signature));
    }

    /**
     * Whether the walk is inside an entity, where the parser keeps type
     * variables and parameters as they are rather than resolving them.
     */
    private boolean isInEntity() {
        return frames.stream()
                .anyMatch(frame -> frame.node() instanceof EntityNode);
    }

    private static TypeModel.ScalarKind scalarKind(AnnotatedElement signature) {
        if (Signatures.isBoolean(signature)) {
            return TypeModel.ScalarKind.BOOLEAN;
        }

        if (Signatures.isInteger(signature) || Signatures.isFloat(signature)
                || Signatures.isLong(signature) || Signatures.isShort(signature)
                || Signatures.isByte(signature)
                || Signatures.isDouble(signature)
                || Signatures.isBigDecimal(signature)
                || Signatures.isBigInteger(signature)) {
            return TypeModel.ScalarKind.NUMBER;
        }

        if (Signatures.isString(signature) || Signatures.isCharacter(signature)
                || Signatures.isDate(signature)
                || Signatures.isDateTime(signature)
                // An enum of the application is declared on its own instead
                || (Signatures.isEnum(signature) && !isEntity(signature))) {
            return TypeModel.ScalarKind.STRING;
        }

        return TypeModel.ScalarKind.UNKNOWN;
    }

    private static boolean isProvided(String javaClass) {
        return PROVIDED_TYPES.containsKey(javaClass);
    }

    /**
     * The name a type is referred to by and the module exporting it under that
     * name, both of which the class says itself: the ones the browser has say
     * nothing, and go by the name of the class.
     */
    private static TypeModel.Provided providedType(Class<?> javaClass) {
        var fromModule = javaClass.getAnnotation(FromModule.class);

        if (fromModule == null) {
            return new TypeModel.Provided(javaClass.getSimpleName(), "",
                    List.of(), false);
        }

        if (fromModule.namedSpecifier().isBlank()) {
            throw new IllegalStateException(javaClass.getName()
                    + " says which module it comes from without saying the"
                    + " name it is exported under");
        }

        return new TypeModel.Provided(fromModule.namedSpecifier(),
                fromModule.module(), List.of(), false);
    }

    /**
     * Whether the type comes from outside the JDK, which is what makes it a
     * declaration of its own unless it is a scalar, such as a class extending a
     * date. An enum of the application is declared too: it becomes a TypeScript
     * enum of its own, not the string it is serialized as.
     */
    private static boolean isEntity(AnnotatedElement signature) {
        return Signatures.isNonJDKClass(signature);
    }

    /**
     * Whether the browser has a value of the type, which a class of the
     * application can be one of as well: a class extending Date is a date, and
     * an endpoint sends it as one rather than as a type of its own. The order
     * is the one the walk decides whether a value can be absent in, so that
     * both say the same about a type.
     */
    private static boolean isValue(AnnotatedElement signature) {
        return Signatures.isString(signature)
                || Signatures.isCharacter(signature)
                || Signatures.isBoolean(signature)
                || Signatures.hasIntegerType(signature)
                || Signatures.isBigInteger(signature)
                || Signatures.hasFloatType(signature)
                || Signatures.isBigDecimal(signature)
                || Signatures.isDate(signature)
                || Signatures.isDateTime(signature);
    }

    private static String name(AnnotatedElement signature) {
        var cls = Signatures.getClass(signature);
        if (cls.isPresent()) {
            return cls.get().getName();
        }

        // An array says its own name, which is all of it: what it is annotated
        // with belongs to the value rather than to the type, and reads as
        // part of the name otherwise
        if (signature instanceof AnnotatedArrayType array) {
            return name(array.getAnnotatedGenericComponentType()) + "[]";
        }

        if (signature instanceof TypeVariable<?> typeParameter) {
            return typeParameter.getName();
        }

        return String.valueOf(signature);
    }

    private static TypeModel only(List<TypeModel> types) {
        return types.stream().findFirst().orElseGet(() -> TypeModel.Scalar
                .of(TypeModel.ScalarKind.UNKNOWN, Object.class.getName()));
    }
}
