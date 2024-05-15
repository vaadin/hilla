package com.vaadin.hilla.parser.utils;

import com.googlecode.gentyref.GenericTypeReflector;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Utility class for working with generics.
 */
public class Generics {
    /**
     * Returns the exact type of the elements in an iterable class.
     *
     * @param cls
     *            the class to get the exact type of
     * @return the exact type of the elements in the iterable class
     */
    public static Optional<Type> getExactIterableType(Class<?> cls) {
        try {
            // We can find the exact type by looking at the iterator method
            // return type
            var method = cls.getMethod("iterator");
            var exactReturnType = GenericTypeReflector
                    .getExactReturnType(method, cls);

            // We know the format of the method, so we can safely cast the type
            if (exactReturnType instanceof ParameterizedType) {
                return Optional.of(((ParameterizedType) exactReturnType)
                        .getActualTypeArguments()[0]);
            }

            return Optional.empty();
        } catch (NoSuchMethodException e) {
            // This should really never happen if the passed class is an
            // Iterable
            throw new IllegalArgumentException(
                    "Class " + cls.getName() + " is not an Iterable");
        }
    }
}
