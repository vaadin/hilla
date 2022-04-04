package dev.hilla.parser.core;

import java.util.HashSet;
import java.util.function.BiPredicate;
import java.util.function.Function;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;

import io.github.classgraph.ClassInfo;

public class MappingRuleSet
        extends HashSet<Function<ClassInfoModel, ClassInfoModel>> {
    public static Function<ClassInfoModel, ClassInfoModel> createReplacer(
            String from, ClassInfoModel to) {
        return createReplacer(from, to, ClassInfoModel::isAssignableFrom);
    }

    public static Function<ClassInfoModel, ClassInfoModel> createReplacer(
            ClassInfo from, ClassInfoModel to) {
        return createReplacer(from, to, ClassInfoModel::isAssignableFrom);
    }

    public static Function<ClassInfoModel, ClassInfoModel> createReplacer(
            Class<?> from, ClassInfoModel to) {
        return createReplacer(from, to, ClassInfoModel::isAssignableFrom);
    }

    public static Function<ClassInfoModel, ClassInfoModel> createReplacer(
            ClassInfoModel from, ClassInfoModel to) {
        return createReplacer(from, to, ClassInfoModel::isAssignableFrom);
    }

    private static <T> Function<ClassInfoModel, ClassInfoModel> createReplacer(
            T from, ClassInfoModel to,
            BiPredicate<T, ClassInfoModel> predicate) {
        return cls -> predicate.test(from, cls) ? to : cls;
    }

    public ClassInfoModel map(ClassInfoModel model) {
        var result = model;

        for (var mapper : this) {
            result = mapper.apply(result);
        }

        return result;
    }

    public ClassRefSignatureModel map(ClassRefSignatureModel model) {
        var reference = model.resolve();
        model.setReference(map(reference));
        return model;
    }
}
