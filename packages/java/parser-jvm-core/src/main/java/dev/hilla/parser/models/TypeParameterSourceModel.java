package dev.hilla.parser.models;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.utils.Streams;

import io.github.classgraph.TypeParameter;

final class TypeParameterSourceModel extends TypeParameterModel
        implements SourceSignatureModel {
    private final TypeParameter origin;

    TypeParameterSourceModel(TypeParameter origin) {
        this.origin = origin;
    }

    @Override
    public TypeParameter get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        var annotations = origin.getTypeAnnotationInfo();
        return annotations == null ? List.of()
                : annotations.stream().map(AnnotationInfoModel::of)
                        .collect(Collectors.toList());
    }

    @Override
    protected List<SignatureModel> prepareBounds() {
        return Streams
                .combine(Stream.of(getClassBoundSignature()),
                        origin.getInterfaceBounds().stream()
                                .map(SignatureModel::of))
                .distinct().collect(Collectors.toList());
    }

    @Nonnull
    private SignatureModel getClassBoundSignature() {
        // FIXME: param class bound is sometimes null and sometimes Object.
        // Possibly a bug in ClassGraph. Use Object to align with reflection.
        var classBound = origin.getClassBound();
        if (classBound == null) {
            return ClassRefSignatureModel.of(Object.class);
        }

        return SignatureModel.of(classBound);
    }
}
