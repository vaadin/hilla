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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import io.swagger.v3.oas.models.media.Schema;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.BaseSignatureModel;
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
import com.vaadin.hilla.parser.plugins.model.Annotation;
import com.vaadin.hilla.parser.plugins.model.ValidationConstraint;
import com.vaadin.hilla.parser.plugins.subtypes.SubTypesPlugin;
import com.vaadin.hilla.transfertypes.annotations.FromModule;

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
     * Where the plugin building the OpenAPI definition leaves what the
     * annotations of the validation API say about a value.
     */
    private static final String VALIDATION_CONSTRAINTS = "x-validation-constraints";

    /**
     * Where the plugin building the OpenAPI definition leaves the annotations
     * of a value which a form model is told about.
     */
    private static final String ANNOTATIONS = "x-annotations";

    private final Map<Node<?, ?>, List<TypeModel>> types = new IdentityHashMap<>();
    private final Map<Node<?, ?>, List<ParameterModel>> parameters = new IdentityHashMap<>();
    private final Map<Node<?, ?>, Map<String, MethodModel>> methods = new IdentityHashMap<>();
    private final Map<Node<?, ?>, List<PropertyModel>> properties = new IdentityHashMap<>();
    private final List<EndpointModel> endpoints = new ArrayList<>();
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
            types.clear();
            parameters.clear();
            methods.clear();
            properties.clear();
            endpoints.clear();
            entities.clear();
            unions.clear();
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        var node = nodePath.getNode();

        if (node instanceof TypedNode type) {
            collect(parentOf(nodePath), buildType(type, taken(node)));
        } else if (node instanceof MethodParameterNode parameter) {
            collect(parentOf(nodePath), parameter, taken(node));
        } else if (node instanceof MethodNode method) {
            collectMethod(nodePath, method);
        } else if (node instanceof PropertyNode property) {
            collect(parentOf(nodePath), property, taken(node));
        } else if (node instanceof EntityNode entity
                && !isProvided(entity.getSource().getName())) {
            entities.add(buildEntity(entity,
                    properties.getOrDefault(node, List.of())));
        } else if (node instanceof SubTypesPlugin.UnionNode union) {
            unions.put(union.getSource().getName(), subTypesOf(union));
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

        var pushed = pushedValue(returnType);

        var method = new MethodModel(node.getSource().getName(),
                parameters.getOrDefault(node, List.of()),
                pushed.orElse(returnType),
                pushed.map(values -> MethodModel.Kind.SUBSCRIBED)
                        .orElseGet(() -> kindOf(returnType)));

        findEndpoint(nodePath).ifPresent(endpoint -> methods
                .computeIfAbsent(endpoint, key -> new LinkedHashMap<>())
                .put(method.name(), method));
    }

    /**
     * The type of the values a method pushes, if it pushes them rather than
     * returning one: the types the endpoint sends values through are carried as
     * the collection of what they hold, since that is what the walk which
     * builds the OpenAPI needs, and the name of the Java type is what tells one
     * of them from an ordinary collection.
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

    private void collect(Node<?, ?> parent, TypeModel type) {
        types.computeIfAbsent(parent, key -> new ArrayList<>()).add(type);
    }

    private void collect(Node<?, ?> parent, MethodParameterNode node,
            List<TypeModel> ownTypes) {
        var type = only(ownTypes);

        parameters.computeIfAbsent(parent, key -> new ArrayList<>())
                .add(new ParameterModel(node.getTarget(), type));
    }

    private void collect(Node<?, ?> parent, PropertyNode node,
            List<TypeModel> ownTypes) {
        properties.computeIfAbsent(parent, key -> new ArrayList<>())
                .add(new PropertyModel(node.getTarget(), only(ownTypes)));
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

        var discriminator = discriminator(node.getTarget());

        // The discriminator is written as the ids it accepts rather than as
        // the type the class declares it with, so it is not among the
        // properties as well: TypeScript would have the name twice
        var properties = ownProperties.stream()
                .filter(property -> discriminator
                        .map(EntityModel.Discriminator::name)
                        .filter(property.name()::equals).isEmpty())
                .toList();

        return new EntityModel.Bean(cls.getName(), typeParameters(cls),
                superTypes(cls), properties, discriminator);
    }

    /**
     * The types a value of a polymorphic type can be, which the walk carries as
     * a node of its own next to the type itself.
     */
    private static List<String> subTypesOf(SubTypesPlugin.UnionNode node) {
        var members = node.getTarget().getOneOf();

        return members == null ? List.of()
                : members.stream().map(Schema::get$ref).filter(Objects::nonNull)
                        .map(EndpointModelPlugin::classOfRef).toList();
    }

    /**
     * The property saying which subtype a value is, which the plugin handling
     * the subtypes adds to the schema of every type of the hierarchy: it is the
     * only property whose schema holds the values it accepts.
     */
    private static Optional<EntityModel.Discriminator> discriminator(
            Schema<?> schema) {
        return schemasOf(schema)
                .flatMap(member -> member.getProperties() == null
                        ? Stream.<Map.Entry<String, Schema>> of()
                        : member.getProperties().entrySet().stream())
                .filter(property -> property.getValue().getEnum() != null
                        && !property.getValue().getEnum().isEmpty())
                .findFirst()
                .map(property -> new EntityModel.Discriminator(
                        property.getKey(), property.getValue().getEnum()
                                .stream().map(String::valueOf).toList()));
    }

    /**
     * The schema itself and the ones it is composed of, since the properties a
     * subtype declares itself are in a member of its own.
     */
    private static Stream<Schema<?>> schemasOf(Schema<?> schema) {
        if (schema == null) {
            return Stream.of();
        }

        var members = schema.getAnyOf() == null ? Stream.<Schema<?>> of()
                : schema.getAnyOf().stream().map(member -> (Schema<?>) member);

        return Stream.concat(Stream.of(schema), members);
    }

    private static String classOfRef(String ref) {
        return ref.substring(ref.lastIndexOf('/') + 1);
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
        var constraints = constraintsOf(schema);
        var annotations = annotationsOf(schema);

        // A type parameter bound to something else than an object stands for
        // that bound, which the walk visits below it, since the declaration
        // does not keep such a parameter
        if (signature.isTypeVariable() || signature.isTypeParameter()) {
            return referred.isEmpty()
                    ? new TypeModel.TypeVariable(name(signature), optional)
                    : bound(only(referred), optional);
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
            return new TypeModel.ArrayOf(only(referred), optional,
                    name(signature), constraints, annotations);
        }

        if (signature.isMap()) {
            return new TypeModel.MapOf(only(referred), optional,
                    name(signature), constraints, annotations);
        }

        if (signature.isClassRef() && isProvided(name(signature))) {
            return provided(name(signature), referred, optional);
        }

        if (signature.isClassRef() && isEntity(signature)
                && !isValue(signature)) {
            return new TypeModel.EntityRef(name(signature), referred, optional,
                    constraints, annotations);
        }

        return new TypeModel.Scalar(scalarKind(signature), optional,
                name(signature), constraints, annotations);
    }

    private static TypeModel bound(TypeModel type, boolean optional) {
        return optional ? asOptional(type) : type;
    }

    private static TypeModel asOptional(TypeModel type) {
        return switch (type) {
        case TypeModel.Scalar scalar ->
            new TypeModel.Scalar(scalar.kind(), true, scalar.javaType(),
                    scalar.constraints(), scalar.annotations());
        case TypeModel.ArrayOf array ->
            new TypeModel.ArrayOf(array.items(), true, array.javaType(),
                    array.constraints(), array.annotations());
        case TypeModel.MapOf map -> new TypeModel.MapOf(map.values(), true,
                map.javaType(), map.constraints(), map.annotations());
        case TypeModel.EntityRef entity ->
            new TypeModel.EntityRef(entity.javaClass(), entity.typeArguments(),
                    true, entity.constraints(), entity.annotations());
        case TypeModel.Provided provided -> new TypeModel.Provided(
                provided.name(), provided.module(), provided.typeArguments(),
                true, provided.constraints(), provided.annotations());
        case TypeModel.TypeVariable variable ->
            new TypeModel.TypeVariable(variable.name(), true,
                    variable.constraints(), variable.annotations());
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

        return fromModule == null
                ? new TypeModel.Provided(javaClass.getSimpleName(), "",
                        List.of(), false)
                : new TypeModel.Provided(fromModule.namedSpecifier(),
                        fromModule.module(), List.of(), false);
    }

    /**
     * The type as it is referred to where it is used, which is what the class
     * says of it together with the arguments it is used with.
     */
    private static TypeModel provided(String javaClass,
            List<TypeModel> typeArguments, boolean optional) {
        var provided = PROVIDED_TYPES.get(javaClass);

        return new TypeModel.Provided(provided.name(), provided.module(),
                typeArguments, optional);
    }

    /**
     * Whether the type is generated as a declaration of its own, which every
     * type outside the JDK is, an enum included: an enum becomes a TypeScript
     * enum of its own, not the string it is serialized as.
     */
    private static boolean isEntity(SignatureModel signature) {
        return signature.isNonJDKClass();
    }

    /**
     * Whether the browser has a value of the type, which a class of the
     * application can be one of as well: a class extending Date is a date, and
     * an endpoint sends it as one rather than as a type of its own. The order
     * is the one the OpenAPI definition of the same walk is built in, so that
     * both say the same about a type.
     */
    /**
     * What the annotations of the validation API say about a value, which the
     * plugin building the OpenAPI definition has already read off the walk and
     * left on the schema of the value.
     */
    private static List<ConstraintModel> constraintsOf(Schema<?> schema) {
        if (schema == null || schema.getExtensions() == null) {
            return List.of();
        }

        if (!(schema.getExtensions()
                .get(VALIDATION_CONSTRAINTS) instanceof List<?> constraints)) {
            return List.of();
        }

        return constraints.stream()
                .filter(ValidationConstraint.class::isInstance)
                .map(ValidationConstraint.class::cast)
                .map(constraint -> new ConstraintModel(
                        constraint.getSimpleName(),
                        constraint.getAttributes() == null ? Map.of()
                                : constraint.getAttributes()))
                .toList();
    }

    /**
     * The annotations of a value which a form model is told about, which the
     * plugin building the OpenAPI definition has already picked out of the ones
     * the property declares.
     */
    private static List<String> annotationsOf(Schema<?> schema) {
        if (schema == null || schema.getExtensions() == null) {
            return List.of();
        }

        if (!(schema.getExtensions()
                .get(ANNOTATIONS) instanceof List<?> annotations)) {
            return List.of();
        }

        return annotations.stream().filter(Annotation.class::isInstance)
                .map(Annotation.class::cast).map(Annotation::getName).toList();
    }

    private static boolean isValue(SignatureModel signature) {
        return signature.isString() || signature.isCharacter()
                || signature.isBoolean() || signature.hasIntegerType()
                || signature.isBigInteger() || signature.hasFloatType()
                || signature.isBigDecimal() || signature.isDate()
                || signature.isDateTime();
    }

    private static String name(SignatureModel signature) {
        if (signature instanceof ClassRefSignatureModel classRef) {
            return classRef.getClassInfo().getName();
        }

        // A primitive says its own name, which is all of it: what it is
        // annotated with belongs to the value rather than to the type, and
        // reads as part of the name otherwise
        if (signature instanceof BaseSignatureModel base) {
            return base.getType().getName();
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
