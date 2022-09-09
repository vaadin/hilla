package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.node.MethodNode;
import dev.hilla.parser.node.MethodParameterNode;
import dev.hilla.parser.node.Node;
import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.NodePath;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;

public class MethodParameterPlugin extends AbstractPlugin<PluginConfiguration> {

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (nodeDependencies.getNode() instanceof MethodNode) {
            var methodNode = (MethodNode) nodeDependencies.getNode();
            return NodeDependencies.of(methodNode,
                getParametersStream(methodNode), Stream.empty());
        }
        return nodeDependencies;
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        var node = nodePath.getNode();
        var parentNode = nodePath.getParentPath().getNode();
        if (node instanceof MethodParameterNode &&
            parentNode instanceof MethodNode) {
            var pathItem = (PathItem) parentNode.getTarget();
            if (pathItem.getPost().getRequestBody() == null) {
                pathItem.getPost().setRequestBody(createRequestBody());
            }
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    private Stream<Node<?, ?>> getParametersStream(MethodNode methodNode) {
        var parameters = new ArrayList<>(
            methodNode.getSource().getParameters());
        var parameterNodes = new ArrayList<Node<?, ?>>(parameters.size());
        for (var i = 0; i < parameters.size(); i++) {
            var parameter = parameters.get(i);
            var name = Optional.ofNullable(parameters.get(i).getName())
                .orElse(String.format("_param_%d", i));
            parameterNodes.add(i, MethodParameterNode.of(parameter, name));
        }
        return parameterNodes.stream().sequential();
    }

    private RequestBody createRequestBody() {
        var requestMap = new ObjectSchema();
        return new RequestBody().content(new Content().addMediaType(
            MethodPlugin.MEDIA_TYPE,
            new MediaType().schema(requestMap)));
    }

}
