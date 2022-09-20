package dev.hilla.parser.plugins.backbone;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.plugins.backbone.nodes.EntityNode;
import dev.hilla.parser.plugins.backbone.nodes.FieldNode;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JSONPlugin extends AbstractPlugin<PluginConfiguration> {

    public static final String JSON_IGNORE = JsonIgnore.class.getName();

    public static final String JSON_IGNORE_PROPERTIES = JsonIgnoreProperties.class
            .getName();

    @Override
    @Nonnull
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (!(nodeDependencies.getNode() instanceof EntityNode)) {
            return nodeDependencies;
        }

        var cls = (ClassInfoModel) nodeDependencies.getNode().getSource();
        if (cls.isEnum()) {
            return nodeDependencies;
        }

        // Find fields annotated with JsonIgnore
        var ignoredByAnnotation = cls.getFieldsStream()
                .filter(f -> f.getAnnotations().stream()
                        .map(AnnotationInfoModel::getName)
                        .anyMatch(n -> n.equals(JSON_IGNORE)))
                .map(FieldInfoModel::getName);

        // Find the JsonIgnoreProperties and get list of ignored fields
        var ignoredByClassAnnotation = cls.getAnnotations().stream()
                .filter(a -> a.getName().equals(JSON_IGNORE_PROPERTIES))
                .flatMap(AnnotationInfoModel::getParametersStream)
                .flatMap(p -> Arrays.stream(((Object[]) p.getValue())))
                .map(Objects::toString);

        // Build the final list of field that must be ignored according to
        // Jackson annotations
        var ignored = Stream
                .concat(ignoredByAnnotation, ignoredByClassAnnotation)
                .collect(Collectors.toSet());

        // Filter out ignored fields and rebuild dependencies stream for the
        // node
        return nodeDependencies
                .withChildNodes(nodeDependencies.getChildNodes().filter(n -> {
                    if (n instanceof FieldNode) {
                        var fieldNode = (FieldNode) n;

                        if (ignored.contains(fieldNode.getSource().getName())) {
                            return false;
                        }
                    }

                    return true;
                }));
    }

    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }
}
