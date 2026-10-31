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
package com.vaadin.hilla.parser.test.helpers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

public final class ClassMemberUtils {
    private ClassMemberUtils() {
    }

    public static Method getDeclaredMethod(Class<?> cls, String name) {
        try {
            return cls.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The methods a class declares, ordered by name, leaving out the synthetic
     * ones, such as those code coverage adds.
     */
    public static Stream<Method> getMethods(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredMethods())
                .filter(method -> !method.isSynthetic()
                        && !method.getName().contains("jacoco"))
                .sorted(Comparator.comparing(Method::getName));
    }
}
