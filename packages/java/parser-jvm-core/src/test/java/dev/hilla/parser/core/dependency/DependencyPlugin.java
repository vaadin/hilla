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
import dev.hilla.parser.node.EndpointNode;
import dev.hilla.parser.node.EntityNode;
import dev.hilla.parser.node.FieldNode;
import dev.hilla.parser.node.MethodNode;
import dev.hilla.parser.node.MethodParameterNode;
import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.NodePath;
import dev.hilla.parser.node.RootNode;
import dev.hilla.parser.node.TypeSignatureNode;

final class DependencyPlugin extends AbstractPlugin<PluginConfiguration> {
    public static final String ENTITY_DEPS_STORAGE_KEY = "DependencyPlugin_EntityDeps";
    public static final String DEPS_MEMBERS_STORAGE_KEY = "DependencyPlugin_DepsMembers";
    public static final String ENDPOINTS_DIRECT_DEPS_STORAGE_KEY = "DependencyPlugin_EndpointsDirectDeps";

    private final List<String> entityDependencies = new ArrayList<>();
    private final List<String> dependencyMembers = new ArrayList<>();
    private final List<String> endpointDependencies = new ArrayList<>();

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();
        if (node instanceof RootNode) {
            var rootNode = (RootNode) node;
            return NodeDependencies.of(rootNode, rootNode.getSource()
                    .getClassesWithAnnotation(
                        getStorage().getParserConfig().getEndpointAnnotationName())
                    .stream().map(ClassInfoModel::of).map(EndpointNode::of),
                Stream.empty());
        } else if ((node instanceof EndpointNode)) {
            var cls = (ClassInfoModel) node.getSource();
            return NodeDependencies.of(node,
                cls.getMethodsStream().map(MethodNode::of), Stream.empty());
        } else if (node instanceof MethodNode) {
            var methodNode = (MethodNode) node;
            var resultTypeNode = TypeSignatureNode.of(
                methodNode.getSource().getResultType());
            return NodeDependencies.of(methodNode,
                Stream.concat(Stream.of(resultTypeNode),
                    methodNode.getSource().getParametersStream().map(
                        param -> MethodParameterNode.of(param,
                            Optional.ofNullable(param.getName())
                                .orElse("_unnamed")))), Stream.empty());
        } else if ((node instanceof EntityNode)) {
            var cls = (ClassInfoModel) node.getSource();
            return NodeDependencies.of(node,
                cls.getFieldsStream().map(FieldNode::of), Stream.empty());
        } else if (node instanceof FieldNode) {
            var fieldNode = (FieldNode) node;
            return NodeDependencies.of(fieldNode, Stream.of(
                    TypeSignatureNode.of(fieldNode.getSource().getType())),
                Stream.empty());
        } else if ((node instanceof TypeSignatureNode) &&
            (node.getSource() instanceof ClassRefSignatureModel) &&
            !(((ClassRefSignatureModel) node.getSource()).isJDKClass())) {
            var entityCls = ((ClassRefSignatureModel) node.getSource()).getClassInfo();
            return NodeDependencies.of(node, nodeDependencies.getChildNodes(),
                Stream.of(EntityNode.of(entityCls)));
        }
        return nodeDependencies;
    }

    @Override
    public void enter(NodePath<?> nodePath) {

    }

    @Override
    public void exit(NodePath<?> nodePath) {
        if (nodePath.getNode().getSource() instanceof NamedModel &&
            (nodePath.getParentPath().getNode() instanceof EntityNode)) {
            var model = (NamedModel) nodePath.getNode().getSource();
            dependencyMembers.add(model.getName());
        }
        if ((nodePath.getNode() instanceof EntityNode) &&
            (nodePath.getNode().getSource() instanceof ClassInfoModel)) {
            var model = (ClassInfoModel) nodePath.getNode().getSource();
            entityDependencies.add(model.getName());
        }
        if (nodePath.getNode().getSource() instanceof NamedModel &&
            (nodePath.getParentPath().getNode() instanceof EndpointNode)) {
            var model = (NamedModel) nodePath.getNode().getSource();
            endpointDependencies.add(model.getName());
        }
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        super.setStorage(storage);
        var pluginStorage = storage.getPluginStorage();
        pluginStorage.put(DependencyPlugin.ENTITY_DEPS_STORAGE_KEY,
            entityDependencies);
        pluginStorage.put(DependencyPlugin.DEPS_MEMBERS_STORAGE_KEY,
            dependencyMembers);
        pluginStorage.put(DependencyPlugin.ENDPOINTS_DIRECT_DEPS_STORAGE_KEY,
            endpointDependencies);
    }
}
