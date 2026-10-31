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
package com.vaadin.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.vaadin.hilla.parser.models.pack.Foo;
import com.vaadin.hilla.parser.models.pack.PackageInfoModelSample;

public class PackageInfoModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp() {
        this.ctx = new Context();
    }

    @DisplayName("It should contain an annotation")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ContainAnnotation(PackageInfoModel model) {
        assertEquals(List.of(Foo.class.getName()),
                model.getAnnotations().stream()
                        .map(AnnotationInfoModel::getName)
                        .collect(Collectors.toList()));
    }

    @DisplayName("It should get annotations with ClassInfo")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetAnnotationsWithClassInfo(PackageInfoModel model) {
        assertEquals(List.of(ClassInfoModel.of(Foo.class)),
                model.getAnnotations().stream()
                        .map(AnnotationInfoModel::getClassInfo)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    @DisplayName("It should have the same hashCode for models of the same origin")
    @Test
    public void should_HaveSameHashCodeForModelsOfSameOrigin() {
        var model = PackageInfoModel.of(ctx.getReflectionOrigin());
        var other = PackageInfoModel.of(ctx.getReflectionOrigin());

        assertEquals(model.hashCode(), other.hashCode());
    }

    @DisplayName("It should have models of the same origin equal")
    @Test
    public void should_HaveModelsOfSameOriginEqual() {
        var model = PackageInfoModel.of(ctx.getReflectionOrigin());
        var other = PackageInfoModel.of(ctx.getReflectionOrigin());

        assertEquals(model, model);
        assertEquals(model, other);
        assertEquals(other, model);

        assertNotEquals(model, new Object());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(PackageInfoModel model) {
        assertEquals(ctx.getReflectionOrigin(), model.get());
    }

    static class Context {
        private static final Package reflectionOrigin = PackageInfoModelSample.class
                .getPackage();

        public Package getReflectionOrigin() {
            return reflectionOrigin;
        }
    }

    static class ModelProvider implements ArgumentsProvider {
        static final String testNamePattern = "{0}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new Context();

            return Stream.of(Arguments
                    .of(PackageInfoModel.of(ctx.getReflectionOrigin())));
        }
    }
}
