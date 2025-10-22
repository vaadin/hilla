package com.vaadin.hilla.typescript.parser.models;

import io.github.classgraph.HierarchicalTypeSignature;

@Deprecated
public interface SourceSignatureModel extends SourceModel {
    @Override
    HierarchicalTypeSignature get();
}
