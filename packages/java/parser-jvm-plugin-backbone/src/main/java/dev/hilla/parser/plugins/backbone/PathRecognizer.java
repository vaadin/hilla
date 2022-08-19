package dev.hilla.parser.plugins.backbone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

class PathRecognizer {
    private final Map<ClassInfoModel, List<NodePath>> classMentions = new HashMap<>();
    private final String endpointAnnotationName;

    public PathRecognizer(String endpointAnnotationName) {
        this.endpointAnnotationName = endpointAnnotationName;
    }

    public void addClassMention(NodePath path) {
        var model = (ClassInfoModel) path.getModel();

        if (classMentions.containsKey(model)) {
            classMentions.get(model).add(path);
        } else {
            var paths = new ArrayList<NodePath>();
            paths.add(path);
            classMentions.put(model, paths);
        }
    }

    public boolean isEndpointClass(NodePath path) {
        var model = path.getModel();

        return model instanceof ClassInfoModel
                && isEndpointClass((ClassInfoModel) model);
    }

    public boolean isEndpointClass(ClassInfoModel model) {
        return model.getAnnotationsStream().anyMatch(annotation -> annotation
                .getName().equals(endpointAnnotationName));
    }

    public boolean isEntityClass(NodePath path) {
        var model = path.getModel();

        return model instanceof ClassInfoModel
                && isEntityClass((ClassInfoModel) model);
    }

    /**
     * Checks if at least one mention of the model is an entity class. Class
     * is considered as an entity if:
     * <ul>
     * <li>It is mentioned in endpoint method signature.</li>
     * <li>It is mentioned in another entity's field signature.</li>
     * <li>It is another entity's superclass or interface</li>
     * </ul>
     */
    public boolean isEntityClass(ClassInfoModel model) {
        var mentions = classMentions.get(model);

        return mentions != null
                && mentions.stream().map(NodePath::getAscendants)
                        .flatMap(Collection::stream)
                        .anyMatch(path -> isEndpointMethod(path)
                                || isEntityField(path)
                                || isEntitySuperClassOrInterface(model, path))
                && !model.isSynthetic() && !model.isIterable()
                && !model.isDate() && !model.isDateTime() && !model.isIterable()
                && !model.isMap();
    }

    private boolean isEndpointMethod(NodePath path) {
        return path.getModel() instanceof MethodInfoModel
                && isEndpointClass(path.getParent());
    }

    private boolean isEntityField(NodePath path) {
        return path.getModel() instanceof FieldInfoModel
                && !isEndpointClass(path.getParent());
    }

    private boolean isEntitySuperClassOrInterface(
            ClassInfoModel maybeSuperClassOrInterfaceModel, NodePath path) {
        if (!isEntityClass(path)) {
            return false;
        }

        var model = (ClassInfoModel) path.getModel();

        return model.getSuperClass().map(ClassRefSignatureModel::getClassInfo)
                .map(cls -> cls.equals(maybeSuperClassOrInterfaceModel))
                .orElse(false)
                || model.getInterfaces().stream()
                        .map(ClassRefSignatureModel::getClassInfo).anyMatch(
                                i -> i.equals(maybeSuperClassOrInterfaceModel));
    }
}
