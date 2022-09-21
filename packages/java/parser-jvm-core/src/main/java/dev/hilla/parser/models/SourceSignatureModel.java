package dev.hilla.parser.models;

import io.github.classgraph.HierarchicalTypeSignature;

public interface SourceSignatureModel extends SourceModel {
    @Override
    HierarchicalTypeSignature get();
}
