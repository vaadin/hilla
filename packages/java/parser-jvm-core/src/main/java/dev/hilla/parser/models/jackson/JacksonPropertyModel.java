package dev.hilla.parser.models.jackson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import dev.hilla.parser.models.AnnotatedAbstractModel;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.models.NamedModel;
import dev.hilla.parser.models.OwnedModel;
import dev.hilla.parser.models.SignatureModel;

import jakarta.annotation.Nonnull;

public final class JacksonPropertyModel extends AnnotatedAbstractModel
        implements Model, NamedModel, OwnedModel<ClassInfoModel> {
    private final BeanPropertyDefinition origin;
    private Optional<FieldInfoModel> field;
    private Optional<MethodInfoModel> getter;
    private SignatureModel type;

    private JacksonPropertyModel(BeanPropertyDefinition origin) {
        this.origin = origin;
    }

    public static JacksonPropertyModel of(
            @Nonnull BeanPropertyDefinition origin) {
        return new JacksonPropertyModel(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof JacksonPropertyModel)) {
            return false;
        }

        var other = (JacksonPropertyModel) obj;

        return origin.getName().equals(other.origin.getName())
                && getOwner().equals(other.getOwner());
    }

    @Override
    public BeanPropertyDefinition get() {
        return origin;
    }

    @Override
    public Class<? extends Model> getCommonModelClass() {
        return JacksonPropertyModel.class;
    }

    public Optional<FieldInfoModel> getField() {
        if (field == null) {
            field = origin.hasField()
                    ? Optional.of(
                            FieldInfoModel.of(origin.getField().getAnnotated()))
                    : Optional.empty();
        }

        return field;
    }

    public Optional<MethodInfoModel> getGetter() {
        if (getter == null) {
            getter = origin.hasGetter()
                    ? Optional.of(MethodInfoModel
                            .of(origin.getGetter().getAnnotated()))
                    : Optional.empty();
        }

        return getter;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public ClassInfoModel getOwner() {
        return ClassInfoModel.of(origin.getAccessor().getDeclaringClass());
    }

    public SignatureModel getType() {
        var accessor = origin.getAccessor().getAnnotated();

        if (type == null) {
            type = SignatureModel.of(accessor instanceof Method
                    ? ((Method) accessor).getAnnotatedReturnType()
                    : ((Field) accessor).getAnnotatedType());
        }

        return type;
    }

    public boolean hasField() {
        return origin.hasField();
    }

    public boolean hasGetter() {
        return origin.hasGetter();
    }

    @Override
    public int hashCode() {
        return (origin.getName().hashCode() + getOwner().hashCode())
                ^ 0x73448be4;
    }

    @Override
    public boolean isReflection() {
        return true;
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return Arrays
                .stream(origin.getAccessor().getAnnotated().getAnnotations())
                .map(AnnotationInfoModel::of).collect(Collectors.toList());
    }
}
