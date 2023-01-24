package dev.hilla.parser.plugins.backbone;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.AnnotationParameterModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.plugins.backbone.nodes.EntityNode;
import dev.hilla.parser.plugins.backbone.nodes.FieldNode;

public class JSONPlugin extends AbstractPlugin<PluginConfiguration> {
    private static final String JSON_IGNORE = JsonIgnore.class.getName();
    private static final String JSON_IGNORE_PROPERTIES = JsonIgnoreProperties.class
            .getName();
    private static final String JSON_IGNORE_TYPE = JsonIgnoreType.class
            .getName();

    private static boolean isFieldIgnored(FieldInfoModel field) {
        return field.getAnnotations().stream().map(AnnotationInfoModel::getName)
                .anyMatch(n -> n.equals(JSON_IGNORE));
    }

    private static boolean isFieldTypeIgnored(FieldInfoModel field) {
        SignatureModel type = field.getType();

        if (!(type instanceof ClassRefSignatureModel)) {
            return false;
        }

        return ((ClassRefSignatureModel) type).getClassInfo().getAnnotations()
                .stream().map(AnnotationInfoModel::getName)
                .anyMatch(n -> n.equals(JSON_IGNORE_TYPE));
    }

    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

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

        // Find fields annotated with JsonIgnore or whose type is annotated with
        // JsonIgnoreType
        var ignoredByAnnotation = cls.getFieldsStream().filter(
                field -> isFieldIgnored(field) || isFieldTypeIgnored(field))
                .map(FieldInfoModel::getName);

        // Find the JsonIgnoreProperties and get list of ignored fields
        var ignoredByClassAnnotation = cls.getAnnotations().stream()
                .filter(a -> a.getName().equals(JSON_IGNORE_PROPERTIES))
                .flatMap(AnnotationInfoModel::getParametersStream)
                .filter(Predicate.not(AnnotationParameterModel::isDefault))
                .flatMap(p -> Arrays.stream(((Object[]) p.getValue())))
                .map(Objects::toString);

        // Build the final list of field that must be ignored according to
        // Jackson annotations
        var ignored = Stream
                .concat(ignoredByAnnotation, ignoredByClassAnnotation)
                .collect(Collectors.toSet());

        // Filter out ignored fields
        return nodeDependencies
                .processChildNodes(nodeStream -> nodeStream.filter(n -> {
                    if (n instanceof FieldNode) {
                        var fieldNode = (FieldNode) n;

                        if (ignored.contains(fieldNode.getSource().getName())) {
                            return false;
                        }
                    }

                    return true;
                }));
    }
}
