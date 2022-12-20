package dev.hilla.parser.models;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An artificial annotation info implementation.
 */
final class AnnotationInfoArtificialModel extends AnnotationInfoModel {
    private final String name;
    private final Set<AnnotationParameterModel> parameters;

    AnnotationInfoArtificialModel(String name,
            Set<AnnotationParameterModel> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected Optional<ClassInfoModel> prepareClassInfo() {
        return Optional.empty();
    }

    @Override
    protected Set<AnnotationParameterModel> prepareParameters() {
        return parameters;
    }

    @Override
    public Object get() {
        return null;
    }
}
