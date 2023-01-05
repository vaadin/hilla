package dev.hilla.parser.core;

import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class ProjectScanResult {
    private final ScanResult result;

    public ProjectScanResult(ScanResult result) {
        this.result = result;
    }

    public List<? extends Class<?>> getClassesWithAnnotation(
            String annotation) {
        return result.getClassesWithAnnotation(annotation).stream()
                .map(ClassInfo::loadClass).collect(Collectors.toList());
    }
}
