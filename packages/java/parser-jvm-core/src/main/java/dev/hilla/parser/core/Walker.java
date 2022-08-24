package dev.hilla.parser.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.AnnotationParameterEnumValueModel;
import dev.hilla.parser.models.AnnotationParameterModel;
import dev.hilla.parser.models.ArraySignatureModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.MethodParameterInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.models.PackageInfoModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.models.TypeArgumentModel;
import dev.hilla.parser.models.TypeParameterModel;
import dev.hilla.parser.models.TypeVariableModel;

public class Walker {
    private final Queue<ClassInfoModel> dependencies;
    private final Set<Model> visitedNodes;
    private final SortedSet<Visitor> visitors = new TreeSet<>(
            Comparator.comparing(Visitor::getOrder));

    public Walker(Collection<? extends Visitor> visitors,
            Collection<ClassInfoModel> dependencies) {
        this.dependencies = new LinkedList<>(dependencies);
        this.visitedNodes = new HashSet<>(dependencies);
        this.visitors.addAll(visitors);
    }

    // TODO: fixme
    // ClassGraph's TypeVariable throws an exception on resolve which is
    // probably a bug. This function fixes it by manual search the owner and
    // getting the correct type parameter.
    private static TypeParameterModel resolveTypeVariableFixed(
            TypeVariableModel model, List<NodePath> ascendants) {
        TypeParameterModel typeParameter;

        try {
            typeParameter = model.resolve();
        } catch (IllegalArgumentException e) {
            List<TypeParameterModel> typeParameters = null;

            for (var i = ascendants.size() - 1; i >= 0; i--) {
                var owner = ascendants.get(i).getModel();

                if (owner instanceof ClassInfoModel) {
                    typeParameters = ((ClassInfoModel) owner)
                            .getTypeParameters();
                    break;
                } else if (owner instanceof MethodInfoModel) {
                    typeParameters = ((MethodInfoModel) owner)
                            .getTypeParameters();
                    break;
                }
            }

            typeParameter = typeParameters != null
                    ? typeParameters.stream()
                            .filter(p -> p.getName().equals(model.getName()))
                            .findFirst().orElse(null)
                    : null;

            model.setTypeParameter(typeParameter);
        }

        return typeParameter;
    }

    public void traverse() {
        while (!dependencies.isEmpty()) {
            visitClassDeclaration(dependencies.poll());
        }
    }

    private void enter(NodePath path) {
        try {
            for (var visitor : visitors) {
                visitor.enter(path);
            }
        } catch (Exception e) {
            throw new WalkerException(e);
        }
    }

    private void exit(NodePath path) {
        try {
            for (var visitor : visitors) {
                visitor.exit(path);
            }
        } catch (Exception e) {
            throw new WalkerException(e);
        }
    }

    private void visitAnnotation(NodePath path) {
        enter(path);

        if (!path.isSkipped()) {
            var model = (AnnotationInfoModel) path.getModel();
            var ascendants = path.getAscendantsForChild();

            model.getParametersStream().map(m -> NodePath.of(m, ascendants))
                    .forEach(this::visitAnnotationParameter);
        }

        exit(path);
    }

    private void visitAnnotationParameter(NodePath path) {
        enter(path);

        if (!path.isSkipped()) {
            var model = (AnnotationParameterModel) path.getModel();
            var ascendants = path.getAscendantsForChild();
            var value = model.getValue();

            if (value instanceof AnnotationParameterEnumValueModel) {
                visitAnnotationParameterEnumValue(NodePath.of(
                        (AnnotationParameterEnumValueModel) value, ascendants));
            } else if (value instanceof ClassInfoModel) {
                visitClass(NodePath.of((ClassInfoModel) value, ascendants));
            }
        }

        exit(path);
    }

    private void visitAnnotationParameterEnumValue(NodePath path) {
        enter(path);

        if (!path.isSkipped()) {
            var model = (AnnotationParameterEnumValueModel) path.getModel();

            visitClass(NodePath.of(model.getClassInfo(),
                    path.getAscendantsForChild()));
        }

        exit(path);
    }

    private void visitClass(NodePath path) {
        var model = (ClassInfoModel) path.getModel();

        if (model.isJDKClass()) {
            return;
        }

        enter(path);

        if (!visitedNodes.contains(model) && !path.isSkipped()) {
            dependencies.add(model);
            visitedNodes.add(model);
        }

        exit(path);
    }

    private void visitClassDeclaration(ClassInfoModel target) {
        var packagePath = NodePath.of(target.getPackage(), List.of());
        enter(packagePath);

        if (!packagePath.isSkipped()) {
            var classPath = NodePath.of(target, List.of(packagePath), true);

            enter(classPath);

            if (!classPath.isSkipped()) {
                var ascendants = classPath.getAscendantsForChild();
                Function<Model, NodePath> mapper = m -> NodePath.of(m,
                        ascendants);

                target.getTypeParametersStream().map(mapper)
                        .forEach(this::visitSignature);
                target.getSuperClass().map(mapper)
                        .ifPresent(this::visitSignature);
                target.getInterfacesStream().map(mapper)
                        .forEach(this::visitSignature);
                target.getFieldsStream().map(mapper).forEach(this::visitField);
                target.getMethodsStream().map(mapper)
                        .forEach(this::visitMethod);
                target.getInnerClassesStream().map(mapper)
                        .forEach(this::visitClass);
            }

            exit(classPath);
        }
        exit(packagePath);
    }

    private void visitField(NodePath path) {
        enter(path);

        if (!path.isSkipped()) {
            var model = (FieldInfoModel) path.getModel();
            visitSignature(
                    NodePath.of(model.getType(), path.getAscendantsForChild()));
        }

        exit(path);
    }

    private void visitMethod(NodePath path) {
        enter(path);

        if (!path.isSkipped()) {
            var model = (MethodInfoModel) path.getModel();
            var ascendants = path.getAscendantsForChild();
            model.getParametersStream().map(m -> NodePath.of(m, ascendants))
                    .forEach(this::visitMethodParameter);
            visitSignature(NodePath.of(model.getResultType(), ascendants));
        }

        exit(path);
    }

    private void visitMethodParameter(NodePath path) {
        enter(path);

        if (!path.isSkipped()) {
            var model = (MethodParameterInfoModel) path.getModel();
            visitSignature(
                    NodePath.of(model.getType(), path.getAscendantsForChild()));
        }

        exit(path);
    }

    private void visitPackage(NodePath path) {
        enter(path);

        if (!path.isSkipped()) {
            exit(path);
        }
    }

    private void visitSignature(NodePath path) {
        enter(path);

        if (!path.isSkipped()) {
            var model = path.getModel();
            var ascendants = path.getAscendantsForChild();

            if (model instanceof ArraySignatureModel) {
                visitSignature(NodePath.of(
                        ((ArraySignatureModel) model).getNestedType(),
                        ascendants));
            } else if (model instanceof ClassRefSignatureModel) {
                visitClass(NodePath.of(
                        ((ClassRefSignatureModel) model).getClassInfo(),
                        ascendants));
                ((ClassRefSignatureModel) model).getTypeArgumentsStream()
                        .map(m -> NodePath.of(m, ascendants))
                        .forEach(this::visitSignature);
            } else if (model instanceof TypeArgumentModel) {
                ((TypeArgumentModel) model).getAssociatedTypesStream()
                        .map(m -> NodePath.of(m, ascendants))
                        .forEach(this::visitSignature);
            } else if (model instanceof TypeParameterModel) {
                ((TypeParameterModel) model).getBoundsStream()
                        .map(m -> NodePath.of(m, ascendants))
                        .forEach(this::visitSignature);
            } else if (model instanceof TypeVariableModel) {
                var typeParameter = resolveTypeVariableFixed(
                        (TypeVariableModel) model, ascendants);
                if (typeParameter != null) {
                    visitSignature(NodePath.of(typeParameter, ascendants));
                }
            } // Skipping BaseSignatureModel because it could have no nested
              // signatures

            exit(path);
        }
    }

    private void visitUnknown(NodePath path) {
        var model = path.getModel();

        if (model instanceof AnnotationInfoModel) {
            visitAnnotation(path);
        } else if (model instanceof AnnotationParameterEnumValueModel) {
            visitAnnotationParameterEnumValue(path);
        } else if (model instanceof AnnotationParameterModel) {
            visitAnnotationParameter(path);
        } else if (model instanceof SignatureModel) {
            visitSignature(path);
        } else if (model instanceof ClassInfoModel) {
            visitClass(path);
        } else if (model instanceof FieldInfoModel) {
            visitField(path);
        } else if (model instanceof MethodInfoModel) {
            visitMethod(path);
        } else if (model instanceof MethodParameterInfoModel) {
            visitMethodParameter(path);
        } else if (model instanceof PackageInfoModel) {
            visitPackage(path);
        }
    }

}
