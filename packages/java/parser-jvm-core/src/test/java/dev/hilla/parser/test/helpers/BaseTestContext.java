package dev.hilla.parser.test.helpers;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.github.classgraph.ScanResult;

public abstract class BaseTestContext {
    private final ScanResult scanResult = null;

    public BaseTestContext(ExtensionContext context) {
        // this.scanResult = SourceExtension.getScanResult(context);
    }

    public ScanResult getScanResult() {
        return scanResult;
    }
}
