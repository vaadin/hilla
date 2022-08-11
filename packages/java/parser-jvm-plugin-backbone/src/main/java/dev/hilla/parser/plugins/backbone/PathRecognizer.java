package dev.hilla.parser.plugins.backbone;

import java.util.HashSet;
import java.util.Set;

import dev.hilla.parser.core.Path;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

class PathRecognizer {
    private final Set<ClassInfoModel> dependenciesToIgnore = new HashSet<>();
    private final String endpointAnnotationName;

    public PathRecognizer(String endpointAnnotationName) {
        this.endpointAnnotationName = endpointAnnotationName;
    }

    static boolean isGetter(MethodInfoModel method) {
        return method.getName().startsWith("get");
    }

    public void ignoreDependency(ClassInfoModel model) {
        dependenciesToIgnore.add(model);
    }

    public boolean isClassReferencedByUsedMembers(Path<?> path) {
        return path.getAscendants().stream()
                .anyMatch(ascendant -> isEndpointMethodReference(ascendant)
                        || isEntityFieldReference(ascendant));
    }

    public boolean isEndpointClass(Path<?> path) {
        var model = path.getModel();

        return model instanceof ClassInfoModel
                && isEndpointClass((ClassInfoModel) model);
    }

    public boolean isEndpointClass(ClassInfoModel model) {
        return model.getAnnotationsStream().anyMatch(annotation -> annotation
                .getName().equals(endpointAnnotationName));
    }

    public boolean isEndpointMethodReference(Path<?> path) {
        return path.getModel() instanceof MethodInfoModel
                && isEndpointClass(path.getParent());
    }

    public boolean isEntityClass(ClassInfoModel model) {
        return !dependenciesToIgnore.contains(model) && !isEndpointClass(model)
                && !model.isSynthetic() && !model.isIterable()
                && !model.isDate() && !model.isDateTime() && !model.isIterable()
                && !model.isMap();
    }

    public boolean isEntityFieldReference(Path<?> path) {
        return path.getModel() instanceof FieldInfoModel
                && !isEndpointClass(path.getParent());
    }
}
