package dev.hilla.parser.plugins.nonnull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.models.AnnotatedModel;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.PackageInfoModel;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import io.swagger.v3.oas.models.media.Schema;

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
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        if (!(nodePath.getNode().getTarget() instanceof Schema)) {
            return;
        }

        var schema = (Schema<?>) nodePath.getNode().getTarget();

        // Apply annotations from package (NonNullApi)
        var annotations = getPackageAnnotationsStream(nodePath);

        // Apply from current node, if source is annotated
        if (nodePath.getNode().getSource() instanceof AnnotatedModel) {
            annotations = Stream.concat(annotations,
                    ((AnnotatedModel) nodePath.getNode().getSource())
                            .getAnnotationsStream());
        }

        // When the parent source is annotated, but the parent target is not a
        // schema (consider MethodNode, MethodParameterNode, and FieldNode),
        // apply annotations from parent node to the current node’s target.
        var parentNode = nodePath.getParentPath().getNode();
        if ((parentNode.getSource() instanceof AnnotatedModel)
                && !(parentNode.getTarget() instanceof Schema)) {
            annotations = Stream.concat(annotations,
                    ((AnnotatedModel) parentNode.getSource())
                            .getAnnotationsStream());
        }

        annotations.map(annotation -> annotationsMap.get(annotation.getName()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(AnnotationMatcher::getScore))
                .map(AnnotationMatcher::doesMakeNullable).ifPresent(
                        nullable -> schema.setNullable(nullable ? true : null));
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

    private Optional<PackageInfoModel> findClosestPackage(
            NodePath<?> nodePath) {
        return nodePath.stream().map(NodePath::getNode)
                .filter(node -> node.getSource() instanceof ClassInfoModel)
                .map(node -> (ClassInfoModel) node.getSource()).findFirst()
                .map(ClassInfoModel::getPackage);
    }

    private Stream<AnnotationInfoModel> getPackageAnnotationsStream(
            NodePath<?> nodePath) {
        return findClosestPackage(nodePath).stream()
                .flatMap(PackageInfoModel::getAnnotationsStream);
    }

    static private Map<String, AnnotationMatcher> mapByName(
            Collection<AnnotationMatcher> annotations) {
        return annotations.stream().collect(Collectors
                .toMap(AnnotationMatcher::getName, Function.identity()));
    }
}
