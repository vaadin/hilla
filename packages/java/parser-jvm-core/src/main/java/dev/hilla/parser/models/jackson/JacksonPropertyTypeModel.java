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
        implements Model {
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
                && getFieldType().equals(other.getFieldType())
                && getGetterType().equals(other.getGetterType())
                && getSetterType().equals(other.getSetterType());
    }

    @Override
    public BeanPropertyDefinition get() {
        return origin;
    }

    @Override
    public Class<? extends Model> getCommonModelClass() {
        return JacksonPropertyTypeModel.class;
    }

    public Optional<SignatureModel> getFieldType() {
        if (fieldType == null) {
            fieldType = origin.hasField()
                    ? Optional.of(SignatureModel.of(origin.getField()
                            .getAnnotated().getAnnotatedType()))
                    : Optional.empty();
        }

        return fieldType;
    }

    public Optional<SignatureModel> getGetterType() {
        if (getterType == null) {
            getterType = origin.hasGetter()
                    ? Optional.of(SignatureModel.of(origin.getGetter()
                            .getAnnotated().getAnnotatedReturnType()))
                    : Optional.empty();
        }

        return getterType;
    }

    public SignatureModel getPrimaryType() {
        Optional<SignatureModel> type;

        if (origin.hasGetter()) {
            type = getGetterType();
        } else if (origin.hasSetter()) {
            type = getSetterType();
        } else {
            type = getFieldType();
        }

        return type.get();
    }

    public Optional<SignatureModel> getSetterType() {
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
        return (origin.getName().hashCode() + getFieldType().hashCode()
                + getGetterType().hashCode() + getSetterType().hashCode())
                ^ 0x10e6f7b;
    }

    @Override
    public String toString() {
        return "JacksonPropertyTypeModel[" + origin + "]";
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return Stream.of(getFieldType(), getGetterType(), getSetterType())
                .filter(Optional::isPresent).map(Optional::get)
                .flatMap(AnnotatedModel::getAnnotationsStream)
                .collect(Collectors.toList());
    }
}
