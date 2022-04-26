package dev.hilla.parser.test.helpers;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.github.classgraph.ScanResult;

public abstract class BaseTestContext {
    private final ScanResult scanResult;

    public BaseTestContext(ExtensionContext context) {
        this.scanResult = ParserExtension.getScanResult(context);
    }

    public ScanResult getScanResult() {
        return scanResult;
    }
}
