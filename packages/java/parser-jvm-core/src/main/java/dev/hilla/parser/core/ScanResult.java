package dev.hilla.parser.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;

public class ScanResult {
    private final io.github.classgraph.ScanResult result;

    public ScanResult(io.github.classgraph.ScanResult result) {
        this.result = result;
    }

    public io.github.classgraph.ScanResult get() {
        return result;
    }

    public List<Class<?>> getClassesWithAnnotation(String... annotations) {
        Stream<ClassInfo> c = Stream.of(annotations)
                .flatMap(annotation -> result
                        .getClassesWithAnnotation(annotation).stream());
        return c.map(ClassInfo::loadClass).toList();
    }
}
