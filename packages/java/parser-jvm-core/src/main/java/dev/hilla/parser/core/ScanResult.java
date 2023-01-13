package dev.hilla.parser.core;

import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.ClassInfo;

public class ScanResult {
    private final io.github.classgraph.ScanResult result;

    public ScanResult(io.github.classgraph.ScanResult result) {
        this.result = result;
    }

    public io.github.classgraph.ScanResult get() {
        return result;
    }

    public List<? extends Class<?>> getClassesWithAnnotation(
            String annotation) {
        return result.getClassesWithAnnotation(annotation).stream()
                .map(ClassInfo::loadClass).collect(Collectors.toList());
    }
}
