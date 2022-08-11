package dev.hilla.parser.plugins.nonnull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.core.Path;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.MethodParameterInfoModel;
import dev.hilla.parser.models.PackageInfoModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.plugins.backbone.AssociationMap;
import dev.hilla.parser.utils.Streams;

final class NonnullVisitor implements Visitor {
    private final Map<String, AnnotationMatcher> annotations;
    private final AssociationMap associationMap;
    private final Supplier<Integer> orderProvider;
    private final int shift;

    NonnullVisitor(Collection<AnnotationMatcher> annotations,
            AssociationMap associationMap, Supplier<Integer> orderProvider,
            int shift) {
        this.annotations = annotations.stream().collect(Collectors
                .toMap(AnnotationMatcher::getName, Function.identity()));
        this.associationMap = associationMap;
        this.orderProvider = orderProvider;
        this.shift = shift;
    }

    @Override
    public void enter(Path<?> path) {
        if (path.isRemoved()) {
            return;
        }

        var model = path.getModel();

        if (model instanceof SignatureModel && associationMap.getSignatures().containsKey(model)) {
            var schema = associationMap.getSignatures().get(model);

            var matcher = Streams.combine(
                    ((SignatureModel) path.getModel()).getAnnotationsStream(),
                    getParentAnnotationStream((Path<SignatureModel>) path),
                    getPackageAnnotationStream((Path<SignatureModel>) path))
                .map(annotation -> annotations.get(annotation.getName()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(AnnotationMatcher::getScore))
                .orElse(AnnotationMatcher.DEFAULT);

            schema.setNullable(matcher.doesMakeNonNull() ? null : true);
        }
    }

    @Override
    public int getOrder() {
        return orderProvider.get() + shift;
    }

    private Stream<AnnotationInfoModel> getPackageAnnotationStream(
            Path<SignatureModel> path) {
        return ((PackageInfoModel) path.getAscendants().get(0).getModel())
                .getAnnotationsStream();
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
            Path<SignatureModel> path) {
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
