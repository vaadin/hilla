package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.Node;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.ParameterizedModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.models.TypeArgumentModel;
import dev.hilla.parser.models.TypeParameterModel;
import dev.hilla.parser.models.TypeVariableModel;
import dev.hilla.parser.plugins.backbone.nodes.EndpointExposedNode;
import dev.hilla.parser.plugins.backbone.nodes.EndpointSignatureNode;
import dev.hilla.parser.plugins.backbone.nodes.MethodNode;
import dev.hilla.parser.plugins.backbone.nodes.MethodParameterNode;
import dev.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;
import io.github.classgraph.TypeArgument;

public class TypeParameterPlugin extends AbstractPlugin<PluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    @Override
    @Nonnull
    public Node<?, ?> resolve(@Nonnull Node<?, ?> node,
        @Nonnull NodePath<?> parentPath) {
        if (node instanceof TypeSignatureNode) {
            return TypeSignatureNode.of(
                resolveTypeSignature((SignatureModel) node.getSource(),
                    parentPath));
        } else if (node instanceof EndpointSignatureNode) {
            return EndpointSignatureNode.of(
                resolveTypeSignature((SignatureModel) node.getSource(),
                    parentPath));
        } else {
            return node;
        }
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    private ClassRefSignatureModel resolveClassRef(
        ClassRefSignatureModel classRef, NodePath<?> path) {
        if (classRef.getTypeArguments().isEmpty()) {
            return classRef;
        }

        var typeArguments = classRef.getTypeArgumentsStream()
            .map(typeArgument -> resolveTypeArgument(typeArgument, path))
            .collect(Collectors.toList());
        return ClassRefSignatureModel.of(classRef.getClassInfo(), typeArguments,
            classRef.getAnnotations());
    }

    private TypeArgumentModel resolveTypeArgument(
        TypeArgumentModel typeArgument, NodePath<?> path) {
        if (typeArgument.getAssociatedTypes().isEmpty()) {
            return typeArgument;
        }

        var signatures = typeArgument.getAssociatedTypesStream()
            .map(signature -> resolveTypeSignature(signature, path))
            .collect(Collectors.toList());
        return TypeArgumentModel.of(typeArgument.getWildcard(), signatures,
            typeArgument.getAnnotations());
    }

    private SignatureModel resolveTypeSignature(SignatureModel signature,
        NodePath<?> path) {
        if (signature instanceof ClassRefSignatureModel) {
            // TODO: remove once
            // https://github.com/classgraph/classgraph/issues/706 is fixed.
            return resolveClassRef((ClassRefSignatureModel) signature, path);
        } else if (signature instanceof TypeVariableModel) {
            // TODO: remove once
            // https://github.com/classgraph/classgraph/issues/706 is fixed.
            return resolveTypeVariable((TypeVariableModel) signature, path);
        } else if (signature instanceof TypeParameterModel) {
            return resolveTypeParameter((TypeParameterModel) signature, path);
        } else {
            return signature;
        }
    }

    private TypeVariableModel resolveTypeVariable(
        TypeVariableModel typeVariable, NodePath<?> path) {
        var node = path.getNode();
        if (node instanceof MethodParameterNode ||
            node instanceof TypeSignatureNode ||
            node instanceof EndpointSignatureNode) {
            return resolveTypeVariable(typeVariable, path.getParentPath());
        }

        var parameterStream = Stream.<TypeParameterModel> empty();
        if (node.getSource() instanceof ParameterizedModel) {
            parameterStream = ((ParameterizedModel) node.getSource()).getTypeParametersStream();
        }

        var resolvedTypeVariable = parameterStream.filter(
            typeParameter -> typeParameter.getName()
                .equals(typeVariable.getName())).findFirst().map(
            typeParameter -> TypeVariableModel.of(typeParameter,
                typeVariable.getAnnotations()));

        if (node instanceof MethodNode && resolvedTypeVariable.isEmpty()) {
            // Method has no matching type parameter, so lookup in the class
            return resolveTypeVariable(typeVariable, path.getParentPath());
        }

        return resolvedTypeVariable.orElse(typeVariable);
    }

    private SignatureModel resolveTypeParameter(
        TypeParameterModel typeParameter, NodePath<?> path) {
        var closestEndpointExposedPath = path.stream()
            .filter(p -> p.getNode() instanceof EndpointExposedNode)
            .findFirst();

        if (closestEndpointExposedPath.isEmpty()) {
            return typeParameter;
        }

        var endpointExposedPath = closestEndpointExposedPath.get();
        var endpointExposedNode = (EndpointExposedNode) endpointExposedPath.getNode();
        var paramIndex = endpointExposedNode.getSource().getTypeParameters()
            .indexOf(typeParameter);
        var classRef = (ClassRefSignatureModel) ((EndpointSignatureNode) endpointExposedPath.getParentPath()
            .getNode()).getSource();
        var typeArg = classRef.getTypeArguments().get(paramIndex);
        if (!typeArg.getWildcard().equals(TypeArgument.Wildcard.NONE)) {
            return typeParameter;
        }

        var signature = typeArg.getAssociatedTypes().get(0);
        // Recursively resolve type variables
        if (signature instanceof TypeVariableModel) {
            var endpointTypeParameter = ((TypeVariableModel) signature).resolve();
            return resolveTypeParameter(endpointTypeParameter,
                endpointExposedPath.getParentPath());
        }

        return signature;
    }
}
