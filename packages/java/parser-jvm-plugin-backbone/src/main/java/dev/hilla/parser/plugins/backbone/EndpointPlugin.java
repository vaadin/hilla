package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;

import java.util.Collection;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.plugins.backbone.nodes.EndpointNode;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.RootNode;
import io.github.classgraph.ClassInfo;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EndpointPlugin extends AbstractPlugin<PluginConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (nodeDependencies.getNode() instanceof RootNode) {
            var rootNode = (RootNode) nodeDependencies.getNode();
            var endpointAnnotationName = getStorage().getParserConfig()
                    .getEndpointAnnotationName();
            var endpoints = rootNode.getSource()
                    .getClassesWithAnnotation(endpointAnnotationName);
            checkIfJavaCompilerParametersFlagIsEnabled(endpoints);
            return nodeDependencies
                    .appendChildNodes(endpoints.stream().map(ClassInfoModel::of)
                            .filter(ClassInfoModel::isNonJDKClass)
                            .map(EndpointNode::of));
        }
        return nodeDependencies;
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

    private static void checkIfJavaCompilerParametersFlagIsEnabled(
            Collection<ClassInfo> endpoints) {
        endpoints.stream().map(ClassInfoModel::of)
                .flatMap(ClassInfoModel::getMethodsStream)
                .flatMap(MethodInfoModel::getParametersStream).findFirst()
                .ifPresent((parameter) -> {
                    if (parameter.getName() == null) {
                        logger.info("Missing endpoint method parameter names"
                                + " in JVM bytecode, probably because they were not enabled"
                                + " during compilation. For the Java compiler, set the"
                                + " \"parameters\" flag to true to enable them.");
                    }
                });
    }
}
