package com.vaadin.hilla;

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

import static org.junit.Assert.assertTrue;

public class NonnullParserTest {
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
        assertTrue(field.isAnnotationPresent("Nonnull"));
    }

    @Test
    public void should_haveFieldWithNonNullableCollectionItem() {
        assertTrue(field.getVariables().get(0).getType()
                .asClassOrInterfaceType().getTypeArguments().get().get(0)
                .getAnnotations().stream().anyMatch(annotation -> "Nonnull"
                        .equals(annotation.getName().asString())));
    }

    @Test
    public void should_haveMethodWithNonNullableReturnType() {
        assertTrue(method.isAnnotationPresent("Nonnull"));
    }

    @Test
    public void should_haveMethodWithNonNullableParameter() {
        assertTrue(parameter.isAnnotationPresent("Nonnull"));
    }

    @Test
    public void should_haveMethodParameterWithNonNullableCollectionItemType() {
        assertTrue(parameter.getType().asClassOrInterfaceType()
                .getTypeArguments().get().get(1).getAnnotations().stream()
                .anyMatch(annotation -> "Nonnull"
                        .equals(annotation.getName().asString())));
    }
}
