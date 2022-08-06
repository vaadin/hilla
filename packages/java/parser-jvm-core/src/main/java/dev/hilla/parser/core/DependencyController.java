package dev.hilla.parser.core;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import dev.hilla.parser.models.ClassInfoModel;

class DependencyController {
    private final Queue<ClassInfoModel> dependencies;
    private final Set<ClassInfoModel> visited = new HashSet<>();

    DependencyController(Queue<ClassInfoModel> dependencies) {
        this.dependencies = dependencies;
    }

    public boolean isVisited(ClassInfoModel model) {
        return visited.contains(model);
    }

    public void register(ClassInfoModel model) {
        dependencies.add(model);
        visited.add(model);
    }
}
