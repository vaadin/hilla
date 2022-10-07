package dev.hilla.parser.models;

import java.util.List;
import java.util.stream.Stream;

public interface ParameterizedModel {
    List<TypeParameterModel> getTypeParameters();

    default Stream<TypeParameterModel> getTypeParametersStream() {
        return getTypeParameters().stream();
    }
}
