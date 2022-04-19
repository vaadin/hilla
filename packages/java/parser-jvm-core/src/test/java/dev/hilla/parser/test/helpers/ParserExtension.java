package dev.hilla.parser.test.helpers;

import java.net.URISyntaxException;
import java.util.Objects;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import dev.hilla.parser.testutils.ResourceLoader;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class ParserExtension implements BeforeAllCallback, AfterAllCallback {
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
                .overrideClasspath(targetDir.toString()).scan();
        var store = context.getStore(STORE);
        store.put(Keys.SCAN_RESULT, scanResult);
    }

    public enum Keys {
        SCAN_RESULT
    }
}
