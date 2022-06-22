package dev.hilla.parser.models;

import static dev.hilla.parser.utils.AnnotatedOwnerUtils.getAllOwnersAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class ClassRefSignatureReflectionModel<T extends AnnotatedElement>
        extends AbstractAnnotatedReflectionModel<T>
        implements ClassRefSignatureModel, ReflectionSignatureModel {
    private ClassInfoModel reference;

    public ClassRefSignatureReflectionModel(T origin) {
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
    public String getClassName() {
        return getOriginClassInfo().getName();
    }

    @Override
    public int hashCode() {
        return getClassName().hashCode() + 7 * getTypeArguments().hashCode()
                + 23 * getAnnotations().hashCode() + 53 * getOwner().hashCode();
    }

    @Override
    public ClassInfoModel resolve() {
        if (reference == null) {
            reference = ClassInfoModel.of(getOriginClassInfo());
        }

        return reference;
    }

    @Override
    public void setReference(ClassInfoModel reference) {
        this.reference = reference;
    }

    protected abstract Class<?> getOriginClassInfo();

    static final class AnnotatedBare extends Annotated<AnnotatedType> {
        public AnnotatedBare(AnnotatedType origin,
                List<Annotation[]> annotations, int ownerIndex) {
            super(origin, annotations, ownerIndex);
        }

        @Override
        public List<TypeArgumentModel> getTypeArguments() {
            return List.of();
        }

        @Override
        protected Class<?> getOriginClassInfo() {
            return (Class<?>) origin.getType();
        }
    }

    static final class Bare extends ClassRefSignatureReflectionModel<Class<?>> {
        public Bare(Class<?> origin) {
            super(origin);
        }

        @Override
        public Optional<ClassRefSignatureModel> getOwner() {
            return Optional.empty();
        }

        @Override
        public List<TypeArgumentModel> getTypeArguments() {
            return List.of();
        }

        @Override
        protected Class<?> getOriginClassInfo() {
            return origin;
        }
    }

    static final class Regular extends Annotated<AnnotatedParameterizedType> {
        private List<TypeArgumentModel> typeArguments;

        private Regular(AnnotatedParameterizedType origin,
                List<Annotation[]> annotations, int ownerIndex) {
            super(origin, annotations, ownerIndex);
        }

        @Override
        public List<TypeArgumentModel> getTypeArguments() {
            if (typeArguments == null) {
                typeArguments = Arrays
                        .stream(origin.getAnnotatedActualTypeArguments())
                        .map(TypeArgumentModel::of)
                        .collect(Collectors.toList());
            }

            return typeArguments;
        }

        @Override
        protected Class<?> getOriginClassInfo() {
            return (Class<?>) ((ParameterizedType) origin.getType())
                    .getRawType();
        }
    }

    static abstract class Annotated<T extends AnnotatedType>
            extends ClassRefSignatureReflectionModel<T> {
        protected final List<Annotation[]> ownedAnnotations;
        protected final int ownerIndex;

        public Annotated(T origin, List<Annotation[]> ownedAnnotations,
                int ownerIndex) {
            super(origin);
            this.ownedAnnotations = ownedAnnotations;
            this.ownerIndex = ownerIndex;
        }

        public static Annotated<?> of(AnnotatedType origin) {
            return of(origin, getAllOwnersAnnotations(origin), 0);
        }

        private static Annotated<?> of(AnnotatedType origin,
                List<Annotation[]> annotations, int ownerIndex) {
            return origin instanceof AnnotatedParameterizedType
                    ? new Regular((AnnotatedParameterizedType) origin,
                            annotations, ownerIndex)
                    : new AnnotatedBare(origin, annotations, ownerIndex);
        }

        @Override
        public List<AnnotationInfoModel> getAnnotations() {
            if (annotations == null) {
                annotations = Arrays.stream(ownedAnnotations.get(ownerIndex))
                        .map(AnnotationInfoModel::of)
                        .collect(Collectors.toList());
            }

            return annotations;
        }

        @Override
        public Optional<ClassRefSignatureModel> getOwner() {
            var owner = origin.getAnnotatedOwnerType();

            if (owner != null) {
                return Optional.of(
                        Annotated.of(owner, ownedAnnotations, ownerIndex + 1));
            }

            return Optional.empty();
        }
    }
}
