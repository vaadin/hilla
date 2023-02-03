package dev.hilla.parser.plugins.backbone;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.Node;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.models.TypeArgumentModel;
import dev.hilla.parser.models.TypeParameterModel;
import dev.hilla.parser.models.TypeVariableModel;
import dev.hilla.parser.models.jackson.JacksonPropertyTypeModel;
import dev.hilla.parser.plugins.backbone.nodes.EndpointExposedNode;
import dev.hilla.parser.plugins.backbone.nodes.EndpointNode;
import dev.hilla.parser.plugins.backbone.nodes.EndpointNonExposedNode;
import dev.hilla.parser.plugins.backbone.nodes.EndpointSignatureNode;
import dev.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;

public final class EndpointExposedPlugin
        extends AbstractPlugin<PluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    @Nonnull
    @Override
    public Node<?, ?> resolve(@Nonnull Node<?, ?> node,
            @Nonnull NodePath<?> parentPath) {
        if (!(node instanceof TypeSignatureNode)
                || node.getSource() instanceof JacksonPropertyTypeModel) {
            return node;
        }

        var signature = (SignatureModel) node.getSource();
        if (!(signature instanceof TypeParameterModel)) {
            return node;
        }

        return TypeSignatureNode.of(resolveTypeParameter(
                (TypeParameterModel) signature, parentPath));
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();

        if (node instanceof EndpointNode || node instanceof EndpointExposedNode
                || node instanceof EndpointNonExposedNode) {
            // Attach type signature nodes for hierarchy parent superclass and
            // implemented interfaces, if any
            var cls = (ClassInfoModel) node.getSource();
            return nodeDependencies
                    .appendChildNodes(scanEndpointClassSignature(cls));
        }

        if (node instanceof EndpointSignatureNode) {
            // Attach the referenced class from the endpoint hierarchy type
            // signature
            var classRef = ((ClassRefSignatureModel) node.getSource());
            var classInfo = classRef.getClassInfo();
            return nodeDependencies.appendChildNodes(
                    Stream.of(createEndpointHierarchyClassNode(classInfo)));
        }

        return nodeDependencies;
    }

    /**
     * Creates a node that wraps the given endpoint hierarchy class as a source.
     * If the class is annotated with the configured {@code @EndpointExposed}
     * annotation, uses {@code EndpointExposedNode}, otherwise uses
     * {@code EndpointNonExposedNode}.
     *
     * @param classInfo
     *            The class from the endpoint hierarchy.
     * @return The node for the class.
     */
    private Node<?, ?> createEndpointHierarchyClassNode(
            ClassInfoModel classInfo) {
        var endpointExposedAnnotationName = getStorage().getParserConfig()
                .getEndpointExposedAnnotationName();
        var exposed = classInfo.getAnnotationsStream()
                .map(AnnotationInfoModel::getName)
                .anyMatch(endpointExposedAnnotationName::equals);
        var classInfoNode = exposed ? EndpointExposedNode.of(classInfo)
                : EndpointNonExposedNode.of(classInfo);
        return classInfoNode;
    }

    /**
     * Replaces generic type parameters used in {@code @EndpointExposed} with
     * their arguments defined in type signatures of endpoint class hierarchy
     * descendants.
     *
     * @param typeParameter
     * @param path
     * @return
     */
    private SignatureModel resolveTypeParameter(
            TypeParameterModel typeParameter, NodePath<?> path) {
        var closestEndpointSignaturePath = path.stream()
                .filter(p -> p.getNode() instanceof EndpointSignatureNode)
                .findFirst();
        if (closestEndpointSignaturePath.isEmpty()) {
            return typeParameter;
        }

        var endpointSignaturePath = closestEndpointSignaturePath.get();
        var classRef = (ClassRefSignatureModel) endpointSignaturePath.getNode()
                .getSource();

        var paramIndex = classRef.getClassInfo().getTypeParameters()
                .indexOf(typeParameter);
        var typeArg = classRef.getTypeArguments().get(paramIndex);
        if (!typeArg.getWildcard().equals(TypeArgumentModel.Wildcard.NONE)) {
            // TODO: add resolving for wildcard type arguments
            return typeParameter;
        }

        var signature = typeArg.getAssociatedTypes().get(0);
        // Recursively resolve type variables
        if (signature instanceof TypeVariableModel) {
            var endpointTypeParameter = ((TypeVariableModel) signature)
                    .resolve();
            return resolveTypeParameter(endpointTypeParameter,
                    endpointSignaturePath.getParentPath());
        }

        return signature;
    }

    /**
     * Creates and returns nodes for the type signatures of the superclass and
     * implemented interfaces of the endpoint or endpoint hierarchy ancestor
     * class.
     *
     * <p>
     * The signatures are used later for type arguments lookup to resolve
     * generic type variables in exposed method parameter and result types, see
     * the {@code resolve(Node, NodePath)} lifecycle hook.
     *
     * @param endpointClass
     *            The endpoint or endpoint hierarchy class.
     * @return The stream of nodes.
     */
    private Stream<Node<?, ?>> scanEndpointClassSignature(
            ClassInfoModel endpointClass) {
        return Stream
                .concat(endpointClass.getSuperClass().stream(),
                        endpointClass.getInterfacesStream())
                .map(EndpointSignatureNode::of);
    }
}
