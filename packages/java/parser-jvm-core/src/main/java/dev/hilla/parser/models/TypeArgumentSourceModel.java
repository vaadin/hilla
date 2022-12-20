package dev.hilla.parser.models;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationInfoList;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.HierarchicalTypeSignature;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeSignature;

final class TypeArgumentSourceModel extends TypeArgumentModel
        implements SourceSignatureModel {
    private final TypeArgument origin;

    TypeArgumentSourceModel(TypeArgument origin) {
        this.origin = origin;
    }

    @Override
    public TypeArgument get() {
        return origin;
    }

    @Override
    public TypeArgument.Wildcard getWildcard() {
        return origin.getWildcard();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return Stream.concat(
                getAssociatedTypes().stream()
                        .flatMap(SignatureModel::getAnnotationsStream),
                getWildcardAnnotationsStream()).collect(Collectors.toList());
    }

    @Override
    protected List<SignatureModel> prepareAssociatedTypes() {
        var signature = origin.getTypeSignature();

        return signature == null ? List.of()
                : List.of(SignatureModel.of(signature));
    }

    private Stream<AnnotationInfoModel> getWildcardAnnotationsStream() {
        // FIXME: workaround for
        // https://github.com/classgraph/classgraph/issues/741,
        // remove when the issue is fixed.
        if (getWildcard().equals(TypeArgument.Wildcard.NONE)) {
            return Stream.empty();
        }
        var strings = List.of(origin.toString().split(" "));
        strings = strings.subList(0, strings.indexOf("?"));
        return AnnotationInfoModel
                .parseStringsStream(strings.stream());
    }
}
