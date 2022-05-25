package dev.hilla.parser.models;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassRefTypeSignature;

class ClassRefSignatureSourceModel
        extends AbstractAnnotatedSourceModel<ClassRefTypeSignature>
        implements ClassRefSignatureModel, SourceSignatureModel {
    protected List<TypeArgumentModel> typeArguments;
    private ClassInfoModel reference;

    public ClassRefSignatureSourceModel(ClassRefTypeSignature origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ClassRefSignatureModel)) {
            return false;
        }

        var other = (ClassRefSignatureModel) obj;

        return origin.getFullyQualifiedClassName().equals(other.getClassName())
                && getTypeArguments().equals(other.getTypeArguments())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public String getClassName() {
        return origin.getFullyQualifiedClassName();
    }

    @Override
    public Optional<ClassRefSignatureModel> getOwner() {
        return Optional.empty();
    }

    @Override
    public List<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            typeArguments = origin.getTypeArguments().stream()
                    .map(TypeArgumentModel::of).collect(Collectors.toList());
        }

        return typeArguments;
    }

    @Override
    public int hashCode() {
        return origin.getFullyQualifiedClassName().hashCode()
                + 7 * getTypeArguments().hashCode()
                + 13 * getAnnotations().hashCode();
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
        var annotations = origin.getTypeAnnotationInfo();

        return annotations != null ? annotations.stream() : Stream.empty();
    }

    final static class Suffixed extends ClassRefSignatureSourceModel {
        private final int currentSuffixIndex;

        public Suffixed(ClassRefTypeSignature origin) {
            this(origin, origin.getSuffixes().size() - 1);
        }

        public Suffixed(ClassRefTypeSignature origin, int currentSuffixIndex) {
            super(origin);
            this.currentSuffixIndex = currentSuffixIndex;
        }

        @Override
        public Optional<ClassRefSignatureModel> getOwner() {
            return currentSuffixIndex > 0
                    ? Optional.of(new Suffixed(origin, currentSuffixIndex - 1))
                    : Optional.empty();
        }

        @Override
        public List<TypeArgumentModel> getTypeArguments() {
            if (typeArguments == null) {
                typeArguments = origin.getSuffixTypeArguments()
                        .get(currentSuffixIndex).stream()
                        .map(TypeArgumentModel::of)
                        .collect(Collectors.toList());
            }

            return typeArguments;
        }

        @Override
        protected Stream<AnnotationInfo> getOriginAnnotations() {
            var annotations = origin.getSuffixTypeAnnotationInfo()
                    .get(currentSuffixIndex);

            return annotations != null ? annotations.stream() : Stream.empty();
        }
    }
}
