package dev.hilla.parser.models;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import jakarta.annotation.Nonnull;

public abstract class PropertyInfoModel extends AnnotatedAbstractModel
        implements Model, NamedModel, OwnedModel<ClassInfoModel> {
    private final ClassInfoModel owner;
    private FieldInfoModel field;
    private Optional<MethodInfoModel> getter;
    private String name;
    private SignatureModel type;

    protected PropertyInfoModel(ClassInfoModel owner) {
        this.owner = owner;
    }

    public static PropertyInfoModel of(@Nonnull BeanPropertyDefinition origin,
            @Nonnull ClassInfoModel owner) {
        return new PropertyInfoReflectionModel(origin, owner);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PropertyInfoModel)) {
            return false;
        }

        var other = (PropertyInfoModel) obj;

        return getName().equals(other.getName())
                && getOwner().equals(other.getOwner());
    }

    @Override
    public Class<? extends Model> getCommonModelClass() {
        return PropertyInfoModel.class;
    }

    public FieldInfoModel getField() {
        if (field == null) {
            field = prepareField();
        }

        return field;
    }

    public Optional<MethodInfoModel> getGetter() {
        if (getter == null) {
            getter = prepareGetter();
        }

        return getter;
    }

    @Override
    public String getName() {
        if (name == null) {
            name = prepareName();
        }

        return name;
    }

    @Override
    public ClassInfoModel getOwner() {
        return owner;
    }

    public SignatureModel getType() {
        if (type == null) {
            type = prepareType();
        }

        return type;
    }

    public abstract boolean hasGetter();

    @Override
    public int hashCode() {
        return (getName().hashCode() + getOwner().hashCode()) ^ 0x73448be4;
    }

    public abstract boolean isTransient();

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return Stream
                .of(getField().getAnnotationsStream(),
                        getGetter().map(MethodInfoModel::getAnnotationsStream)
                                .orElse(Stream.empty()))
                .flatMap(Function.identity()).collect(Collectors.toList());
    }

    protected abstract FieldInfoModel prepareField();

    protected abstract Optional<MethodInfoModel> prepareGetter();

    protected abstract String prepareName();

    protected abstract SignatureModel prepareType();
}
