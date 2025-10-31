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

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.AnnotatedModel;
import com.vaadin.hilla.parser.models.AnnotationInfoModel;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
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

                computeNullabilityFromAnnotations(annotations).ifPresent(
                        nullable -> schema.setNullable(nullable ? true : null));

                // For type arguments, it is necessary to apply the same
                // processing
                if (nodeSource instanceof ClassRefSignatureModel) {
                    var args = ((ClassRefSignatureModel) nodeSource)
                            .getTypeArguments();

                    if (!args.isEmpty()) {
                        Optional.ofNullable(schema.getExtensions())
                                .map(ext -> ext.get("x-type-arguments"))
                                .map(ext -> (Schema<?>) ext)
                                .map(Schema::getAllOf).ifPresent(schemas -> {
                                    if (schemas.size() != args.size()) {
                                        throw new IllegalStateException(
                                                "Number of parameters mismatch for "
                                                        + nodePath);
                                    }

                                    var nullables = args.stream()
                                            .map(param -> Stream.concat(
                                                    getPackageAnnotationsStream(
                                                            nodePath),
                                                    param.getAnnotations()
                                                            .stream()))
                                            .map(this::computeNullabilityFromAnnotations)
                                            .toList();

                                    for (var i = 0; i < nullables.size(); i++) {
                                        var sch = schemas.get(i);
                                        nullables.get(i).ifPresent(
                                                nullable -> sch.setNullable(
                                                        nullable ? true
                                                                : null));
                                    }
                                });
                    }
                }
            }
        }
    }

    private Optional<Boolean> computeNullabilityFromAnnotations(
            Stream<AnnotationInfoModel> annotations) {
        return annotations
                .map(annotation -> annotationsMap.get(annotation.getName()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(AnnotationMatcher::getScore))
                .map(AnnotationMatcher::doesMakeNullable);
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    @Override
    public void setConfiguration(@NonNull PluginConfiguration configuration) {
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
