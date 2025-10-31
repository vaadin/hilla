package com.vaadin.hilla.parser.plugins.backbone;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.models.FieldInfoModel;
import com.vaadin.hilla.parser.models.SpecializedModel;
import com.vaadin.hilla.parser.models.TypeParameterModel;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;

import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;
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
            Schema<?> schema = cls.isEnum() ? enumSchema(cls)
                    : new ObjectSchema();
            entityNode.setTarget(schema);

            // Create an array of schemas for the type parameters
            var generics = entityNode.getSource().getTypeParameters().stream()
                    .filter(tp -> tp.getBounds().stream()
                            .filter(Objects::nonNull)
                            .noneMatch(Predicate
                                    .not(SpecializedModel::isNativeObject)))
                    .map(TypeParameterModel::getName).toList();

            if (!generics.isEmpty()) {
                schema.addExtension("x-type-parameters", generics);
            }
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

        return nodeDependencies.appendRelatedNodes(
                Stream.of(EntityNode.of(ref.getClassInfo())));
    }

    public static void attachSchemaWithNameToOpenApi(Schema<?> schema,
            String name, OpenAPI openApi) {
        var components = openApi.getComponents();

        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        components.addSchemas(name, schema);
    }

    private Schema<?> enumSchema(ClassInfoModel entity) {
        var schema = new StringSchema();

        schema.setEnum(entity.getFields().stream()
                .filter(FieldInfoModel::isPublic).map(FieldInfoModel::getName)
                .collect(Collectors.toList()));

        return schema;
    }
}
