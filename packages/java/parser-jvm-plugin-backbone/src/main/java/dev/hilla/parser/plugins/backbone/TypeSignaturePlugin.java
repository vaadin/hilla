package dev.hilla.parser.plugins.backbone;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.models.ArraySignatureModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.ReflectionSignatureModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.models.SpecializedModel;
import dev.hilla.parser.models.TypeArgumentModel;
import dev.hilla.parser.models.TypeParameterModel;
import dev.hilla.parser.models.TypeVariableModel;
import dev.hilla.parser.plugins.backbone.nodes.CompositeTypeSignatureNode;
import dev.hilla.parser.plugins.backbone.nodes.EntityNode;
import dev.hilla.parser.plugins.backbone.nodes.MethodNode;
import dev.hilla.parser.plugins.backbone.nodes.MethodParameterNode;
import dev.hilla.parser.plugins.backbone.nodes.PropertyNode;
import dev.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;

import dev.hilla.parser.plugins.backbone.nodes.TypedNode;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public final class TypeSignaturePlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof TypedNode) {
            var typedNode = (TypedNode) nodePath.getNode();
            typedNode.setTarget(
                    new SchemaProcessor(typedNode.getType()).process());
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        if (!(nodePath.getNode() instanceof TypedNode)) {
            return;
        }

        var node = (TypedNode) nodePath.getNode();
        var schema = node.getTarget();
        var parentNode = nodePath.getParentPath().getNode();
        var grandParentNode = nodePath.getParentPath().getParentPath()
                .getNode();
        if (parentNode instanceof MethodNode) {
            attachSchemaToMethod(schema, (MethodNode) parentNode);
        } else if (parentNode instanceof MethodParameterNode
                && grandParentNode instanceof MethodNode) {
            attachSchemaToParameterOfMethod(schema,
                    ((MethodParameterNode) parentNode),
                    ((MethodNode) grandParentNode));
        } else if (parentNode instanceof EntityNode
                && schema instanceof ComposedSchema) {
            attachSchemaToEntitySubclass((ComposedSchema) schema,
                    (EntityNode) parentNode);
        } else if (parentNode instanceof PropertyNode
                && grandParentNode instanceof EntityNode) {
            attachSchemaToPropertyOfEntity(schema, (PropertyNode) parentNode,
                    (EntityNode) grandParentNode);
        } else if (parentNode instanceof TypedNode) {
            attachSchemaToNestingParentSignature(schema,
                    (TypedNode) parentNode);
        }
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();
        if (node instanceof MethodNode) {
            return scanMethodNode((MethodNode) node, nodeDependencies);
        } else if (node instanceof MethodParameterNode) {
            return scanMethodParameter((MethodParameterNode) node,
                    nodeDependencies);
        } else if (node instanceof EntityNode) {
            return scanEntity((EntityNode) node, nodeDependencies);
        } else if (node instanceof PropertyNode) {
            return scanProperty((PropertyNode) node, nodeDependencies);
        } else if (node instanceof CompositeTypeSignatureNode) {
            return scanCompositeTypeSignature((CompositeTypeSignatureNode) node,
                    nodeDependencies);
        } else if (node instanceof TypeSignatureNode) {
            return scanTypeSignature((TypeSignatureNode) node,
                    nodeDependencies);
        }

        return nodeDependencies;
    }

    private void attachSchemaToEntitySubclass(ComposedSchema schema,
            EntityNode entityNode) {
        var subclassSchema = entityNode.getTarget();
        schema.addAnyOfItem(subclassSchema);
        schema.setNullable(null);
        entityNode.setTarget(schema);
    }

    private void attachSchemaToMethod(Schema<?> schema, MethodNode methodNode) {
        methodNode.getTarget().getPost().getResponses().get("200")
                .setContent(new Content().addMediaType(MethodPlugin.MEDIA_TYPE,
                        new MediaType().schema(schema)));
    }

    private void attachSchemaToNestingParentSignature(Schema<?> schema,
            TypedNode parentNode) {
        var parentSchema = parentNode.getTarget();
        if (parentSchema instanceof ArraySchema) {
            ((ArraySchema) parentSchema).setItems(schema);
        } else if (parentSchema instanceof MapSchema) {
            parentSchema.additionalProperties(schema);
        } else {
            // The nested schema replaces parent for type argument, type
            // parameter, type variable, and optional signatures
            parentNode.setTarget(schema);
        }
    }

    private void attachSchemaToParameterOfMethod(Schema<?> schema,
            MethodParameterNode methodParameterNode, MethodNode methodNode) {
        var requestMap = (ObjectSchema) methodNode.getTarget().getPost()
                .getRequestBody().getContent().get(MethodPlugin.MEDIA_TYPE)
                .getSchema();
        requestMap.addProperties(methodParameterNode.getTarget(), schema);
    }

    private void attachSchemaToPropertyOfEntity(Schema<?> schema,
            PropertyNode propertyNode, EntityNode entityNode) {
        var propertyName = propertyNode.getTarget();
        entityNode.getTarget().addProperties(propertyName, schema);
    }

    /**
     * If a given type signature refers other types, returns other type
     * signatures that are referred. These include:
     *
     * <ul>
     * <li>Array nested type</li>
     * <li>Type argument of an optional and iterable types</li>
     * <li>Item type for a map</li>
     * <li>Known type parameter for a type variable</li>
     * <li>Known bounds for a type parameter</li>
     * </ul>
     *
     * <p>
     * Returns empty list if no referred types are found.
     * </p>
     *
     * @param signature
     *            the singature to consider
     * @return list of referred types
     */
    private List<SignatureModel> getReferredTypes(SignatureModel signature) {
        var items = List.<SignatureModel> of();

        if (signature.isArray()) {
            items = List.of(((ArraySignatureModel) signature).getNestedType());
        } else if (signature.isIterable()) {
            var typeArguments = ((ClassRefSignatureModel) signature)
                    .getTypeArguments();

            if (!typeArguments.isEmpty()) {
                items = List.of(typeArguments.get(0));
            }
        } else if (signature.isOptional()) {
            var typeArguments = ((ClassRefSignatureModel) signature)
                    .getTypeArguments();

            if (!typeArguments.isEmpty()) {
                items = List.of(typeArguments.get(0));
            }
        } else if (signature.isMap()) {
            var typeArguments = ((ClassRefSignatureModel) signature)
                    .getTypeArguments();

            if (!typeArguments.isEmpty()) {
                items = List.of(typeArguments.get(1));
            }
        } else if (signature.isTypeArgument()) {
            var associatedTypes = ((TypeArgumentModel) signature)
                    .getAssociatedTypes();

            if (!associatedTypes.isEmpty()) {
                items = List.of(associatedTypes.get(0));
            }
        } else if (signature.isTypeParameter()) {
            var bounds = ((TypeParameterModel) signature).getBounds();

            if (!bounds.isEmpty()) {
                items = bounds.stream().filter(Objects::nonNull)
                        .filter(Predicate.not(SpecializedModel::isNativeObject))
                        .collect(Collectors.toList());
            }
        } else if (signature.isTypeVariable()) {
            items = List.of(((TypeVariableModel) signature).resolve());
        }

        return items;
    }

    private List<SignatureModel> filterCompatible(List<SignatureModel> types) {
        if (types.isEmpty()) {
            // Nothing to filter, return the original (empty) list.
            return types;
        }

        var primary = signatureToTypeString(types.get(0));
        return types.stream()
                .filter(type -> primary.equals(signatureToTypeString(type)))
                .collect(Collectors.toList());
    }

    private NodeDependencies scanCompositeTypeSignature(
            CompositeTypeSignatureNode node,
            NodeDependencies nodeDependencies) {
        var types = node.getSource();
        // Find referred types for all composite type items
        var referredTypes = types.stream().map(this::getReferredTypes)
                .flatMap(List::stream).collect(Collectors.toList());
        return scanTypes(referredTypes, nodeDependencies);
    }

    private NodeDependencies scanEntity(EntityNode node,
            NodeDependencies nodeDependencies) {
        var cls = node.getSource();
        if (cls.getSuperClass().isPresent()
                && cls.getSuperClass().get().isNonJDKClass()) {
            return nodeDependencies.appendChildNodes(
                    Stream.of(TypeSignatureNode.of(cls.getSuperClass().get())));
        }

        return nodeDependencies;
    }

    private NodeDependencies scanMethodNode(MethodNode methodNode,
            NodeDependencies nodeDependencies) {
        if (methodNode.getSource().getResultType().isVoid()) {
            return nodeDependencies;
        }

        var resultTypeNode = TypeSignatureNode
                .of(methodNode.getSource().getResultType());
        return nodeDependencies.appendChildNodes(Stream.of(resultTypeNode));
    }

    private NodeDependencies scanMethodParameter(
            MethodParameterNode methodParameterNode,
            NodeDependencies nodeDependencies) {
        return nodeDependencies.appendChildNodes(Stream.of(TypeSignatureNode
                .of(methodParameterNode.getSource().getType())));
    }

    private NodeDependencies scanProperty(PropertyNode propertyNode,
            NodeDependencies nodeDependencies) {
        var property = propertyNode.getSource();
        var types = property.getAssociatedTypes();
        return scanTypes(types, nodeDependencies);
    }

    private NodeDependencies scanTypes(List<SignatureModel> types,
            NodeDependencies nodeDependencies) {
        // Discard type signatures that are different from the primary one
        types = filterCompatible(types);
        if (types.size() > 1) {
            return nodeDependencies.appendChildNodes(
                    Stream.of(CompositeTypeSignatureNode.of(types)));
        } else {
            return nodeDependencies.appendChildNodes(
                    types.stream().map(TypeSignatureNode::of));
        }
    }

    private NodeDependencies scanTypeSignature(TypeSignatureNode node,
            NodeDependencies nodeDependencies) {
        var signature = node.getSource();
        var referredTypes = getReferredTypes(signature);
        return nodeDependencies.appendChildNodes(
                referredTypes.stream().map(TypeSignatureNode::of));
    }

    private String signatureToTypeString(SignatureModel type) {
        // Only reflection types are supported.
        // This converts to a name without annotations.
        var annotatedElement = ((ReflectionSignatureModel) type).get();
        if (annotatedElement instanceof AnnotatedType) {
            return ((AnnotatedType) annotatedElement).getType().getTypeName();
        } else if (annotatedElement instanceof Type) {
            return ((Type) annotatedElement).getTypeName();
        } else {
            return annotatedElement.toString();
        }
    }
}
