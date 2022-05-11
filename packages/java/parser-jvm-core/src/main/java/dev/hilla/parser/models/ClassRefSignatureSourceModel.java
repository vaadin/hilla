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
    private ClassInfoModel reference;
    private List<TypeArgumentModel> typeArguments;

    public ClassRefSignatureSourceModel(ClassRefTypeSignature origin,
            Model parent) {
        super(origin, parent);
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
        if (reference == null) {
            var originInfo = origin.getClassInfo();

            reference = originInfo != null ? ClassInfoModel.of(originInfo)
                    : ClassInfoModel.of(origin.loadClass());
        }

        return reference;
    }

    @Override
    public void setReference(ClassInfoModel reference) {
        this.reference = reference;
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
}
