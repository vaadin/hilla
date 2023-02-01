package dev.hilla.parser.models.jackson;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import dev.hilla.parser.models.AnnotatedAbstractModel;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassMemberModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.models.NamedModel;
import dev.hilla.parser.models.OwnedModel;

import jakarta.annotation.Nonnull;

public final class JacksonPropertyModel extends AnnotatedAbstractModel
        implements
        JacksonModel<FieldInfoModel, MethodInfoModel, MethodInfoModel>,
        NamedModel, OwnedModel<ClassInfoModel> {
    private final BeanPropertyDefinition origin;
    private Optional<FieldInfoModel> field;
    private Optional<MethodInfoModel> getter;
    private ClassInfoModel owner;
    private Optional<MethodInfoModel> setter;
    private JacksonPropertyTypeModel type;

    private JacksonPropertyModel(BeanPropertyDefinition origin) {
        this.origin = origin;
    }

    public static JacksonPropertyModel of(
            @Nonnull BeanPropertyDefinition origin) {
        return new JacksonPropertyModel(Objects.requireNonNull(origin));
    }

    public boolean couldDeserialize() {
        return origin.couldDeserialize();
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
                && getField().equals(other.getField())
                && getGetter().equals(other.getGetter())
                && getSetter().equals(other.getSetter());
    }

    @Override
    public BeanPropertyDefinition get() {
        return origin;
    }

    public Optional<? extends ClassMemberModel> getAccessor() {
        if (origin.hasField()) {
            return getField();
        } else if (origin.hasGetter()) {
            return getGetter();
        }

        return Optional.empty();
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

    public Optional<? extends ClassMemberModel> getMutator() {
        if (origin.hasSetter()) {
            return getSetter();
        } else if (origin.hasField()) {
            return getField();
        }

        return Optional.empty();
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public ClassInfoModel getOwner() {
        if (owner == null) {
            owner = getPrimaryMember().getOwner();
        }

        return owner;
    }

    public ClassMemberModel getPrimaryMember() {
        if (origin.hasGetter()) {
            return getGetter().get();
        } else if (origin.hasSetter()) {
            return getSetter().get();
        }

        return getField().get();
    }

    public Optional<MethodInfoModel> getSetter() {
        if (setter == null) {
            setter = origin.hasSetter()
                    ? Optional.of(MethodInfoModel
                            .of(origin.getSetter().getAnnotated()))
                    : Optional.empty();
        }

        return setter;
    }

    public JacksonPropertyTypeModel getType() {
        if (type == null) {
            type = JacksonPropertyTypeModel.of(origin);
        }

        return type;
    }

    public boolean hasField() {
        return origin.hasField();
    }

    public boolean hasGetter() {
        return origin.hasGetter();
    }

    public boolean hasSetter() {
        return origin.hasSetter();
    }

    @Override
    public int hashCode() {
        return (origin.getName().hashCode() + getField().hashCode()
                + getGetter().hashCode() + getSetter().hashCode()) ^ 0x73448be4;
    }

    public boolean isExplicitlyIncluded() {
        return origin.isExplicitlyIncluded();
    }

    @Override
    public boolean isReflection() {
        return true;
    }

    @Override
    public String toString() {
        return "JacksonPropertyModel[" + origin + "]";
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return Stream
                .of(origin.getField(), origin.getGetter(), origin.getSetter())
                .filter(Objects::nonNull).map(Annotated::getAnnotated)
                .map(AnnotatedElement::getAnnotations).flatMap(Arrays::stream)
                .map(AnnotationInfoModel::of).collect(Collectors.toList());
    }
}
