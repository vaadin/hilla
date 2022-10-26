package dev.hilla.parser.plugins.backbone;

import jakarta.annotation.Nonnull;
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
import dev.hilla.parser.plugins.backbone.nodes.EndpointSignatureNode;
import dev.hilla.parser.plugins.backbone.nodes.MethodNode;
import dev.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;

/**
 * Implements a workaround for the
 * <a href="https://github.com/classgraph/classgraph/issues/706">ClassGraph’s
 * {@code TypeVariable.resolve()} issue</a>. Processes type signatures to find
 * type variables, then manually resolves them to find respective type
 * parameters, and replaces the original type variables with artificial
 * pre-resolved ones, which use a known type parameter in {@code
 * TypeVariableModel.resolve()} to avoid calling the ClassGraph’s {@code
 * TypeVariable.resolve()} implementation.
 */
public class FixClassGraphPlugin extends AbstractPlugin<PluginConfiguration> {
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
            // Resolve signature in TypeSignatureNode
            return TypeSignatureNode.of(resolveTypeSignature(
                    (SignatureModel) node.getSource(), parentPath));
        } else if (node instanceof EndpointSignatureNode) {
            // Resolve signature in EndpointSingatureNode
            return EndpointSignatureNode.of(resolveTypeSignature(
                    (SignatureModel) node.getSource(), parentPath));
        } else {
            return node;
        }
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    /**
     * Processes the class reference to find and pre-resolve type variables in
     * its type arguments.
     *
     * @param classRef
     *            The class reference to process.
     * @param path
     *            The context path for type parameter lookup.
     * @return The original or processed class reference.
     */
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

    /**
     * Processes the type argument to find and pre-resolve type variables used
     * in the associated types.
     *
     * @param typeArgument
     *            The type argument to process.
     * @param path
     *            The context path for type parameter lookup.
     * @return The original or processed type argument.
     */
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

    /**
     * Processes the type signature to find and pre-resolve type variables.
     *
     * @param signature
     *            The type signature to process.
     * @param path
     *            The context path for type parameter lookup.
     * @return The original or processed type signature.
     */
    private SignatureModel resolveTypeSignature(SignatureModel signature,
            NodePath<?> path) {
        if (signature instanceof TypeVariableModel) {
            return resolveTypeVariable((TypeVariableModel) signature, path);
        } else if (signature instanceof ClassRefSignatureModel) {
            return resolveClassRef((ClassRefSignatureModel) signature, path);
        } else {
            return signature;
        }
    }

    /**
     * Pre-resolves the given type variable by looking up the type parameter
     * matching by name in the context path.
     *
     * @param typeVariable
     *            The type variable to process.
     * @param path
     *            The context path for type parameter lookup.
     * @return The pre-resolved type variable if a matching type parameter is
     *         found, otherwise the original type variable.
     */
    private TypeVariableModel resolveTypeVariable(
            TypeVariableModel typeVariable, NodePath<?> path) {
        var parameterStream = Stream.<TypeParameterModel> empty();
        var node = path.getNode();
        if (node.getSource() instanceof ParameterizedModel) {
            // Search among the type parameters of the node source
            parameterStream = ((ParameterizedModel) node.getSource())
                    .getTypeParametersStream();
        } else if (path.hasParentNodes()) {
            // Try resolving in the parent context
            return resolveTypeVariable(typeVariable, path.getParentPath());
        }

        var resolvedTypeVariable = parameterStream
                .filter(typeParameter -> typeParameter.getName()
                        .equals(typeVariable.getName()))
                .findFirst().map(typeParameter -> TypeVariableModel
                        .of(typeParameter, typeVariable.getAnnotations()));

        if (node instanceof MethodNode && resolvedTypeVariable.isEmpty()) {
            // The method has no matching type parameter, try resolving in
            // the enclosing class
            return resolveTypeVariable(typeVariable, path.getParentPath());
        }

        return resolvedTypeVariable.orElse(typeVariable);
    }

}
