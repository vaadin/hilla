package dev.hilla.parser.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.utils.Lists;

public abstract class NodePath {
    protected final Model model;
    private final List<Model> added = new ArrayList<>();
    private final List<NodePath> ascendants;
    private boolean removed = false;

    private NodePath(Model model, List<NodePath> ascendants) {
        this.model = model;
        this.ascendants = ascendants;
    }

    static NodePath of(Model model, List<NodePath> ascendants) {
        return of(model, ascendants, false);
    }

    static NodePath of(Model model, List<NodePath> ascendants,
            boolean declaration) {
        return model instanceof ClassInfoModel && declaration
                ? new ClassDeclaration((ClassInfoModel) model, ascendants)
                : new Regular(model, ascendants);
    }

    public void add(Model... nodes) {
        Collections.addAll(added, nodes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        var other = (NodePath) obj;

        return ascendants.equals(other.ascendants) && model.equals(other.model);
    }

    public List<NodePath> getAscendants() {
        return ascendants;
    }

    public Model getModel() {
        return model;
    }

    public NodePath getParent() {
        return Lists.getLastElement(ascendants);
    }

    @Override
    public int hashCode() {
        return model.hashCode() + 7 * ascendants.hashCode();
    }

    public boolean isRemoved() {
        return removed;
    }

    public void remove() {
        removed = true;
    }

    public void replace(Model... models) {
        removed = true;
        add(models);
    }

    void forEachAddedNode(Consumer<NodePath> consumer) {
        added.stream().map(model -> NodePath.of(model, getAscendantsForChild()))
                .forEach(consumer);
    }

    List<NodePath> getAscendantsForChild() {
        return Lists.append(ascendants, this);
    }

    public static final class ClassDeclaration extends NodePath {
        private ClassDeclaration(ClassInfoModel model,
                List<NodePath> ascendants) {
            super(model, ascendants);
        }
    }

    public static final class Regular extends NodePath {
        private Regular(Model model, List<NodePath> ascendants) {
            super(model, ascendants);
        }
    }
}
