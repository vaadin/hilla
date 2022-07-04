package dev.hilla.parser.models;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.TypeArgument;

abstract class ClassRefSignatureSourceModel
        extends AbstractAnnotatedSourceModel<ClassRefTypeSignature>
        implements ClassRefSignatureModel, SourceSignatureModel {
    protected ClassInfoModel reference;
    private List<TypeArgumentModel> typeArguments;

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

        return getClassName().equals(other.getClassName())
                && getOwner().equals(other.getOwner())
                && getTypeArguments().equals(other.getTypeArguments())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public ClassInfoModel getClassInfo() {
        if (reference == null) {
            reference = origin.getBaseClassName().equals("java.lang.Object")
                    ? ClassInfoModel.of(Object.class)
                    : ClassInfoModel.of(getOriginClassInfo());
        }

        return reference;
    }

    @Override
    public String getClassName() {
        return origin.getBaseClassName();
    }

    @Override
    public Optional<ClassRefSignatureModel> getOwner() {
        return Optional.empty();
    }

    @Override
    public List<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            typeArguments = getOriginTypeArguments().stream()
                    .map(TypeArgumentModel::of).collect(Collectors.toList());
        }

        return typeArguments;
    }

    @Override
    public int hashCode() {
        return getClassName().hashCode() + 7 * getTypeArguments().hashCode()
                + 23 * getAnnotations().hashCode() + 53 * getOwner().hashCode();
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

    protected ClassInfo getOriginClassInfo() {
        return origin.getClassInfo();
    }

    protected List<TypeArgument> getOriginTypeArguments() {
        return origin.getTypeArguments();
    }

    static final class Regular extends ClassRefSignatureSourceModel {
        public Regular(ClassRefTypeSignature origin) {
            super(origin);
        }
    }

    static final class Suffixed extends ClassRefSignatureSourceModel {
        private final int currentSuffixIndex;

        public Suffixed(ClassRefTypeSignature origin) {
            this(origin, origin.getSuffixes().size() - 1);
        }

        public Suffixed(ClassRefTypeSignature origin, int currentSuffixIndex) {
            super(origin);
            this.currentSuffixIndex = currentSuffixIndex;
        }

        @Override
        public String getClassName() {
            var builder = new StringBuilder(origin.getBaseClassName());

            for (var i = 0; i <= currentSuffixIndex; i++) {
                builder.append('$');
                builder.append(origin.getSuffixes().get(i));
            }

            return builder.toString();
        }

        @Override
        public Optional<ClassRefSignatureModel> getOwner() {
            return currentSuffixIndex > 0
                    ? Optional.of(new Suffixed(origin, currentSuffixIndex - 1))
                    : Optional.of(new SuffixedBase(origin));
        }

        @Override
        protected Stream<AnnotationInfo> getOriginAnnotations() {
            var suffixAnnotations = origin.getSuffixTypeAnnotationInfo();

            return suffixAnnotations != null
                    ? origin.getSuffixTypeAnnotationInfo()
                            .get(currentSuffixIndex).stream()
                    : Stream.empty();
        }

        @Override
        protected ClassInfo getOriginClassInfo() {
            if (currentSuffixIndex == origin.getSuffixes().size() - 1) {
                return origin.getClassInfo();
            }

            var outerClasses = origin.getClassInfo().getOuterClasses();
            var currentSuffix = origin.getSuffixes().get(currentSuffixIndex);

            for (var cls : outerClasses) {
                if (cls.getName().endsWith(currentSuffix)) {
                    return cls;
                }
            }

            throw new NoSuchElementException();
        }

        @Override
        protected List<TypeArgument> getOriginTypeArguments() {
            return origin.getSuffixTypeArguments().get(currentSuffixIndex);
        }
    }

    static final class SuffixedBase extends ClassRefSignatureSourceModel {
        public SuffixedBase(ClassRefTypeSignature origin) {
            super(origin);
        }

        @Override
        protected ClassInfo getOriginClassInfo() {
            var outerClasses = origin.getClassInfo().getOuterClasses();
            return outerClasses.get(outerClasses.size() - 1);
        }
    }
}
