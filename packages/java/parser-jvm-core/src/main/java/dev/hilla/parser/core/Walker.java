package dev.hilla.parser.core;

import java.util.ArrayList;
import java.util.Arrays;
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
import dev.hilla.parser.models.Model;
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
        visit(target.getPackage(), null);
        target.getTypeParameters()
                .forEach(typeParameter -> visit(typeParameter, target));
        target.getSuperClass().ifPresent(cls -> visit(cls, target));
        target.getInterfaces().forEach(iface -> visit(iface, target));
        target.getFields().forEach(field -> visit(field, target));
        target.getMethods().forEach(method -> visit(method, target));
        target.getInnerClasses().forEach(cls -> visit(cls, target));
    }

    private List<Command> enter(Model model, Model parent) {
        try {
            var commands = new ArrayList<Command>();

            for (var visitor : visitors) {
                var command = visitor.enter(model, parent);
                commands.add(command);
                // If the current node is removed or replaced, we stop visitor
                // loop
                // immediately
                if (command instanceof Command.Replace
                        || command instanceof Command.Remove) {
                    return commands;
                }
            }

            return commands;
        } catch (Exception e) {
            throw new WalkerException(e);
        }
    }

    private void exit(Model model, Model parent) {
        try {
            for (var visitor : visitors) {
                visitor.exit(model, parent);
            }
        } catch (Exception e) {
            throw new WalkerException(e);
        }
    }

    private boolean hasNoRemovingCommand(Collection<Command> commands) {
        return commands.stream().noneMatch(Command::isRemovingCommand);
    }

    private void processCommandResult(Collection<Command> commands,
            Model parent) {
        // Added nodes will always be the current node siblings, so we use
        // `parent` variable here
        commands.stream().flatMap(cmd -> Arrays.stream(cmd.getContent()))
                .forEach(model -> unknownVisit(model, parent));
    }

    private void unknownVisit(Model model, Model parent) {
        if (model instanceof AnnotationInfoModel) {
            visit((AnnotationInfoModel) model, parent);
        } else if (model instanceof AnnotationParameterEnumValueModel) {
            visit((AnnotationParameterEnumValueModel) model, parent);
        } else if (model instanceof AnnotationParameterModel) {
            visit((AnnotationParameterModel) model, parent);
        } else if (model instanceof SignatureModel) {
            visit((SignatureModel) model, parent);
        } else if (model instanceof ClassInfoModel) {
            visit((ClassInfoModel) model, parent);
        } else if (model instanceof FieldInfoModel) {
            visit((FieldInfoModel) model, parent);
        } else if (model instanceof MethodInfoModel) {
            visit((MethodInfoModel) model, parent);
        } else if (model instanceof MethodParameterInfoModel) {
            visit((MethodParameterInfoModel) model, parent);
        } else if (model instanceof PackageInfoModel) {
            visit((PackageInfoModel) model, parent);
        }
    }

    private void visit(MethodParameterInfoModel model, Model parent) {
        var commands = enter(model, parent);

        if (hasNoRemovingCommand(commands)) {
            visit(model.getType(), model);
            exit(model, parent);
        }

        processCommandResult(commands, parent);
    }

    private void visit(AnnotationInfoModel model, Model parent) {
        var commands = enter(model, parent);

        if (hasNoRemovingCommand(commands)) {
            model.getParameters().forEach(parameter -> visit(parameter, model));
            exit(model, parent);
        }

        processCommandResult(commands, parent);
    }

    private void visit(AnnotationParameterModel model, Model parent) {
        var commands = enter(model, parent);

        if (hasNoRemovingCommand(commands)) {
            var value = model.getValue();

            if (value instanceof AnnotationParameterEnumValueModel) {
                visit((AnnotationParameterEnumValueModel) value, model);
            } else if (value instanceof ClassInfoModel) {
                visit((ClassInfoModel) value, model);
            }

            exit(model, parent);
        }

        processCommandResult(commands, parent);
    }

    private void visit(AnnotationParameterEnumValueModel model, Model parent) {
        var commands = enter(model, parent);

        if (hasNoRemovingCommand(commands)) {
            visit(model.getClassInfo(), model);
            exit(model, parent);
        }

        processCommandResult(commands, parent);
    }

    private void visit(PackageInfoModel model, Model parent) {
        var commands = enter(model, parent);

        if (hasNoRemovingCommand(commands)) {
            exit(model, parent);
        }

        processCommandResult(commands, parent);
    }

    private void visit(ClassInfoModel model, Model parent) {
        if (model.isJDKClass() || controller.isVisited(model)) {
            return;
        }

        var commands = enter(model, parent);

        if (hasNoRemovingCommand(commands)) {
            controller.register(model);
            exit(model, parent);
        }

        processCommandResult(commands, parent);
    }

    private void visit(FieldInfoModel model, Model parent) {
        var commands = enter(model, parent);

        if (hasNoRemovingCommand(commands)) {
            visit(model.getType(), model);
            exit(model, parent);
        }

        processCommandResult(commands, parent);
    }

    private void visit(MethodInfoModel model, Model parent) {
        var commands = enter(model, parent);

        if (hasNoRemovingCommand(commands)) {
            model.getParameters().forEach(param -> visit(param, model));
            visit(model.getResultType(), model);
            exit(model, parent);
        }

        processCommandResult(commands, parent);
    }

    private void visit(SignatureModel model, Model parent) {
        var commands = enter(model, parent);

        if (hasNoRemovingCommand(commands)) {
            if (model instanceof ArraySignatureModel) {
                visit(((ArraySignatureModel) model).getNestedType(), model);
            } else if (model instanceof ClassRefSignatureModel) {
                visit(((ClassRefSignatureModel) model).getClassInfo(), model);
                ((ClassRefSignatureModel) model).getTypeArguments()
                        .forEach(arg -> visit(arg, model));
            } else if (model instanceof TypeArgumentModel) {
                ((TypeArgumentModel) model).getAssociatedTypes()
                        .forEach(type -> visit(type, model));
            } else if (model instanceof TypeParameterModel) {
                ((TypeParameterModel) model).getBounds()
                        .forEach(type -> visit(type, model));
            } else if (model instanceof TypeVariableModel) {
                visit(((TypeVariableModel) model).resolve(), model);
            } // Skipping BaseSignatureModel because it could have no nested
              // signatures

            exit(model, parent);
        }

        processCommandResult(commands, parent);
    }
}
