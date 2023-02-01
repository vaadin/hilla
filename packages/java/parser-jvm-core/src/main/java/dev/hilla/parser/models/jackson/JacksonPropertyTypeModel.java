package dev.hilla.parser.models.jackson;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import dev.hilla.parser.models.AnnotatedAbstractModel;
import dev.hilla.parser.models.AnnotatedModel;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.models.SignatureModel;

import jakarta.annotation.Nonnull;

public final class JacksonPropertyTypeModel extends AnnotatedAbstractModel
        implements
        JacksonModel<SignatureModel, SignatureModel, SignatureModel> {
    private final BeanPropertyDefinition origin;
    private Optional<SignatureModel> fieldType;
    private Optional<SignatureModel> getterType;
    private Optional<SignatureModel> setterType;

    private JacksonPropertyTypeModel(BeanPropertyDefinition origin) {
        this.origin = origin;
    }

    public static JacksonPropertyTypeModel of(
            @Nonnull BeanPropertyDefinition origin) {
        return new JacksonPropertyTypeModel(Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof JacksonPropertyTypeModel)) {
            return false;
        }

        var other = (JacksonPropertyTypeModel) obj;

        return origin.getName().equals(other.origin.getName())
                && getField().equals(other.getField())
                && getGetter().equals(other.getGetter())
                && getSetter().equals(other.getSetter());
    }

    @Override
    public BeanPropertyDefinition get() {
        return origin;
    }

    @Override
    public Class<? extends Model> getCommonModelClass() {
        return JacksonPropertyTypeModel.class;
    }

    public Optional<SignatureModel> getField() {
        if (fieldType == null) {
            fieldType = origin.hasField()
                    ? Optional.of(SignatureModel.of(origin.getField()
                            .getAnnotated().getAnnotatedType()))
                    : Optional.empty();
        }

        return fieldType;
    }

    public Optional<SignatureModel> getGetter() {
        if (getterType == null) {
            getterType = origin.hasGetter()
                    ? Optional.of(SignatureModel.of(origin.getGetter()
                            .getAnnotated().getAnnotatedReturnType()))
                    : Optional.empty();
        }

        return getterType;
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

    public Optional<SignatureModel> getSetter() {
        if (setterType == null) {
            setterType = origin.hasSetter() ? Optional.of(SignatureModel
                    .of(origin.getSetter().getAnnotated().getParameters()[0]
                            .getAnnotatedType()))
                    : Optional.empty();
        }

        return setterType;
    }

    public boolean hasFieldType() {
        return origin.hasField();
    }

    public boolean hasGetterType() {
        return origin.hasGetter();
    }

    public boolean hasSetterType() {
        return origin.hasSetter();
    }

    @Override
    public int hashCode() {
        return (origin.getName().hashCode() + getField().hashCode()
                + getGetter().hashCode() + getSetter().hashCode()) ^ 0x10e6f7b;
    }

    @Override
    public String toString() {
        return "JacksonPropertyTypeModel[" + origin + "]";
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return Stream.of(getField(), getGetter(), getSetter())
                .filter(Optional::isPresent).map(Optional::get)
                .flatMap(AnnotatedModel::getAnnotationsStream)
                .collect(Collectors.toList());
    }
}
