package dev.hilla.parser.plugins.backbone;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.RootNode;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.plugins.backbone.nodes.EntityNode;
import dev.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public final class EntityPlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof EntityNode) {
            var entityNode = (EntityNode) nodePath.getNode();
            var cls = entityNode.getSource();
            entityNode.setTarget(
                    cls.isEnum() ? enumSchema(cls) : new ObjectSchema());
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof EntityNode
                && nodePath.getParentPath().getNode() instanceof RootNode) {
            var schema = (Schema<?>) nodePath.getNode().getTarget();
            var cls = (ClassInfoModel) nodePath.getNode().getSource();
            var openApi = (OpenAPI) nodePath.getParentPath().getNode()
                    .getTarget();

            attachSchemaWithNameToOpenApi(schema, cls.getName(), openApi);
        }
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (!(nodeDependencies.getNode() instanceof TypeSignatureNode)) {
            return nodeDependencies;
        }

        var signatureNode = (TypeSignatureNode) nodeDependencies.getNode();
        if (!(signatureNode.getSource() instanceof ClassRefSignatureModel)) {
            return nodeDependencies;
        }

        var ref = (ClassRefSignatureModel) signatureNode.getSource();
        if (ref.isJDKClass() || ref.isDate() || ref.isIterable()) {
            return nodeDependencies;
        }

        return nodeDependencies.appendRelatedNodes(
                Stream.of(EntityNode.of(ref.getClassInfo())));
    }

    private void attachSchemaWithNameToOpenApi(Schema<?> schema, String name,
            OpenAPI openApi) {
        var components = openApi.getComponents();

        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        components.addSchemas(name, schema);
    }

    private Schema<?> enumSchema(ClassInfoModel entity) {
        var schema = new StringSchema();

        schema.setEnum(entity.getFieldsStream().filter(FieldInfoModel::isPublic)
                .map(FieldInfoModel::getName).collect(Collectors.toList()));

        return schema;
    }
}
