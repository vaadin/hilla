package dev.hilla.parser.models;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.TypeArgument;

abstract class ClassRefSignatureSourceModel extends ClassRefSignatureModel
        implements SourceSignatureModel {
    protected final ClassRefTypeSignature origin;

    ClassRefSignatureSourceModel(ClassRefTypeSignature origin) {
        this.origin = origin;
    }

    @Override
    public ClassRefTypeSignature get() {
        return origin;
    }

    @Override
    public String getClassName() {
        return origin.getBaseClassName();
    }

    @Override
    public Optional<ClassRefSignatureModel> getOwner() {
        return Optional.empty();
    }

    protected List<AnnotationInfo> getOriginAnnotations() {
        return origin.getTypeAnnotationInfo();
    }

    protected ClassInfo getOriginClassInfo() {
        return origin.getClassInfo();
    }

    protected List<TypeArgument> getOriginTypeArguments() {
        return origin.getTypeArguments();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(getOriginAnnotations());
    }

    @Override
    protected ClassInfoModel prepareClassInfo() {
        return origin.getBaseClassName().equals("java.lang.Object")
                ? ClassInfoModel.of(Object.class)
                : ClassInfoModel.of(getOriginClassInfo());
    }

    @Override
    protected List<TypeArgumentModel> prepareTypeArguments() {
        return getOriginTypeArguments().stream().map(TypeArgumentModel::of)
                .collect(Collectors.toList());
    }

    static final class Regular extends ClassRefSignatureSourceModel {
        public Regular(ClassRefTypeSignature origin) {
            super(origin);
        }
    }

    static final class Suffixed extends ClassRefSignatureSourceModel {
        private final int currentSuffixIndex;

        Suffixed(ClassRefTypeSignature origin) {
            this(origin, origin.getSuffixes().size() - 1);
        }

        Suffixed(ClassRefTypeSignature origin, int currentSuffixIndex) {
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
        protected List<AnnotationInfo> getOriginAnnotations() {
            var suffixAnnotations = origin.getSuffixTypeAnnotationInfo();

            return suffixAnnotations != null
                    ? origin.getSuffixTypeAnnotationInfo()
                            .get(currentSuffixIndex)
                    : null;
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
        SuffixedBase(ClassRefTypeSignature origin) {
            super(origin);
        }

        @Override
        protected ClassInfo getOriginClassInfo() {
            var outerClasses = origin.getClassInfo().getOuterClasses();
            return outerClasses.get(outerClasses.size() - 1);
        }
    }
}
