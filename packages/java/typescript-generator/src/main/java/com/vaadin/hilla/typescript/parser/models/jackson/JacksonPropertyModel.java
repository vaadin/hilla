package com.vaadin.hilla.typescript.parser.models.jackson;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.jackson.databind.introspect.BeanPropertyDefinition;

import com.vaadin.hilla.typescript.parser.models.AnnotatedModel;
import com.vaadin.hilla.typescript.parser.models.AnnotationInfoModel;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import com.vaadin.hilla.typescript.parser.models.ClassMemberModel;
import com.vaadin.hilla.typescript.parser.models.FieldInfoModel;
import com.vaadin.hilla.typescript.parser.models.MethodInfoModel;
import com.vaadin.hilla.typescript.parser.models.Model;
import com.vaadin.hilla.typescript.parser.models.NamedModel;
import com.vaadin.hilla.typescript.parser.models.OwnedModel;
import com.vaadin.hilla.typescript.parser.models.ReflectionModel;

import com.vaadin.hilla.typescript.parser.models.SignatureModel;
import org.jspecify.annotations.NonNull;

public final class JacksonPropertyModel
        extends JacksonModel<FieldInfoModel, MethodInfoModel, MethodInfoModel>
        implements NamedModel, OwnedModel<ClassInfoModel>, ReflectionModel {
    private ClassInfoModel owner;
    private final List<SignatureModel> types;

    private JacksonPropertyModel(BeanPropertyDefinition origin) {
        super(origin);
        this.types = Stream
                .of(getGetter().map(MethodInfoModel::getResultType), getSetter()
                        .map(setter -> setter.getParameters().get(0).getType()),
                        getField().map(FieldInfoModel::getType))
                .flatMap(Optional::stream).collect(Collectors.toList());
    }

    public static JacksonPropertyModel of(
            @NonNull BeanPropertyDefinition origin) {
        return new JacksonPropertyModel(Objects.requireNonNull(origin));
    }

    public boolean couldDeserialize() {
        return origin.couldDeserialize();
    }

    public List<SignatureModel> getAssociatedTypes() {
        return types;
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
