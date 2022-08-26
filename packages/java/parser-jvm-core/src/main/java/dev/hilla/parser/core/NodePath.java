package dev.hilla.parser.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import dev.hilla.parser.models.AnnotatedModel;
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
import dev.hilla.parser.utils.Lists;

public abstract class NodePath {
    protected final Model model;
    private final List<NodePath> ascendants;
    private boolean skipped = false;

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

    private static <M extends Model> void removeOrReplaceInCollection(M model,
            Collection<M> siblings, M[] additionalNodes) {
        if (additionalNodes != null) {
            var _additions = Arrays.asList(additionalNodes);

            if (siblings instanceof List) {
                var _siblings = (List<M>) siblings;
                _siblings.addAll(_siblings.indexOf(model), _additions);
            } else {
                siblings.addAll(_additions);
            }
        }

        siblings.remove(model);
    }

    private static <M extends Model> void removeOrReplaceOptional(
            Consumer<Optional<M>> setter, M[] additionalNodes) {
        setter.accept(additionalNodes != null && additionalNodes.length != 0
                ? Optional.of(additionalNodes[0])
                : Optional.empty());
    }

    private static <M extends Model> void removeOrReplaceRequired(
            Consumer<M> setter, M[] additionalNodes) {
        if (additionalNodes == null || additionalNodes.length == 0) {
            throw new IllegalArgumentException("Cannot remove required node");
        }

        setter.accept(additionalNodes[0]);
    }

    @SafeVarargs
    public final <M extends Model> void replace(M... models) {
        removeOrReplace(models);
        skip();
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

    public boolean isSkipped() {
        return skipped;
    }

    public void remove() {
        removeOrReplace(null);
        skip();
    }

    public <M extends Model> void removeOrReplace(M[] additionalNodes) {
        if (model instanceof PackageInfoModel) {
            throw new IllegalArgumentException(
                    String.format("Cannot remove package %s",
                            ((PackageInfoModel) model).getName()));
        }

        var parent = getParent().getModel();

        if (parent instanceof AnnotatedModel
                && model instanceof AnnotationInfoModel) {
            removeOrReplaceInCollection((AnnotationInfoModel) model,
                    ((AnnotatedModel) parent).getAnnotations(),
                    (AnnotationInfoModel[]) additionalNodes);
        } else if (parent instanceof AnnotationInfoModel) {
            var _parent = (AnnotationInfoModel) parent;

            if (model instanceof AnnotationParameterModel) {
                removeOrReplaceInCollection((AnnotationParameterModel) model,
                        _parent.getParameters(),
                        (AnnotationParameterModel[]) additionalNodes);
            } else if (model instanceof ClassInfoModel) {
                removeOrReplaceOptional(_parent::setClassInfo,
                        (ClassInfoModel[]) additionalNodes);
            }
        } else if (parent instanceof AnnotationParameterEnumValueModel
                && model instanceof ClassInfoModel) {
            removeOrReplaceRequired(
                    ((AnnotationParameterEnumValueModel) parent)::setClassInfo,
                    (ClassInfoModel[]) additionalNodes);
        } else if (parent instanceof AnnotationParameterModel) {
            var _parent = (AnnotationParameterModel) parent;

            if (model instanceof ClassInfoModel) {
                removeOrReplaceRequired(_parent::setValue,
                        (ClassInfoModel[]) additionalNodes);
            } else if (model instanceof AnnotationParameterEnumValueModel) {
                removeOrReplaceRequired(_parent::setValue,
                        (AnnotationParameterEnumValueModel[]) additionalNodes);
            }
        } else if (parent instanceof ArraySignatureModel
                && model instanceof SignatureModel) {
            removeOrReplaceRequired(
                    ((ArraySignatureModel) parent)::setNestedType,
                    (SignatureModel[]) additionalNodes);
        } else if (parent instanceof ClassInfoModel) {
            var _parent = (ClassInfoModel) parent;

            if (model instanceof FieldInfoModel) {
                removeOrReplaceInCollection((FieldInfoModel) model,
                        _parent.getFields(),
                        (FieldInfoModel[]) additionalNodes);
            } else if (model instanceof MethodInfoModel) {
                removeOrReplaceInCollection((MethodInfoModel) model,
                        _parent.getMethods(),
                        (MethodInfoModel[]) additionalNodes);
            } else if (model instanceof ClassInfoModel) {
                removeOrReplaceInCollection((ClassInfoModel) model,
                        _parent.getInnerClasses(),
                        (ClassInfoModel[]) additionalNodes);
            } else if (model instanceof ClassRefSignatureModel) {
                var interfaces = _parent.getInterfaces();

                if (interfaces.contains(model)) {
                    removeOrReplaceInCollection((ClassRefSignatureModel) model,
                            interfaces,
                            (ClassRefSignatureModel[]) additionalNodes);
                } else {
                    removeOrReplaceOptional(_parent::setSuperClass,
                            (ClassRefSignatureModel[]) additionalNodes);
                }
            } else if (model instanceof TypeParameterModel) {
                removeOrReplaceInCollection((TypeParameterModel) model,
                        _parent.getTypeParameters(),
                        (TypeParameterModel[]) additionalNodes);
            }
        } else if (parent instanceof ClassRefSignatureModel) {
            if (model instanceof ClassInfoModel) {
                removeOrReplaceRequired(
                        ((ClassRefSignatureModel) parent)::setReference,
                        (ClassInfoModel[]) additionalNodes);
            } else if (model instanceof TypeArgumentModel) {
                removeOrReplaceInCollection((TypeArgumentModel) model,
                        ((ClassRefSignatureModel) parent).getTypeArguments(),
                        (TypeArgumentModel[]) additionalNodes);
            }
        } else if (parent instanceof FieldInfoModel
                && model instanceof SignatureModel) {
            removeOrReplaceRequired(((FieldInfoModel) parent)::setType,
                    (SignatureModel[]) additionalNodes);
        } else if (parent instanceof MethodInfoModel) {
            var _parent = (MethodInfoModel) parent;

            if (model instanceof MethodParameterInfoModel) {
                removeOrReplaceInCollection((MethodParameterInfoModel) model,
                        _parent.getParameters(),
                        (MethodParameterInfoModel[]) additionalNodes);
            } else if (model instanceof TypeParameterModel) {
                removeOrReplaceInCollection((TypeParameterModel) model,
                        _parent.getTypeParameters(),
                        (TypeParameterModel[]) additionalNodes);
            } else if (model instanceof SignatureModel) {
                removeOrReplaceRequired(_parent::setResultType,
                        (SignatureModel[]) additionalNodes);
            }
        } else if (parent instanceof MethodParameterInfoModel
                && model instanceof SignatureModel) {
            removeOrReplaceRequired(
                    ((MethodParameterInfoModel) parent)::setType,
                    (SignatureModel[]) additionalNodes);
        } else if (parent instanceof TypeArgumentModel
                && model instanceof SignatureModel) {
            removeOrReplaceInCollection((SignatureModel) model,
                    ((TypeArgumentModel) parent).getAssociatedTypes(),
                    (SignatureModel[]) additionalNodes);
        } else if (parent instanceof TypeParameterModel
                && model instanceof SignatureModel) {
            removeOrReplaceInCollection((SignatureModel) model,
                    ((TypeParameterModel) parent).getBounds(),
                    (SignatureModel[]) additionalNodes);
        } else if (parent instanceof TypeVariableModel
                && model instanceof TypeParameterModel) {
            removeOrReplaceRequired(
                    ((TypeVariableModel) parent)::setTypeParameter,
                    (TypeParameterModel[]) additionalNodes);
        }
    }

    public void skip() {
        skipped = true;
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
