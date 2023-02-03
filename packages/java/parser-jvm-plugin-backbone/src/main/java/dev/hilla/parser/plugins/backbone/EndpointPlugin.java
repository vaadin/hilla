package dev.hilla.parser.plugins.backbone;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.RootNode;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.plugins.backbone.nodes.EndpointNode;

import io.swagger.v3.oas.models.tags.Tag;

public final class EndpointPlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private static void checkIfJavaCompilerParametersFlagIsEnabled(
            Collection<ClassInfoModel> endpoints) {
        endpoints.stream().map(ClassInfoModel::getMethods)
                .flatMap(Collection::stream).map(MethodInfoModel::getParameters)
                .flatMap(Collection::stream).findFirst()
                .ifPresent((parameter) -> {
                    if (parameter.getName() == null) {
                        logger.info("Missing endpoint method parameter names"
                                + " in JVM bytecode, probably because they were not enabled"
                                + " during compilation. For the Java compiler, set the"
                                + " \"parameters\" flag to true to enable them.");
                    }
                });
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof EndpointNode) {
            var endpointNode = (EndpointNode) nodePath.getNode();
            var name = endpointNode.getSource().getSimpleName();
            endpointNode.setTarget(new Tag().name(name));
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        var node = nodePath.getNode();
        var parentNode = nodePath.getParentPath().getNode();
        if (node instanceof EndpointNode && parentNode instanceof RootNode) {
            ((RootNode) parentNode).getTarget()
                    .addTagsItem(((EndpointNode) node).getTarget());
        }
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (nodeDependencies.getNode() instanceof RootNode) {
            var rootNode = (RootNode) nodeDependencies.getNode();
            var endpointAnnotationName = getStorage().getParserConfig()
                    .getEndpointAnnotationName();
            var endpoints = rootNode.getSource()
                    .getClassesWithAnnotation(endpointAnnotationName).stream()
                    .map(ClassInfoModel::of).collect(Collectors.toList());
            checkIfJavaCompilerParametersFlagIsEnabled(endpoints);
            return nodeDependencies.appendChildNodes(
                    endpoints.stream().filter(ClassInfoModel::isNonJDKClass)
                            .map(EndpointNode::of));
        }
        return nodeDependencies;
    }
}
