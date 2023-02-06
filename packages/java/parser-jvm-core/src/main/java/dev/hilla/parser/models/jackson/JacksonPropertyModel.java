package dev.hilla.parser.models.jackson;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import dev.hilla.parser.models.AnnotatedModel;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassMemberModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.models.NamedModel;
import dev.hilla.parser.models.OwnedModel;
import dev.hilla.parser.models.ReflectionModel;

import jakarta.annotation.Nonnull;

public final class JacksonPropertyModel
        extends JacksonModel<FieldInfoModel, MethodInfoModel, MethodInfoModel>
        implements NamedModel, OwnedModel<ClassInfoModel>, ReflectionModel {
    private ClassInfoModel owner;
    private JacksonPropertyTypeModel type;

    private JacksonPropertyModel(BeanPropertyDefinition origin) {
        super(origin);
    }

    public static JacksonPropertyModel of(
            @Nonnull BeanPropertyDefinition origin) {
        return new JacksonPropertyModel(Objects.requireNonNull(origin));
    }

    public boolean couldDeserialize() {
        return origin.couldDeserialize();
    }

    public Optional<? extends ClassMemberModel> getAccessor() {
        if (origin.hasGetter()) {
            return getGetter();
        } else if (origin.hasField()) {
            return getField();
        }

        return Optional.empty();
    }

    @Override
    public Class<? extends Model> getCommonModelClass() {
        return JacksonPropertyModel.class;
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

    public JacksonPropertyTypeModel getType() {
        if (type == null) {
            type = JacksonPropertyTypeModel.of(origin);
        }

        return type;
    }

    public boolean isExplicitlyIncluded() {
        return origin.isExplicitlyIncluded();
    }

    @Override
    public String toString() {
        return "JacksonPropertyModel[" + origin + "]";
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return Stream
                .of(getField().map(AnnotatedModel::getAnnotations),
                        getGetter().map(AnnotatedModel::getAnnotations),
                        getSetter().map(
                                m -> m.getParameters().get(0).getAnnotations()))
                .flatMap(Optional::stream).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    protected FieldInfoModel prepareField() {
        return origin.hasField()
                ? FieldInfoModel.of(origin.getField().getAnnotated())
                : null;
    }

    @Override
    protected MethodInfoModel prepareGetter() {
        return origin.hasGetter()
                ? MethodInfoModel.of(origin.getGetter().getAnnotated())
                : null;
    }

    @Override
    protected MethodInfoModel prepareSetter() {
        return origin.hasSetter()
                ? MethodInfoModel.of(origin.getSetter().getAnnotated())
                : null;
    }
}
