package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;
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
import dev.hilla.parser.plugins.backbone.nodes.EndpointSignatureNode;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EndpointExposedPlugin
        extends AbstractPlugin<PluginConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
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
            // Scan the referenced class from endpoint marked type signature
            var classRef = ((ClassRefSignatureModel) node.getSource());

            // Create class info node depending on presence of
            // @EndpointExposed annotation.
            var endpointExposedAnnotationName = getStorage().getParserConfig()
                    .getEndpointExposedAnnotationName();
            var exposed = classRef.getClassInfo().getAnnotationsStream()
                    .map(AnnotationInfoModel::getName)
                    .anyMatch(endpointExposedAnnotationName::equals);
            var classInfoNode = exposed
                    ? EndpointExposedNode.of(classRef.getClassInfo())
                    : EndpointNonExposedNode.of(classRef.getClassInfo());

            // Attach class info node even if not exposed, so that it is
            // scanned to reveal referenced exposed classes, if any
            return nodeDependencies.appendChildNodes(Stream.of(classInfoNode));
        }

        return nodeDependencies;
    }

    private Stream<Node<?, ?>> scanEndpointClassSignature(
            ClassInfoModel endpointClass) {
        return Stream
                .concat(endpointClass.getSuperClass().stream(),
                        endpointClass.getInterfacesStream())
                .map(EndpointSignatureNode::of);
    }
}
