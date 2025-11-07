/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.testutils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

/**
 * A converter for relative paths of
 * com.vaadin.hilla.engine.EngineAutoConfiguration. It solves an issue of
 * testing absolute paths on different OSes.
 * <p>
 * The class is detached (has no dependency to the real
 * com.vaadin.hilla.engine.EngineAutoConfiguration class) to avoid circular
 * dependencies with the `engine-core` package.
 */
public final class TestEngineConfigurationPathResolver {
    private static final String BASE_FIELD_NAME = "baseDir";
    private static final String CLASS_NAME = "com.vaadin.hilla.engine.EngineAutoConfiguration";
    private static final String CLASS_PATH_FIELD_NAME = "classPath";

    public static <T> T resolve(@NonNull T obj, @NonNull Path baseDir)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        Objects.requireNonNull(baseDir);
        if (!CLASS_NAME
                .equals(Objects.requireNonNull(obj).getClass().getName())) {
            throw new IllegalArgumentException(String.format(
                    "Only %s class instancies are allowed", CLASS_NAME));
        }

        var klass = (Class<T>) obj.getClass();

        var constructor = klass.getDeclaredConstructor();
        constructor.setAccessible(true);

        var clone = constructor.newInstance();
        for (var field : klass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            var value = field.get(obj);

            if (value instanceof Path && !((Path) value).isAbsolute()) {
                value = BASE_FIELD_NAME.equals(field.getName()) ? baseDir
                        : baseDir.resolve((Path) value);
            } else if (value instanceof Collection
                    && CLASS_PATH_FIELD_NAME.equals(field.getName())) {
                value = ((Collection<Path>) value).stream()
                        .map(baseDir::resolve)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }

            field.set(clone, value);
        }

        return clone;
    }
}
