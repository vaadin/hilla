package dev.hilla.parser.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

final class MemberList<E extends OwnedModel<O>, O extends Model>
        extends ArrayList<E> {
    private final O owner;

    MemberList(O owner) {
        super();
        this.owner = owner;
    }

    MemberList(Collection<E> collection, O owner) {
        super(collection);
        this.owner = owner;

        collection.forEach(this::setOwner);
    }

    public static <E extends OwnedModel<O>, O extends Model> Collector<E, ?, List<E>> collectWithOwner(
            O owner) {
        return Collectors.toCollection(() -> new MemberList<>(owner));
    }

    @Override
    public boolean add(E element) {
        setOwner(element);
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        collection.forEach(this::setOwner);
        return super.addAll(collection);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        collection.forEach(this::setOwner);
        return super.addAll(index, collection);
    }

    private void setOwner(OwnedModel<O> model) {
        if (model instanceof FieldInfoModel
                && owner instanceof ClassInfoModel) {
            ((FieldInfoModel) model).setOwner((ClassInfoModel) owner);
        } else if (model instanceof MethodInfoModel
                && owner instanceof ClassInfoModel) {
            ((MethodInfoModel) model).setOwner((ClassInfoModel) owner);
        } else if (model instanceof MethodParameterInfoModel
                && owner instanceof MethodInfoModel) {
            ((MethodParameterInfoModel) model)
                    .setOwner((MethodInfoModel) owner);
        }
    }
}
