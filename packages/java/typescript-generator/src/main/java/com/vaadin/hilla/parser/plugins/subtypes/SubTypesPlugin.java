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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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

        // entity nodes whose superclass has a @JsonSubTypes annotation must
        // have a discriminator property whose name and value come from the
        // annotations
        if (nodePath.getNode() instanceof EntityNode) {
            var entityNode = (EntityNode) nodePath.getNode();
            var cls = (Class<?>) entityNode.getSource().get();

            var info = Optional.ofNullable(cls.getSuperclass())
                    .flatMap(SubTypesPlugin::findSubTypesInfo);
            var property = info.flatMap(SubTypesInfo::discriminatorProperty);
            var value = info.flatMap(i -> i.discriminatorValue(cls));

            if (property.isPresent() && value.isPresent()) {
                addDiscriminatorProperty(
                        (ComposedSchema) entityNode.getTarget(), property.get(),
                        value.get());
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

    private static Optional<SubTypesInfo> findSubTypesInfo(Class<?> cls) {
        var typeInfo = cls.getAnnotation(JsonTypeInfo.class);
        var subTypes = cls.getAnnotation(JsonSubTypes.class);

        return typeInfo != null && subTypes != null
                ? Optional.of(new SubTypesInfo(typeInfo, subTypes))
                : Optional.empty();
    }

    private static void addDiscriminatorProperty(ComposedSchema schema,
            String property, String typeName) {
        // the properties of the subtype itself are in the object schema of the
        // `anyOf` list
        schema.getAnyOf().stream().filter(ObjectSchema.class::isInstance)
                .map(ObjectSchema.class::cast).forEach(s -> {
                    var discriminator = new StringSchema();
                    discriminator.setExample(typeName);
                    s.addProperty(property, discriminator);
                });
    }

    /**
     * The {@code @JsonTypeInfo} and {@code @JsonSubTypes} annotations that
     * apply to a class.
     */
    private record SubTypesInfo(JsonTypeInfo typeInfo, JsonSubTypes subTypes) {
        /**
         * Returns the name of the property that holds the type discriminator,
         * or an empty optional if the type information is not serialized as a
         * property of the object itself.
         */
        Optional<String> discriminatorProperty() {
            var include = typeInfo.include();

            if (include != JsonTypeInfo.As.PROPERTY
                    && include != JsonTypeInfo.As.EXISTING_PROPERTY) {
                return Optional.empty();
            }

            var property = typeInfo.property();

            return property.isBlank()
                    ? Optional
                            .ofNullable(typeInfo.use().getDefaultPropertyName())
                    : Optional.of(property);
        }

        /**
         * Returns the value of the type discriminator for the given subtype,
         * which defaults to the simple class name when the annotation does not
         * specify a name, or an empty optional if the class is not mentioned
         * among the subtypes.
         */
        Optional<String> discriminatorValue(Class<?> cls) {
            return Arrays.stream(subTypes.value())
                    .filter(type -> cls.equals(type.value())).findAny()
                    .map(type -> {
                        if (!type.name().isEmpty()) {
                            return type.name();
                        }

                        return type.names().length > 0 ? type.names()[0]
                                : cls.getSimpleName();
                    });
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
