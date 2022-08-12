package dev.hilla.parser.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.utils.Lists;

public abstract class NodePath {
    private final List<NodePath> ascendants;
    private final List<Command> commands = new ArrayList<>();
    private final Model model;

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
        commands.add(new Command.Add(nodes));
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

        return ascendants.equals(other.ascendants) && model.equals(other.model)
                && commands.equals(other.commands);
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
        return model.hashCode() + 7 * ascendants.hashCode()
                + 11 * commands.hashCode();
    }

    public boolean isRemoved() {
        return commands.stream().anyMatch(cmd -> cmd instanceof Command.Replace
                || cmd instanceof Command.Remove);
    }

    public void remove() {
        commands.add(new Command.Remove());
    }

    public void replace(Model... models) {
        commands.add(new Command.Replace(models));
    }

    void forEachAddedNode(Consumer<NodePath> consumer) {
        commands.stream().map(Command::getContent).filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .map(model -> NodePath.of(model, getAscendantsForChild()))
                .forEach(consumer);
    }

    List<NodePath> getAscendantsForChild() {
        return Lists.append(ascendants, this);
    }

    List<Command> getCommands() {
        return commands;
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
