package dev.hilla.parser.models.jackson;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import dev.hilla.parser.models.AnnotatedModel;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.models.ReflectionModel;
import dev.hilla.parser.models.SignatureModel;

import jakarta.annotation.Nonnull;

public final class JacksonPropertyTypeModel
        extends JacksonModel<SignatureModel, SignatureModel, SignatureModel>
        implements ReflectionModel {
    private JacksonPropertyTypeModel(BeanPropertyDefinition origin) {
        super(origin);
    }

    public static JacksonPropertyTypeModel of(
            @Nonnull BeanPropertyDefinition origin) {
        return new JacksonPropertyTypeModel(Objects.requireNonNull(origin));
    }

    @Override
    public Class<? extends Model> getCommonModelClass() {
        return JacksonPropertyTypeModel.class;
    }

    public SignatureModel getPrimary() {
        Optional<SignatureModel> type;

        if (origin.hasGetter()) {
            type = getGetter();
        } else if (origin.hasSetter()) {
            type = getSetter();
        } else {
            type = getField();
        }

        return type.get();
    }

    @Override
    protected SignatureModel prepareField() {
        return origin.hasField()
                ? SignatureModel
                        .of(origin.getField().getAnnotated().getAnnotatedType())
                : null;
    }

    @Override
    protected SignatureModel prepareGetter() {
        return origin.hasGetter()
                ? SignatureModel.of(origin.getGetter().getAnnotated()
                        .getAnnotatedReturnType())
                : null;
    }

    @Override
    protected SignatureModel prepareSetter() {
        return origin.hasSetter() ? SignatureModel
                .of(origin.getSetter().getAnnotated().getParameters()[0]
                        .getAnnotatedType())
                : null;
    }

    @Override
    public String toString() {
        return "JacksonPropertyTypeModel[" + origin + "]";
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return Stream.of(getField(), getGetter(), getSetter())
                .flatMap(Optional::stream)
                .flatMap(AnnotatedModel::getAnnotationsStream)
                .collect(Collectors.toList());
    }
}
