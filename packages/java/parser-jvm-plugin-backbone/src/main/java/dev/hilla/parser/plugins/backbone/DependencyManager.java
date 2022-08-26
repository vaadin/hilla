package dev.hilla.parser.plugins.backbone;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import dev.hilla.parser.models.ClassInfoModel;

public class DependencyManager extends LinkedList<ClassInfoModel> {
    private final Set<ClassInfoModel> visited = new HashSet<>();

    @Override
    public boolean add(ClassInfoModel model) {
        if (!visited.contains(model)) {
            visited.add(model);
            return super.add(model);
        }

        return false;
    }
}
