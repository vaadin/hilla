package com.vaadin.hilla.parser.test.helpers;

import java.net.URISyntaxException;
import java.util.Objects;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.vaadin.hilla.parser.testutils.ResourceLoader;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class SourceExtension
        implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
    public static final ExtensionContext.Namespace SOURCE = ExtensionContext.Namespace
            .create(SourceExtension.class);

    public static ScanResult getSource(ExtensionContext context) {
        var store = Objects.requireNonNull(context.getStore(SOURCE));

        return (ScanResult) Objects
                .requireNonNull(store.get(context.getRequiredTestClass()));
    }

    @Override
    public void afterAll(ExtensionContext context) {
        getSource(context).close();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws URISyntaxException {
        var testClass = context.getRequiredTestClass();
        var loader = new ResourceLoader(testClass);
        var targetDir = loader.findTargetDirPath();

        var source = new ClassGraph().enableAllInfo()
                .enableSystemJarsAndModules()
                .overrideClasspath(targetDir.toString()).scan();

        context.getStore(SOURCE).put(testClass, source);
    }

    @Override
    public ScanResult resolveParameter(ParameterContext parameterContext,
            ExtensionContext context) throws ParameterResolutionException {
        return getSource(context);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
            ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter()
                .getAnnotation(Source.class) != null;
    }
}
