package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.stream.Stream;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.Node;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.plugins.backbone.nodes.EndpointExposedNode;
import dev.hilla.parser.plugins.backbone.nodes.EndpointNode;
import dev.hilla.parser.plugins.backbone.nodes.EndpointNonExposedNode;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EndpointExposedPlugin extends AbstractPlugin<PluginConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (nodeDependencies.getNode() instanceof EndpointNode ||
            nodeDependencies.getNode() instanceof EndpointExposedNode ||
            nodeDependencies.getNode() instanceof EndpointNonExposedNode) {
            var cls = (ClassInfoModel) nodeDependencies.getNode().getSource();
            return nodeDependencies.appendChildNodes(scanEndpointClass(cls));
        }

        return nodeDependencies;
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof EndpointExposedNode) {
            var endpointExposedNode = (EndpointExposedNode) nodePath.getNode();
            var name = endpointExposedNode.getSource().getSimpleName();
            endpointExposedNode.setTarget(new Tag().name(name));
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    private Node<?, ?> createEndpointClassChildNode(ClassInfoModel childClass) {
        var endpointExposedAnnotationName = getStorage().getParserConfig()
            .getEndpointExposedAnnotationName();
        return childClass.getAnnotationsStream().map(AnnotationInfoModel::getName)
            .anyMatch(Predicate.isEqual(endpointExposedAnnotationName))
            ? EndpointExposedNode.of(childClass)
            : EndpointNonExposedNode.of(childClass);
    }

    private Stream<Node<?, ?>> scanEndpointClass(ClassInfoModel endpointClass) {
        return Stream.concat(endpointClass.getSuperClass().stream(),
                endpointClass.getInterfacesStream())
            .map(ClassRefSignatureModel::getClassInfo)
            .map(this::createEndpointClassChildNode);
    }
}
