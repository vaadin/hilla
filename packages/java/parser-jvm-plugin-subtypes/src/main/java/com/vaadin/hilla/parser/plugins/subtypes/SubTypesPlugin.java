package com.vaadin.hilla.parser.plugins.subtypes;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

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
            }

            // attach the schema to the openapi
            EntityPlugin.attachSchemaWithNameToOpenApi(unionNode.getTarget(),
                    cls.getName() + "Union",
                    (OpenAPI) nodePath.getParentPath().getNode().getTarget());
        }

        // entity nodes whose superclass has a @JsonSubTypes annotation must
        // have a @type property whose value comes from the annotation
        if (nodePath.getNode() instanceof EntityNode) {
            var entityNode = (EntityNode) nodePath.getNode();
            var cls = (Class<?>) entityNode.getSource().get();

            Optional.ofNullable(cls.getSuperclass())
                    .map(SubTypesPlugin::getJsonSubTypes).stream()
                    .flatMap(Function.identity())
                    .filter(t -> cls.equals(t.value())).findAny()
                    .ifPresent(t -> {
                        var schema = (ComposedSchema) entityNode.getTarget();
                        schema.getAnyOf().stream()
                                .filter(s -> s instanceof ObjectSchema)
                                .map(ObjectSchema.class::cast)
                                .forEach(s -> s.addProperty("@type",
                                        new StringSchema() {
                                            {
                                                setType("string");
                                                setExample(t.name());
                                            }
                                        }));
                    });
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
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
     * A node that represents the union of all the mentioned subclasses of a
     * class annotated with {@code @JsonSubTypes}.
     */
    public static class UnionNode
            extends AbstractNode<ClassInfoModel, Schema<?>> {
        private UnionNode(@Nonnull ClassInfoModel source,
                @Nonnull ObjectSchema target) {
            super(source, target);
        }

        @Nonnull
        static public UnionNode of(@Nonnull ClassInfoModel model) {
            return new UnionNode(model, new ObjectSchema());
        }
    }
}
