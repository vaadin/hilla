package dev.hilla.parser.models;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import dev.hilla.parser.utils.StreamUtils;

import io.github.classgraph.ClassRefTypeSignature;

final class ClassRefSignatureSourceModel
        extends AbstractModel<ClassRefTypeSignature>
        implements ClassRefSignatureModel, SourceSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private List<TypeArgumentModel> typeArguments;
    private ClassInfoModel resolved;

    public ClassRefSignatureSourceModel(ClassRefTypeSignature origin,
            Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = AnnotationUtils.processTypeAnnotations(origin.getTypeAnnotationInfo(), this);
        }

        return annotations;
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
