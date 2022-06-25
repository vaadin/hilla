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
        extends ClassRefSignatureAbstractModel<T>
        implements ReflectionSignatureModel {
    ClassRefSignatureReflectionModel(T origin) {
        super(origin);
    }

    @Override
    public String getClassName() {
        return getOriginClassInfo().getName();
    }

    protected abstract Class<?> getOriginClassInfo();

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return AnnotationUtils.convert(origin.getAnnotations());
    }

    @Override
    protected ClassInfoModel prepareClassInfo() {
        return ClassInfoModel.of(getOriginClassInfo());
    }

    @Override
    protected List<TypeArgumentModel> prepareTypeArguments() {
        return List.of();
    }

    static final class AnnotatedBare extends Annotated<AnnotatedType> {
        AnnotatedBare(AnnotatedType origin, List<Annotation[]> annotations,
                int ownerIndex) {
            super(origin, annotations, ownerIndex);
        }

        @Override
        protected Class<?> getOriginClassInfo() {
            return (Class<?>) origin.getType();
        }
    }

    static final class Bare extends ClassRefSignatureReflectionModel<Class<?>> {
        Bare(Class<?> origin) {
            super(origin);
        }

        @Override
        public Optional<ClassRefSignatureModel> getOwner() {
            return Optional.empty();
        }

        @Override
        protected Class<?> getOriginClassInfo() {
            return origin;
        }
    }

    static final class Regular extends Annotated<AnnotatedParameterizedType> {
        private Regular(AnnotatedParameterizedType origin,
                List<Annotation[]> annotations, int ownerIndex) {
            super(origin, annotations, ownerIndex);
        }

        @Override
        protected Class<?> getOriginClassInfo() {
            return (Class<?>) ((ParameterizedType) origin.getType())
                    .getRawType();
        }

        @Override
        protected List<TypeArgumentModel> prepareTypeArguments() {
            return Arrays.stream(origin.getAnnotatedActualTypeArguments())
                    .map(TypeArgumentModel::of).collect(Collectors.toList());
        }
    }

    static abstract class Annotated<T extends AnnotatedType>
            extends ClassRefSignatureReflectionModel<T> {
        protected final List<Annotation[]> ownedAnnotations;
        protected final int ownerIndex;

        Annotated(T origin, List<Annotation[]> ownedAnnotations,
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
        public Optional<ClassRefSignatureModel> getOwner() {
            var owner = origin.getAnnotatedOwnerType();

            if (owner != null) {
                return Optional.of(
                        Annotated.of(owner, ownedAnnotations, ownerIndex + 1));
            }

            return Optional.empty();
        }

        @Override
        protected List<AnnotationInfoModel> prepareAnnotations() {
            return AnnotationUtils.convert(ownedAnnotations.get(ownerIndex));
        }
    }
}
