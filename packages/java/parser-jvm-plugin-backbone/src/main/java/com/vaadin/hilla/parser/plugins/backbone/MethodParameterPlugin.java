package com.vaadin.hilla.parser.plugins.backbone;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;

public final class MethodParameterPlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {

    @Override
    public void enter(NodePath<?> nodePath) {
        var node = nodePath.getNode();
        var parentNode = nodePath.getParentPath().getNode();
        if (node instanceof MethodParameterNode
                && parentNode instanceof MethodNode) {
            var pathItem = (PathItem) parentNode.getTarget();
            if (pathItem.getPost().getRequestBody() == null) {
                pathItem.getPost().setRequestBody(createRequestBody());
            }
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (nodeDependencies.getNode() instanceof MethodNode) {
            var methodNode = (MethodNode) nodeDependencies.getNode();
            return nodeDependencies
                    .appendChildNodes(getParametersStream(methodNode));
        }
        return nodeDependencies;
    }

    private RequestBody createRequestBody() {
        var requestMap = new ObjectSchema();
        return new RequestBody().content(new Content().addMediaType(
                MethodPlugin.MEDIA_TYPE, new MediaType().schema(requestMap)));
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
        return parameterNodes.stream();
    }

}
