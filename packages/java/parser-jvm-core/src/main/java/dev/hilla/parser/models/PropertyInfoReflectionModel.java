package dev.hilla.parser.models;

import java.util.Optional;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

final class PropertyInfoReflectionModel extends PropertyInfoModel {
    private final BeanPropertyDefinition origin;

    PropertyInfoReflectionModel(BeanPropertyDefinition origin,
            ClassInfoModel owner) {
        super(owner);
        this.origin = origin;
    }

    @Override
    public BeanPropertyDefinition get() {
        return origin;
    }

    @Override
    public boolean hasGetter() {
        return origin.hasGetter();
    }

    @Override
    public boolean isTransient() {
        return !origin.hasGetter() && origin.getField().isTransient();
    }

    @Override
    protected FieldInfoModel prepareField() {
        return FieldInfoModel.of(origin.getField().getAnnotated());
    }

    @Override
    protected Optional<MethodInfoModel> prepareGetter() {
        return origin.hasGetter()
                ? Optional.of(
                        MethodInfoModel.of(origin.getGetter().getAnnotated()))
                : Optional.empty();
    }

    @Override
    protected String prepareName() {
        return origin.getName();
    }

    @Override
    protected SignatureModel prepareType() {
        return SignatureModel.of(origin.hasGetter()
                ? origin.getGetter().getAnnotated().getAnnotatedReturnType()
                : origin.getField().getAnnotated().getAnnotatedType());
    }
}
