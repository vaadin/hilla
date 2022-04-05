package dev.hilla.parser.core;

import java.util.HashSet;
import java.util.function.Function;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;

public class ClassMappers extends HashSet<ClassMappers.Mapper> {
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

    @FunctionalInterface
    public interface Mapper extends Function<ClassInfoModel, ClassInfoModel> {
    }
}
