package dev.hilla.parser.core.dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.NamedModel;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.RootNode;
import dev.hilla.parser.test.nodes.EndpointNode;
import dev.hilla.parser.test.nodes.EntityNode;
import dev.hilla.parser.test.nodes.FieldNode;
import dev.hilla.parser.test.nodes.MethodNode;
import dev.hilla.parser.test.nodes.MethodParameterNode;
import dev.hilla.parser.test.nodes.TypeSignatureNode;

final class DependencyPlugin extends AbstractPlugin<PluginConfiguration> {
    public static final String ENTITY_DEPS_STORAGE_KEY = "x-dependency-entities";
    public static final String DEPS_MEMBERS_STORAGE_KEY = "x-dependency-entity-members";
    public static final String ENDPOINTS_DIRECT_DEPS_STORAGE_KEY = "x-dependency-endpoints";

    private final List<String> entityDependencies = new ArrayList<>();
    private final List<String> dependencyMembers = new ArrayList<>();
    private final List<String> endpointDependencies = new ArrayList<>();

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();
        if (node instanceof RootNode) {
            var rootNode = (RootNode) node;
            return nodeDependencies.appendChildNodes(rootNode.getSource()
                    .getClassesWithAnnotation(getStorage().getParserConfig()
                            .getEndpointAnnotationName())
                    .stream().map(ClassInfoModel::of).map(EndpointNode::of));
        } else if ((node instanceof EndpointNode)) {
            var cls = (ClassInfoModel) node.getSource();
            return nodeDependencies.appendChildNodes(
                    cls.getMethodsStream().map(MethodNode::of));
        } else if (node instanceof MethodNode) {
            var methodNode = (MethodNode) node;
            var resultTypeNode = TypeSignatureNode
                    .of(methodNode.getSource().getResultType());
            return nodeDependencies.prependChildNodes(Stream.of(resultTypeNode))
                    .appendChildNodes(
                            methodNode.getSource().getParametersStream()
                                    .map(param -> MethodParameterNode.of(param,
                                            Optional.ofNullable(param.getName())
                                                    .orElse("_unnamed"))));
        } else if ((node instanceof EntityNode)) {
            var cls = (ClassInfoModel) node.getSource();
            return nodeDependencies
                    .appendChildNodes(cls.getFieldsStream().map(FieldNode::of));
        } else if (node instanceof FieldNode) {
            var fieldNode = (FieldNode) node;
            return nodeDependencies.appendChildNodes(Stream
                    .of(TypeSignatureNode.of(fieldNode.getSource().getType())));
        } else if ((node instanceof TypeSignatureNode)
                && (node.getSource() instanceof ClassRefSignatureModel)
                && !(((ClassRefSignatureModel) node.getSource())
                        .isJDKClass())) {
            var entityCls = ((ClassRefSignatureModel) node.getSource())
                    .getClassInfo();
            return nodeDependencies
                    .appendRelatedNodes(Stream.of(EntityNode.of(entityCls)));
        }
        return nodeDependencies;
    }

    @Override
    public void enter(NodePath<?> nodePath) {

    }

    @Override
    public void exit(NodePath<?> nodePath) {
        if (nodePath.getNode().getSource() instanceof NamedModel
                && (nodePath.getParentPath().getNode() instanceof EntityNode)) {
            var model = (NamedModel) nodePath.getNode().getSource();
            dependencyMembers.add(model.getName());
        }
        if ((nodePath.getNode() instanceof EntityNode)
                && (nodePath.getNode().getSource() instanceof ClassInfoModel)) {
            var model = (ClassInfoModel) nodePath.getNode().getSource();
            entityDependencies.add(model.getName());
        }
        if (nodePath.getNode().getSource() instanceof NamedModel && (nodePath
                .getParentPath().getNode() instanceof EndpointNode)) {
            var model = (NamedModel) nodePath.getNode().getSource();
            endpointDependencies.add(model.getName());
        }
        if (nodePath.getNode() instanceof RootNode) {
            var openApi = ((RootNode) nodePath.getNode()).getTarget();
            openApi.addExtension(ENTITY_DEPS_STORAGE_KEY, entityDependencies);
            openApi.addExtension(DEPS_MEMBERS_STORAGE_KEY, dependencyMembers);
            openApi.addExtension(ENDPOINTS_DIRECT_DEPS_STORAGE_KEY,
                    endpointDependencies);
        }
    }
}
