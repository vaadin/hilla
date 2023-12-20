package com.vaadin.hilla.parser.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class AnnotatedOwnerUtils {
    private static final Function<AnnotatedType, List<Annotation[]>> annotationsGetter;

    static {
        var isJDK11 = Runtime.Version.parse(System.getProperty("java.version"))
                .feature() <= 11;
        annotationsGetter = isJDK11 ? AnnotatedOwnerUtils::getJDK11Annotations
                : AnnotatedOwnerUtils::getDefaultAnnotations;
    }

    public static List<Annotation[]> getAllOwnersAnnotations(
            AnnotatedType type) {
        return annotationsGetter.apply(type);
    }

    private static List<Annotation[]> getDefaultAnnotations(
            AnnotatedType type) {
        var list = new ArrayList<Annotation[]>();

        while (type != null) {
            list.add(type.getAnnotations());
            type = type.getAnnotatedOwnerType();
        }

        return list;
    }

    /**
     * Let's imagine we have a following ClassRef:
     * {@code Owner.CurrentType.@Foo Child}. It has the following algorithm to
     * detect annotations in a JDK11.
     *
     * <ul>
     *
     * <li>if {@code Owner} exists AND ({@code CurrentType} is static OR
     * {@code Owner} is static and parametrized) — then annotations for that
     * type are stored in {@code Owner}.</li>
     *
     * <li>if {@code Child} exists AND ((it is dynamic AND {@code CurrentType}
     * is dynamic) OR (it is dynamic AND {@code CurrentType} is static but not
     * parametrized)) — then annotations for that type are {@code Child}'s
     * annotations and shouldn't be used.</li>
     *
     * <li>if {@code Child} and {@code CurrentType} are static — getting the
     * owner annotations should be skipped because for static parts all
     * annotations shared between all the parts</li>
     *
     * </ul>
     *
     * @param type
     *            a type for detecting annotations
     * @return a list of annotation — an array of annotations per each type part
     */
    private static List<Annotation[]> getJDK11Annotations(AnnotatedType type) {
        var list = new ArrayList<Annotation[]>();

        AnnotatedType child = null;
        var isChildStatic = false;

        while (type != null) {
            var typeCls = resolve(type);
            var isTypeStatic = Modifier.isStatic(typeCls.getModifiers());
            var isTypeParametrized = type instanceof AnnotatedParameterizedType;

            var owner = type.getAnnotatedOwnerType();

            var annotations = (Annotation[]) null;

            if (owner != null
                    && (child == null || !isChildStatic || !isTypeStatic)) {
                var ownerCls = resolve(owner);

                var isOwnerStatic = Modifier.isStatic(ownerCls.getModifiers());
                var isOwnerParametrized = owner instanceof AnnotatedParameterizedType;

                if (isTypeStatic || (!isTypeStatic && isOwnerStatic
                        && isOwnerParametrized)) {
                    annotations = owner.getAnnotations();
                }
            }

            if (annotations == null && (child == null
                    || (!isChildStatic && !isTypeStatic) || (!isChildStatic
                            && isTypeStatic && !isTypeParametrized))) {
                annotations = type.getAnnotations();
            }

            list.add(annotations != null ? annotations : new Annotation[0]);

            child = type;
            isChildStatic = isTypeStatic;
            type = type.getAnnotatedOwnerType();
        }

        return list;
    }

    private static Class<?> resolve(AnnotatedType type) {
        return type instanceof AnnotatedParameterizedType
                ? (Class<?>) ((ParameterizedType) type.getType()).getRawType()
                : (Class<?>) type.getType();
    }
}
