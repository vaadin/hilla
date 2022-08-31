package dev.hilla.parser.plugins.nonnull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.MethodParameterInfoModel;
import dev.hilla.parser.models.PackageInfoModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.plugins.backbone.AssociationMap;
import dev.hilla.parser.utils.Lists;
import dev.hilla.parser.utils.Streams;

final class NonnullVisitor implements Visitor {
    private final Map<String, AnnotationMatcher> annotations;
    private final AssociationMap associationMap;
    private final int order;

    NonnullVisitor(Collection<AnnotationMatcher> annotations,
            AssociationMap associationMap, int order) {
        this.annotations = annotations.stream().collect(Collectors
                .toMap(AnnotationMatcher::getName, Function.identity()));
        this.associationMap = associationMap;
        this.order = order;
    }

    @Override
    public void enter(NodePath path) {
        if (path.isSkipped()) {
            return;
        }

        var model = path.getModel();

        if (model instanceof SignatureModel
                && associationMap.getSignatures().containsKey(model)) {
            var schema = associationMap.getSignatures().get(model);

            var matcher = Streams
                    .combine(
                            ((SignatureModel) path.getModel())
                                    .getAnnotationsStream(),
                            getParentAnnotationStream(path),
                            getPackageAnnotationStream(path))
                    .map(annotation -> annotations.get(annotation.getName()))
                    .filter(Objects::nonNull)
                    .max(Comparator.comparingInt(AnnotationMatcher::getScore))
                    .orElse(AnnotationMatcher.DEFAULT);

            schema.setNullable(matcher.doesMakeNonNull() ? null : true);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    private Stream<AnnotationInfoModel> getPackageAnnotationStream(
            NodePath path) {
        var packageModel = (PackageInfoModel) Lists.getLastElement(path.getAscendants())
                .getModel();
        return packageModel.getAnnotationsStream();
    }

    /**
     * If the signature is not nested, it has an effect of parent model
     * annotations.
     *
     * @param path
     *            signature model path to get annotations of the parent
     * @return a stream of parent annotations
     */
    private Stream<AnnotationInfoModel> getParentAnnotationStream(
            NodePath path) {
        var parent = path.getParent().getModel();

        if (parent instanceof FieldInfoModel) {
            return ((FieldInfoModel) parent).getAnnotationsStream();
        } else if (parent instanceof MethodInfoModel) {
            return ((MethodInfoModel) parent).getAnnotationsStream();
        } else if (parent instanceof MethodParameterInfoModel) {
            return ((MethodParameterInfoModel) parent).getAnnotationsStream();
        }

        return Stream.empty();
    }
}
