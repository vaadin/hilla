package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedElement;

public interface ReflectionSignatureModel extends ReflectionModel {
    @Override
    AnnotatedElement get();
}
