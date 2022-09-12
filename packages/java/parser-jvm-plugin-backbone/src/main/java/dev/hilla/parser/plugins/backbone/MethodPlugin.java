package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.plugins.backbone.nodes.EndpointNode;
import dev.hilla.parser.plugins.backbone.nodes.MethodNode;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.Node;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.RootNode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

public final class MethodPlugin extends AbstractPlugin<PluginConfiguration> {
    public static final String MEDIA_TYPE = "application/json";

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (nodeDependencies.getNode() instanceof EndpointNode) {
            var endpointCls = (ClassInfoModel) nodeDependencies.getNode()
                    .getSource();
            var methods = endpointCls.getMethodsStream()
                    .filter(MethodInfoModel::isPublic)
                    .<Node<?, ?>> map(MethodNode::of);
            return nodeDependencies.appendChildNodes(methods);
        }
        return nodeDependencies;
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        var node = nodePath.getNode();
        var parentNode = nodePath.getParentPath().getNode();
        if (node instanceof MethodNode && parentNode instanceof EndpointNode) {
            var methodNode = (MethodNode) node;
            var endpointNode = (EndpointNode) parentNode;
            methodNode.getTarget()
                    .post(createOperation(endpointNode, methodNode));
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        var node = nodePath.getNode();
        var parentNode = nodePath.getParentPath().getNode();
        var grandParentNode = nodePath.getParentPath().getParentPath()
                .getNode();
        if (node instanceof MethodNode && parentNode instanceof EndpointNode
                && grandParentNode instanceof RootNode) {
            var endpointName = ((EndpointNode) parentNode).getTarget()
                    .getName();
            var methodName = ((MethodNode) node).getSource().getName();
            ((RootNode) grandParentNode).getTarget().path(
                    String.format("/%s/%s", endpointName, methodName),
                    ((MethodNode) node).getTarget());
        }
    }

    private Operation createOperation(EndpointNode endpointNode,
            MethodNode methodNode) {
        var operation = new Operation();

        var endpointName = endpointNode.getTarget().getName();

        operation
                .operationId(String.format("%s_%s_POST", endpointName,
                        methodNode.getSource().getName()))
                .addTagsItem(endpointName).responses(createResponses());

        return operation;
    }

    private ApiResponses createResponses() {
        var response = new ApiResponse().description("");
        return new ApiResponses().addApiResponse("200", response);
    }
}
