package dev.hilla.parser.test.helpers.context;

import io.github.classgraph.ScanResult;

public class BaseContext {
    private final ScanResult source;

    public BaseContext(ScanResult source) {
        this.source = source;
    }

    public ScanResult getSource() {
        return source;
    }
}
