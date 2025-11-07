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
package com.vaadin.hilla;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.hilla.utils.TestUtils;

public class NonnullParserTest {
    private static final String ANNOTATION_NAME = "NonNull";
    FieldDeclaration field;
    MethodDeclaration method;
    com.github.javaparser.ast.body.Parameter parameter;

    @Before
    public void init() throws FileNotFoundException {
        JavaParser parser = new JavaParser();
        File nonnullEntityFile = TestUtils
                .getClassFilePath(NonnullEntity.class.getPackage()).get(0)
                .resolve("NonnullEntity.java").toFile();
        ParseResult<CompilationUnit> result = parser.parse(nonnullEntityFile);
        CompilationUnit compilationUnit = result.getResult().get();
        ClassOrInterfaceDeclaration nonnullEntity = compilationUnit
                .getClassByName("NonnullEntity").get();
        field = nonnullEntity.getFieldByName("nonNullableField").get();
        method = nonnullEntity.getMethodsByName("nonNullableMethod").get(0);
        parameter = method.getParameter(0);
    }

    @Test
    public void should_haveNonNullableField() {
        assertTrue(field.isAnnotationPresent(ANNOTATION_NAME));
    }

    @Test
    public void should_haveFieldWithNonNullableCollectionItem() {
        assertTrue(
                field.getVariables().get(0).getType().asClassOrInterfaceType()
                        .getTypeArguments().get().get(0).getAnnotations()
                        .stream().anyMatch(annotation -> ANNOTATION_NAME
                                .equals(annotation.getName().asString())));
    }

    @Test
    public void should_haveMethodWithNonNullableReturnType() {
        assertTrue(method.isAnnotationPresent(ANNOTATION_NAME));
    }

    @Test
    public void should_haveMethodWithNonNullableParameter() {
        assertTrue(parameter.isAnnotationPresent(ANNOTATION_NAME));
    }

    @Test
    public void should_haveMethodParameterWithNonNullableCollectionItemType() {
        assertTrue(parameter.getType().asClassOrInterfaceType()
                .getTypeArguments().get().get(1).getAnnotations().stream()
                .anyMatch(annotation -> ANNOTATION_NAME
                        .equals(annotation.getName().asString())));
    }
}
