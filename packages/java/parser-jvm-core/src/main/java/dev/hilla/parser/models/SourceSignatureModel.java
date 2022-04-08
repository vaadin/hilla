package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.HierarchicalTypeSignature;

public interface SourceSignatureModel extends SourceModel {
    @Override
    HierarchicalTypeSignature get();

    @Override
    default Stream<ClassInfoModel> getDependenciesStream() {
        return DependencyCollector.collect(get());
    }
}
