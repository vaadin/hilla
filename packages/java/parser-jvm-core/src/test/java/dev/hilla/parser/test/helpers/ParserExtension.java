package dev.hilla.parser.test.helpers;

import java.net.URISyntaxException;
import java.util.Objects;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import dev.hilla.parser.testutils.ResourceLoader;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class ParserExtension
        implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
    public static final ExtensionContext.Namespace STORE = ExtensionContext.Namespace
            .create(ParserExtension.class);

    public static ScanResult getScanResult(ExtensionContext context) {
        var store = context.getStore(STORE);
        return (ScanResult) Objects.requireNonNull(store.get(Keys.SCAN_RESULT));
    }

    @Override
    public void afterAll(ExtensionContext context) {
        getScanResult(context).close();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws URISyntaxException {
        var target = getClass();
        var loader = new ResourceLoader(target::getResource,
                target::getProtectionDomain);
        var targetDir = loader.findTargetDirPath();
        var scanResult = new ClassGraph().enableAllInfo()
                .enableSystemJarsAndModules()
                .overrideClasspath(targetDir.toString()).scan();
        var store = context.getStore(STORE);
        store.put(Keys.SCAN_RESULT, scanResult);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
            ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter()
                .getAnnotation(WithScanResult.class) != null;
    }

    @Override
    public ScanResult resolveParameter(ParameterContext parameterContext,
            ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return getScanResult(extensionContext);
    }

    public enum Keys {
        SCAN_RESULT
    }
}
