package dev.hilla.parser.plugins.nonnull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;

public final class NonnullPlugin extends AbstractPlugin<NonnullPluginConfig> {
    private Map<String, AnnotationMatcher> annotationsMap = mapByName(
            NonnullPluginConfig.Processor.defaults);

    public NonnullPlugin() {
        super();
        setOrder(100);
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (!(nodePath.getNode() instanceof TypeSignatureNode)) {
            return;
        }

        var typeSignatureNode = (TypeSignatureNode) nodePath.getNode();
        var schema = typeSignatureNode.getTarget();
        var matcher = Stream
                .concat(nodePath.getAnnotations(),
                        nodePath.getPackageAnnotations())
                .map(annotation -> annotationsMap.get(annotation.getName()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(AnnotationMatcher::getScore))
                .orElse(AnnotationMatcher.DEFAULT);

        schema.setNullable(matcher.doesMakeNonNull() ? null : true);
    }

    @Override
    public void exit(NodePath<?> nodePath) {

    }

    @Override
    public void setConfiguration(@Nonnull PluginConfiguration configuration) {
        super.setConfiguration(configuration);
        this.annotationsMap = mapByName(
                new NonnullPluginConfig.Processor(getConfiguration())
                        .process());
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    static private Map<String, AnnotationMatcher> mapByName(
            Collection<AnnotationMatcher> annotations) {
        return annotations.stream().collect(Collectors
                .toMap(AnnotationMatcher::getName, Function.identity()));
    }
}
