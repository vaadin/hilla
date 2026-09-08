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
package com.vaadin.hilla.parser.plugins.subtypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.EntityPlugin;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;

/**
 * This plugin adds support for {@code @JsonTypeInfo} and {@code @JsonSubTypes}.
 */
public final class SubTypesPlugin extends AbstractPlugin<PluginConfiguration> {
    /**
     * Marks a discriminator property which the type declares itself, as
     * {@code As.EXISTING_PROPERTY} does, so that the generated model keeps it:
     * it is an ordinary property as well as the type id. A discriminator
     * without the mark exists in the serialized form alone, and a form has
     * nothing to bind to it.
     */
    private static final String EXISTING_PROPERTY = "x-existing-property";

    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        // deal with the union nodes, which does not correspond to an existing
        // class, but express the union of all the @JsonSubTypes
        if (nodePath.getNode() instanceof UnionNode) {
            var unionNode = (UnionNode) nodePath.getNode();
            var cls = (Class<?>) unionNode.getSource().get();

            // verify that the class has a @JsonTypeInfo annotation
            // and then add all the @JsonSubTypes to the schema as a `oneOf`
            if (cls.getAnnotationsByType(JsonTypeInfo.class).length > 0) {
                var schema = (Schema<?>) unionNode.getTarget();
                getJsonSubTypes(cls).map(JsonSubTypes.Type::value)
                        .forEach(c -> {
                            schema.addOneOfItem(new Schema<Object>() {
                                {
                                    set$ref("#/components/schemas/"
                                            + c.getName());
                                }
                            });
                        });

                // expose the name of the discriminator property, so that the
                // TypeScript generator does not have to guess it
                findSubTypesInfo(cls)
                        .flatMap(SubTypesInfo::discriminatorProperty)
                        .ifPresent(property -> schema.setDiscriminator(
                                new Discriminator().propertyName(property)));
            }

            // attach the schema to the openapi
            EntityPlugin.attachSchemaWithNameToOpenApi(unionNode.getTarget(),
                    cls.getName() + "Union",
                    (OpenAPI) nodePath.getParentPath().getNode().getTarget());
        }

        // entity nodes mentioned in a @JsonSubTypes annotation found anywhere
        // in their class hierarchy must have a discriminator property whose
        // values come from the annotation
        if (nodePath.getNode() instanceof EntityNode) {
            var entityNode = (EntityNode) nodePath.getNode();
            var cls = (Class<?>) entityNode.getSource().get();

            var info = findSubTypesInfo(cls);
            var property = info.flatMap(SubTypesInfo::discriminatorProperty);
            var values = info.map(i -> i.discriminatorValues(cls))
                    .orElseGet(List::of);

            if (property.isPresent() && !values.isEmpty()) {
                addDiscriminatorProperty(entityNode.getTarget(), property.get(),
                        values);
            }
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        if (!(nodeDependencies.getNode() instanceof TypedNode)) {
            return nodeDependencies;
        }

        var typedNode = (TypedNode) nodeDependencies.getNode();
        if (!(typedNode.getType() instanceof ClassRefSignatureModel)) {
            return nodeDependencies;
        }

        var ref = (ClassRefSignatureModel) typedNode.getType();
        if (ref.isJDKClass() || ref.isDate() || ref.isIterable()) {
            return nodeDependencies;
        }

        // all types mentioned in @JsonSubTypes must be parsed, even if they are
        // not used directly
        Class<?> refClass = (Class<?>) ref.getClassInfo().get();
        var subTypes = getJsonSubTypes(refClass).map(JsonSubTypes.Type::value)
                .map(ClassInfoModel::of).<Node<?, ?>> map(EntityNode::of);

        // create a union node for classes annotated with @JsonTypeInfo
        if (refClass.getAnnotationsByType(JsonTypeInfo.class).length > 0) {
            var unionType = UnionNode.of(ref.getClassInfo());
            subTypes = Stream.concat(Stream.of(unionType), subTypes);
        }

        return nodeDependencies.appendRelatedNodes(subTypes);
    }

    private static Stream<JsonSubTypes.Type> getJsonSubTypes(Class<?> cls) {
        return Optional.of(cls)
                .map(c -> c.getAnnotationsByType(JsonSubTypes.class))
                .filter(a -> a.length > 0).map(a -> a[0])
                .map(JsonSubTypes::value).stream().flatMap(Arrays::stream);
    }

    /**
     * Looks for the {@code @JsonTypeInfo} and {@code @JsonSubTypes}
     * annotations, starting from the given class and then walking up its
     * hierarchy. Checking the class itself allows a class that declares the
     * subtypes to be a subtype of itself, while walking up the hierarchy covers
     * subtypes that are not direct descendants of the declaring class, as well
     * as those whose supertype is an interface.
     */
    private static Optional<SubTypesInfo> findSubTypesInfo(Class<?> cls) {
        return hierarchyOf(cls)
                .filter(c -> c.getAnnotation(JsonTypeInfo.class) != null
                        && c.getAnnotation(JsonSubTypes.class) != null)
                .findFirst()
                .map(c -> new SubTypesInfo(c.getAnnotation(JsonTypeInfo.class),
                        c.getAnnotation(JsonSubTypes.class)));
    }

    /**
     * Returns the given class followed by its supertypes, superclasses and
     * interfaces alike: the same types, in the same order, from which Jackson
     * collects the class annotations that apply to a type. The interfaces of a
     * class come before its superclass, and each of them is followed by its own
     * supertypes, so that the annotation closest to the class wins.
     */
    private static Stream<Class<?>> hierarchyOf(Class<?> cls) {
        var hierarchy = new LinkedHashSet<Class<?>>();
        collectHierarchy(cls, hierarchy);
        return hierarchy.stream();
    }

    private static void collectHierarchy(Class<?> cls,
            Set<Class<?>> hierarchy) {
        if (cls == null || !hierarchy.add(cls)) {
            return;
        }

        for (var iface : cls.getInterfaces()) {
            collectHierarchy(iface, hierarchy);
        }

        collectHierarchy(cls.getSuperclass(), hierarchy);
    }

    private static void addDiscriminatorProperty(Schema<?> schema,
            String property, List<String> values) {
        // a subtype is rendered as a composed schema, where the properties of
        // the subtype itself are in the object schema of the `anyOf` list,
        // while a class that declares the subtypes is a plain object schema
        if (schema instanceof ComposedSchema composedSchema
                && composedSchema.getAnyOf() != null) {
            composedSchema.getAnyOf().stream()
                    .filter(ObjectSchema.class::isInstance)
                    .map(ObjectSchema.class::cast).forEach(
                            s -> setDiscriminatorProperty(s, property, values));
        } else {
            setDiscriminatorProperty(schema, property, values);
        }
    }

    /**
     * Narrows the discriminator property of one schema to the values the type
     * accepts.
     *
     * <p>
     * With {@code As.EXISTING_PROPERTY} the discriminator is a property the
     * type declares itself, and everything else known about it, such as the
     * Java type it comes from, has to survive: only the accepted values are
     * added to it, and it is marked as declared so that the model keeps it. A
     * discriminator which the type does not declare is added instead, and
     * exists in the serialized form alone.
     */
    private static void setDiscriminatorProperty(Schema<?> schema,
            String property, List<String> values) {
        var properties = schema.getProperties();
        var declared = properties == null ? null : properties.get(property);

        if (declared != null) {
            values.forEach(declared::addEnumItemObject);
            declared.setExample(values.get(0));
            declared.addExtension(EXISTING_PROPERTY, true);
            return;
        }

        schema.addProperty(property, discriminatorSchema(values));
    }

    /**
     * The schema of the discriminator property: it accepts the value of the
     * type itself, which comes first and is kept as the example, along with the
     * values of the subtypes below it.
     */
    private static StringSchema discriminatorSchema(List<String> values) {
        var schema = new StringSchema();
        schema.setExample(values.get(0));
        values.forEach(schema::addEnumItem);
        return schema;
    }

    /**
     * The {@code @JsonTypeInfo} and {@code @JsonSubTypes} annotations that
     * apply to a class.
     */
    private record SubTypesInfo(JsonTypeInfo typeInfo, JsonSubTypes subTypes) {
        /**
         * The type id strategies whose values are known here: both take the id
         * from the annotations and fall back to the name of the class itself.
         * The ids of the class based strategies are built from the name of the
         * base type, and those of a custom resolver are only known to the
         * resolver itself, so no property is generated for them: leaving it out
         * is better than declaring values that the server never sends.
         */
        private static final Set<JsonTypeInfo.Id> SUPPORTED_IDS = EnumSet
                .of(JsonTypeInfo.Id.NAME, JsonTypeInfo.Id.SIMPLE_NAME);

        /**
         * Returns the name of the property that holds the type discriminator,
         * or an empty optional if the type information is not serialized as a
         * property of the object itself, or if its values are not known here.
         */
        Optional<String> discriminatorProperty() {
            var include = typeInfo.include();

            if (include != JsonTypeInfo.As.PROPERTY
                    && include != JsonTypeInfo.As.EXISTING_PROPERTY) {
                return Optional.empty();
            }

            if (!SUPPORTED_IDS.contains(typeInfo.use())) {
                return Optional.empty();
            }

            var property = typeInfo.property();

            return property.isBlank()
                    ? Optional
                            .ofNullable(typeInfo.use().getDefaultPropertyName())
                    : Optional.of(property);
        }

        /**
         * Returns the values of the type discriminator that the given class
         * accepts: its own value, followed by the values of the subtypes below
         * it, which narrow the discriminator further. The list is empty if the
         * class is not mentioned among the subtypes.
         */
        List<String> discriminatorValues(Class<?> cls) {
            var own = Arrays.stream(subTypes.value())
                    .filter(type -> cls.equals(type.value())).findAny()
                    .map(this::discriminatorValue);

            if (own.isEmpty()) {
                return List.of();
            }

            return Stream.concat(own.stream(),
                    Arrays.stream(subTypes.value())
                            .filter(type -> !cls.equals(type.value())
                                    && cls.isAssignableFrom(type.value()))
                            .map(this::discriminatorValue))
                    .toList();
        }

        /**
         * Returns the value of the type discriminator for the given subtype:
         * the name given in the {@code @JsonSubTypes.Type} annotation, then the
         * {@code @JsonTypeName} annotation of the subtype or of one of its
         * supertypes, which is where Jackson looks for it too, and finally the
         * name of the class itself.
         */
        private String discriminatorValue(JsonSubTypes.Type type) {
            if (!type.name().isEmpty()) {
                return type.name();
            }

            if (type.names().length > 0) {
                return type.names()[0];
            }

            return hierarchyOf(type.value())
                    .map(c -> c.getAnnotation(JsonTypeName.class))
                    .filter(Objects::nonNull).map(JsonTypeName::value)
                    .filter(name -> !name.isEmpty()).findFirst()
                    .orElseGet(() -> defaultTypeId(type.value()));
        }

        /**
         * Returns the type id that Jackson builds from the name of the class
         * when no annotation gives one: the simple name for
         * {@code Id.SIMPLE_NAME}, and the binary name without its package for
         * {@code Id.NAME}, which keeps the enclosing classes of a nested class,
         * as in {@code Shape$Circle}.
         */
        private String defaultTypeId(Class<?> cls) {
            if (typeInfo.use() == JsonTypeInfo.Id.SIMPLE_NAME) {
                return cls.getSimpleName();
            }

            var name = cls.getName();

            return name.substring(name.lastIndexOf('.') + 1);
        }
    }

    /**
     * A node that represents the union of all the mentioned subclasses of a
     * class annotated with {@code @JsonSubTypes}.
     */
    public static class UnionNode
            extends AbstractNode<ClassInfoModel, Schema<?>> {
        private UnionNode(@NonNull ClassInfoModel source,
                @NonNull ObjectSchema target) {
            super(source, target);
        }

        @NonNull
        static public UnionNode of(@NonNull ClassInfoModel model) {
            return new UnionNode(model, new ObjectSchema());
        }
    }
}
