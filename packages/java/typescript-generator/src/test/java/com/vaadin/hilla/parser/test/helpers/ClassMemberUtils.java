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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.vaadin.hilla.parser.models.MethodInfoModel;

public final class ClassMemberUtils {
    public static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static Stream<MethodInfoModel> cleanup(
            Stream<MethodInfoModel> models) {
        return models.filter(ClassMemberUtils::skipJacoco);
    }

    public static Optional<Method> getAnyDeclaredMethod(Class<?> cls,
            String name) {
        return Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> name.equals(m.getName())).findFirst();
    }

    public static Stream<Class<?>> getDeclaredClasses(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredClasses());
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> cls) {
        return getDeclaredConstructor(cls, List.of());
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> cls,
            List<Class<?>> parameterTypes) {
        try {
            return cls.getDeclaredConstructor(
                    parameterTypes.toArray(Class<?>[]::new));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Constructor<?>> getDeclaredConstructors(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredConstructors());
    }

    public static Field getDeclaredField(Class<?> cls, String name) {
        try {
            return cls.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Field> getDeclaredFields(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredFields())
                .filter(ClassMemberUtils::skipJacoco);
    }

    public static Method getDeclaredMethod(Class<?> cls, String name) {
        return getDeclaredMethod(cls, name, List.of());
    }

    public static Method getDeclaredMethod(Class<?> cls, String name,
            List<Class<?>> parameterTypes) {
        try {
            return cls.getDeclaredMethod(name,
                    parameterTypes.toArray(Class<?>[]::new));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Method> getDeclaredMethods(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredMethods())
                .filter(ClassMemberUtils::skipJacoco);
    }

    public static Parameter getParameter(Executable method, int index) {
        return method.getParameters()[index];
    }

    public static String toGetterName(String name) {
        return "get" + capitalize(name);
    }

    public static String toSetterName(String name) {
        return "set" + capitalize(name);
    }

    private static boolean skipJacoco(Member member) {
        return !member.getName().contains("jacoco");
    }

    private static boolean skipJacoco(MethodInfoModel model) {
        return !model.getName().contains("jacoco");
    }
}
