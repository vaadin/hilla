package dev.hilla.parser.plugins.subtypes;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.Node;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.backbone.nodes.EntityNode;
import dev.hilla.parser.plugins.backbone.nodes.TypedNode;
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

public final class SubTypesPlugin extends AbstractPlugin<PluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof EntityNode) {
            var entityNode = (EntityNode) nodePath.getNode();
            var cls = (Class<?>) entityNode.getSource().get();

            if (cls.getAnnotationsByType(JsonTypeInfo.class).length > 0) {
                entityNode.setTarget(new Schema<>());
            }
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof EntityNode) {
            var entityNode = (EntityNode) nodePath.getNode();
            var cls = (Class<?>) entityNode.getSource().get();

            if (cls.getAnnotationsByType(JsonTypeInfo.class).length > 0) {
                var schema = (Schema<?>) entityNode.getTarget();
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

            Optional.ofNullable(cls.getSuperclass())
                    .map(SubTypesPlugin::getJsonSubTypes).stream()
                    .flatMap(Function.identity())
                    .filter(t->cls.equals(t.value())).findAny().ifPresent(t -> {
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

        var subTypes = getJsonSubTypes((Class<?>) ref.getClassInfo().get())
                .map(JsonSubTypes.Type::value).map(ClassInfoModel::of)
                .<Node<?, ?>> map(EntityNode::of);

        return nodeDependencies.appendRelatedNodes(subTypes);
    }

    private static Stream<JsonSubTypes.Type> getJsonSubTypes(Class<?> cls) {
        return Optional.of(cls)
                .map(c -> c.getAnnotationsByType(JsonSubTypes.class))
                .filter(a -> a.length > 0).map(a -> a[0])
                .map(JsonSubTypes::value).stream().flatMap(Arrays::stream);
    }
}
