package dev.hilla.parser.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.AnnotationParameterEnumValueModel;
import dev.hilla.parser.models.AnnotationParameterModel;
import dev.hilla.parser.models.ArraySignatureModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.MethodParameterInfoModel;
import dev.hilla.parser.models.PackageInfoModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.models.TypeArgumentModel;
import dev.hilla.parser.models.TypeParameterModel;
import dev.hilla.parser.models.TypeVariableModel;

final class Walker {
    private final DependencyController controller;
    private final SortedSet<Visitor> visitors = new TreeSet<>(
            Comparator.comparing(Visitor::getOrder));

    Walker(Collection<Visitor> visitors, DependencyController controller) {
        this.controller = controller;
        this.visitors.addAll(visitors);
    }

    public void traverse(ClassInfoModel target) {
        var packagePath = new Path<>(target.getPackage(), List.of());
        enter(packagePath);

        if (!packagePath.isRemoved()) {
            var classPath = new Path<>(target, List.of(packagePath), true);
            enter(classPath);

            if (!classPath.isRemoved()) {
                var ascendants = classPath.getAscendantsForChild();

                target.getTypeParametersStream()
                        .map(m -> new Path<SignatureModel>(m, ascendants))
                        .forEach(this::visitSignature);
                target.getSuperClass().map(m -> new Path<>(m, ascendants))
                        .ifPresent(this::visitClass);
                target.getInterfacesStream().map(m -> new Path<>(m, ascendants))
                        .forEach(this::visitClass);
                target.getFieldsStream().map(m -> new Path<>(m, ascendants))
                        .forEach(this::visitField);
                target.getMethodsStream().map(m -> new Path<>(m, ascendants))
                        .forEach(this::visitMethod);
                target.getInnerClassesStream()
                        .map(m -> new Path<>(m, ascendants))
                        .forEach(this::visitClass);
            }

            exit(classPath);
        }
        exit(packagePath);
    }

    private void enter(Path<?> path) {
        try {
            for (var visitor : visitors) {
                visitor.enter(path);
            }
        } catch (Exception e) {
            throw new WalkerException(e);
        }
    }

    private void exit(Path<?> path) {
        try {
            for (var visitor : visitors) {
                visitor.exit(path);
            }
        } catch (Exception e) {
            throw new WalkerException(e);
        }
    }

    private void visitAnnotation(Path<AnnotationInfoModel> path) {
        enter(path);

        if (!path.isRemoved()) {
            var model = path.getModel();
            var ascendants = path.getAscendantsForChild();

            model.getParametersStream().map(m -> new Path<>(m, ascendants))
                    .forEach(this::visitAnnotationParameter);
        }

        exit(path);

        path.forEachAddedNode(this::visitUnknown);
    }

    private void visitAnnotationParameter(Path<AnnotationParameterModel> path) {
        enter(path);

        if (!path.isRemoved()) {
            var model = path.getModel();
            var ascendants = path.getAscendantsForChild();
            var value = model.getValue();

            if (value instanceof AnnotationParameterEnumValueModel) {
                visitAnnotationParameterEnumValue(new Path<>(
                        (AnnotationParameterEnumValueModel) value, ascendants));
            } else if (value instanceof ClassInfoModel) {
                visitClass(new Path<>((ClassInfoModel) value, ascendants));
            }
        }

        exit(path);

        path.forEachAddedNode(this::visitUnknown);
    }

    private void visitAnnotationParameterEnumValue(
            Path<AnnotationParameterEnumValueModel> path) {
        enter(path);

        if (!path.isRemoved()) {
            var model = path.getModel();

            visitClass(new Path<>(model.getClassInfo(),
                    path.getAscendantsForChild()));
        }

        exit(path);

        path.forEachAddedNode(this::visitUnknown);
    }

    private void visitClass(Path<ClassInfoModel> path) {
        var model = path.getModel();

        if (model.isJDKClass() || controller.isVisited(model)) {
            return;
        }

        enter(path);

        if (!path.isRemoved()) {
            controller.register(model);
        }

        exit(path);

        path.forEachAddedNode(this::visitUnknown);
    }

    private void visitField(Path<FieldInfoModel> path) {
        enter(path);

        if (!path.isRemoved()) {
            visitSignature(new Path<>(path.getModel().getType(),
                    path.getAscendantsForChild()));
        }

        exit(path);

        path.forEachAddedNode(this::visitUnknown);
    }

    private void visitMethod(Path<MethodInfoModel> path) {
        enter(path);

        if (!path.isRemoved()) {
            var model = path.getModel();
            var ascendants = path.getAscendantsForChild();
            model.getParametersStream().map(m -> new Path<>(m, ascendants))
                    .forEach(this::visitMethodParameter);
            visitSignature(new Path<>(model.getResultType(), ascendants));
        }

        exit(path);

        path.forEachAddedNode(this::visitUnknown);
    }

    private void visitMethodParameter(Path<MethodParameterInfoModel> path) {
        enter(path);

        if (!path.isRemoved()) {
            visitSignature(new Path<>(path.getModel().getType(),
                    path.getAscendantsForChild()));
        }

        exit(path);

        path.forEachAddedNode(this::visitUnknown);
    }

    private void visitPackage(Path<PackageInfoModel> path) {
        enter(path);

        if (!path.isRemoved()) {
            exit(path);
        }

        path.forEachAddedNode(this::visitUnknown);
    }

    private void visitSignature(Path<SignatureModel> path) {
        enter(path);

        if (!path.isRemoved()) {
            var model = path.getModel();
            var ascendants = path.getAscendantsForChild();

            if (model instanceof ArraySignatureModel) {
                visitSignature(new Path<>(
                        ((ArraySignatureModel) model).getNestedType(),
                        ascendants));
            } else if (model instanceof ClassRefSignatureModel) {
                visitClass(new Path<>(
                        ((ClassRefSignatureModel) model).getClassInfo(),
                        ascendants));
                ((ClassRefSignatureModel) model).getTypeArgumentsStream()
                        .map(m -> new Path<SignatureModel>(m, ascendants))
                        .forEach(this::visitSignature);
            } else if (model instanceof TypeArgumentModel) {
                ((TypeArgumentModel) model).getAssociatedTypesStream()
                        .map(m -> new Path<>(m, ascendants))
                        .forEach(this::visitSignature);
            } else if (model instanceof TypeParameterModel) {
                ((TypeParameterModel) model).getBoundsStream()
                        .map(m -> new Path<>(m, ascendants))
                        .forEach(this::visitSignature);
            } else if (model instanceof TypeVariableModel) {
                visitSignature(new Path<>(((TypeVariableModel) model).resolve(),
                        ascendants));
            } // Skipping BaseSignatureModel because it could have no nested
              // signatures

            exit(path);
        }

        path.forEachAddedNode(this::visitUnknown);
    }

    private void visitUnknown(Path<?> path) {
        var model = path.getModel();

        if (model instanceof AnnotationInfoModel) {
            visitAnnotation((Path<AnnotationInfoModel>) path);
        } else if (model instanceof AnnotationParameterEnumValueModel) {
            visitAnnotationParameterEnumValue(
                    (Path<AnnotationParameterEnumValueModel>) path);
        } else if (model instanceof AnnotationParameterModel) {
            visitAnnotationParameter((Path<AnnotationParameterModel>) path);
        } else if (model instanceof SignatureModel) {
            visitSignature((Path<SignatureModel>) path);
        } else if (model instanceof ClassInfoModel) {
            visitClass((Path<ClassInfoModel>) path);
        } else if (model instanceof FieldInfoModel) {
            visitField((Path<FieldInfoModel>) path);
        } else if (model instanceof MethodInfoModel) {
            visitMethod((Path<MethodInfoModel>) path);
        } else if (model instanceof MethodParameterInfoModel) {
            visitMethodParameter((Path<MethodParameterInfoModel>) path);
        } else if (model instanceof PackageInfoModel) {
            visitPackage((Path<PackageInfoModel>) path);
        }
    }

}
