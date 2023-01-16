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
    protected FieldInfoModel prepareField() {
        return FieldInfoModel.of(origin.getField().getAnnotated());
    }

    @Override
    protected Optional<MethodInfoModel> prepareGetter() {
        var getter = origin.getGetter();
        return getter == null ? Optional.empty()
                : Optional.of(
                        MethodInfoModel.of(origin.getGetter().getAnnotated()));
    }

    @Override
    protected String prepareName() {
        return origin.getName();
    }

    @Override
    protected SignatureModel prepareType() {
        var getter = origin.getGetter();
        return SignatureModel.of(getter == null
                ? origin.getField().getAnnotated().getAnnotatedType()
                : origin.getGetter().getAnnotated().getAnnotatedReturnType());
    }
}
