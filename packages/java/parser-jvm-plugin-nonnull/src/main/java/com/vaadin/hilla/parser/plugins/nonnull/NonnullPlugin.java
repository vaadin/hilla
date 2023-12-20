package com.vaadin.hilla.parser.plugins.nonnull;

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

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.AnnotatedModel;
import com.vaadin.hilla.parser.models.AnnotationInfoModel;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.PackageInfoModel;
import com.vaadin.hilla.parser.models.SpecializedModel;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.nodes.AnnotatedNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;

import io.swagger.v3.oas.models.media.Schema;

public final class NonnullPlugin extends AbstractPlugin<NonnullPluginConfig> {
    private Map<String, AnnotationMatcher> annotationsMap = mapByName(
            NonnullPluginConfig.Processor.defaults);

    public NonnullPlugin() {
        super();
    }

    static private Map<String, AnnotationMatcher> mapByName(
            Collection<AnnotationMatcher> annotations) {
        return annotations.stream().collect(Collectors
                .toMap(AnnotationMatcher::getName, Function.identity()));
    }

    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        var node = nodePath.getNode();

        if (node.getTarget() instanceof Schema) {
            var schema = (Schema<?>) node.getTarget();
            var nodeSource = node.getSource();

            if ((nodeSource instanceof SpecializedModel)
                    && ((SpecializedModel) nodeSource).isOptional()) {
                // Optional is always nullable, regardless of annotations
                schema.setNullable(true);
            } else {
                // Apply annotations from package (NonNullApi)
                var annotations = getPackageAnnotationsStream(nodePath);

                // Apply from current node, if it is annotated
                if (node instanceof AnnotatedNode) {
                    annotations = Stream.concat(annotations,
                            ((AnnotatedNode) node).getAnnotations().stream());
                }

                annotations = considerAscendantAnnotations(annotations,
                        nodePath);

                annotations
                        .map(annotation -> annotationsMap
                                .get(annotation.getName()))
                        .filter(Objects::nonNull)
                        .max(Comparator
                                .comparingInt(AnnotationMatcher::getScore))
                        .map(AnnotationMatcher::doesMakeNullable)
                        .ifPresent(nullable -> schema
                                .setNullable(nullable ? true : null));
            }
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    @Override
    public void setConfiguration(@Nonnull PluginConfiguration configuration) {
        super.setConfiguration(configuration);
        this.annotationsMap = mapByName(
                new NonnullPluginConfig.Processor(getConfiguration())
                        .process());
    }

    private Optional<ClassInfoModel> findClosestClass(NodePath<?> nodePath) {
        return nodePath.stream().map(NodePath::getNode)
                .filter(node -> node.getSource() instanceof ClassInfoModel)
                .map(node -> (ClassInfoModel) node.getSource()).findFirst();
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
                .map(PackageInfoModel::getAnnotations)
                .flatMap(Collection::stream);
    }

    /**
     * Adds ascendant annotations for check in case the type is annotated on
     * method/parameter/property level.
     *
     * @param annotations
     *            initial type annotations
     * @param nodePath
     *            the node path
     * @return stream of all annotations to check
     */
    private Stream<AnnotationInfoModel> considerAscendantAnnotations(
            Stream<AnnotationInfoModel> annotations, NodePath<?> nodePath) {
        var current = nodePath.getNode();
        var parent = nodePath.getParentPath().getNode();

        if (current instanceof TypedNode) {
            if (parent instanceof MethodNode
                    || parent instanceof MethodParameterNode
                    || parent instanceof PropertyNode) {
                annotations = Stream.concat(annotations,
                        ((AnnotatedModel) parent.getSource()).getAnnotations()
                                .stream());
            }
        }

        return annotations;
    }
}
