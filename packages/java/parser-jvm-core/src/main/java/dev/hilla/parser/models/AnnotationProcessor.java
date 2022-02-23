package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationInfoList;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeSignature;

abstract class AnnotationProcessor<T> {
    protected final Model parent;
    private final List<T> annotations = new ArrayList<>();

    protected AnnotationProcessor(Model parent) {
        this.parent = parent;
    }

    public final List<AnnotationInfoModel> process() {
        return annotations.stream().map(this::process)
                .collect(Collectors.toList());
    }

    public AnnotationProcessor<T> add(T annotation) {
        if (annotation != null) {
            annotations.add(annotation);
        }

        return this;
    }

    public AnnotationProcessor<T> add(Stream<T> annotations) {
        if (annotations != null) {
            annotations.filter(Objects::nonNull)
                    .collect(Collectors.toCollection(() -> this.annotations));
        }
        return this;
    }

    public AnnotationProcessor<T> add(T[] annotations) {
        if (annotations != null) {
            add(Arrays.stream(annotations));
        }
        return this;
    }

    public AnnotationProcessor<T> add(Collection<T> annotations) {
        if (annotations != null) {
            add(annotations.stream());
        }

        return this;
    }

    protected abstract AnnotationInfoModel process(T annotation);

    final static class Reflection extends AnnotationProcessor<Annotation> {
        public Reflection(Model parent) {
            super(parent);
        }

        @Override
        public Reflection add(Annotation... annotations) {
            return (Reflection) super.add(annotations);
        }

        @Override
        public Reflection add(Annotation annotation) {
            return (Reflection) super.add(annotation);
        }

        @Override
        public Reflection add(Stream<Annotation> annotations) {
            return (Reflection) super.add(annotations);
        }

        @Override
        public Reflection add(Collection<Annotation> annotations) {
            return (Reflection) super.add(annotations);
        }

        public Reflection add(AnnotatedElement element) {
            return add(element.getAnnotations());
        }

        @Override
        protected AnnotationInfoModel process(Annotation annotation) {
            return AnnotationInfoModel.of(annotation, parent);
        }
    }

    final static class Source extends AnnotationProcessor<AnnotationInfo> {
        public Source(Model parent) {
            super(parent);
        }

        @Override
        public Source add(AnnotationInfo... annotations) {
            return (Source) super.add(annotations);
        }

        @Override
        public Source add(AnnotationInfo annotation) {
            return (Source) super.add(annotation);
        }

        @Override
        public Source add(Stream<AnnotationInfo> annotations) {
            return (Source) super.add(annotations);
        }

        @Override
        public Source add(Collection<AnnotationInfo> annotations) {
            return (Source) super.add(annotations);
        }

        public Source add(TypeSignature signature) {
            if (signature != null) {
                add(signature.getTypeAnnotationInfo());
            }
            return this;
        }

        public Source add(ClassInfo cls) {
            if (cls != null) {
                add(cls.getAnnotationInfo());
            }

            return this;
        }

        public Source add(FieldInfo field) {
            if (field != null) {
                add(field.getAnnotationInfo());
            }
            return this;
        }

        public Source add(MethodInfo method) {
            if (method != null) {
                add(method.getAnnotationInfo());
            }
            return this;
        }

        public Source add(MethodParameterInfo parameter) {
            if (parameter != null) {
                add(parameter.getAnnotationInfo());
            }
            return this;
        }

        public Source add(List<AnnotationInfoList> annotations) {
            if (annotations != null) {
                annotations.forEach(this::add);
            }
            return this;
        }

        public Source add(TypeArgument argument) {
            if (argument != null) {
                add(argument.getTypeSignature().getTypeAnnotationInfo());
            }
            return this;
        }

        @Override
        protected AnnotationInfoModel process(AnnotationInfo annotation) {
            return AnnotationInfoModel.of(annotation, parent);
        }
    }
}
