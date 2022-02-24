package dev.hilla.parser.models;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.utils.StreamUtils;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationInfoList;
import io.github.classgraph.ClassRefTypeSignature;

final class ClassRefSignatureSourceModel
        extends AbstractAnnotatedSourceModel<ClassRefTypeSignature>
        implements ClassRefSignatureModel, SourceSignatureModel {
    private List<TypeArgumentModel> typeArguments;
    private ClassInfoModel resolved;

    public ClassRefSignatureSourceModel(ClassRefTypeSignature origin,
            Model parent) {
        super(origin, parent);
    }

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        List<AnnotationInfo> typeAnnotationInfo = origin
                .getTypeAnnotationInfo() != null
                        ? origin.getTypeAnnotationInfo()
                        : Collections.emptyList();
        List<AnnotationInfoList> suffixTypeAnnotations = origin
                .getSuffixTypeAnnotationInfo() != null
                        ? origin.getSuffixTypeAnnotationInfo()
                        : Collections.emptyList();

        return StreamUtils.combine(typeAnnotationInfo.stream(),
                suffixTypeAnnotations.stream().flatMap(Collection::stream));
    }

    @Override
    public List<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            typeArguments = StreamUtils
                    .combine(origin.getTypeArguments().stream(),
                            origin.getSuffixTypeArguments().stream()
                                    .flatMap(Collection::stream))
                    .map(arg -> TypeArgumentModel.of(arg, this))
                    .collect(Collectors.toList());
        }

        return typeArguments;
    }

    @Override
    public ClassInfoModel resolve() {
        if (resolved == null) {
            var originInfo = origin.getClassInfo();

            resolved = originInfo != null ? ClassInfoModel.of(originInfo)
                    : ClassInfoModel.of(origin.loadClass());
        }

        return resolved;
    }
}
