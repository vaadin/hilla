package dev.hilla.parser.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import dev.hilla.parser.models.Model;
import dev.hilla.parser.utils.Lists;

public final class Path<T extends Model> {
    private final List<Path<?>> ascendants;
    private final List<Command> commands = new ArrayList<>();
    private final boolean dependency;
    private final T model;

    Path(T model, List<Path<?>> ascendants) {
        this(model, ascendants, false);
    }

    Path(T model, List<Path<?>> ascendants, boolean dependency) {
        this.model = model;
        this.ascendants = ascendants;
        this.dependency = dependency;
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

        var other = (Path<?>) obj;

        return ascendants.equals(other.ascendants) && model.equals(other.model);
    }

    public List<Path<?>> getAscendants() {
        return ascendants;
    }

    public T getModel() {
        return model;
    }

    public Path<?> getParent() {
        return Lists.getLastElement(ascendants);
    }

    @Override
    public int hashCode() {
        return model.hashCode() + 7 * ascendants.hashCode();
    }

    public boolean isDependency() {
        return dependency;
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

    void forEachAddedNode(Consumer<Path<?>> consumer) {
        commands.stream().map(Command::getContent).filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .map(model -> new Path<>(model, getAscendantsForChild()))
                .forEach(consumer);
    }

    List<Path<?>> getAscendantsForChild() {
        return Lists.append(ascendants, this);
    }

    List<Command> getCommands() {
        return commands;
    }
}
