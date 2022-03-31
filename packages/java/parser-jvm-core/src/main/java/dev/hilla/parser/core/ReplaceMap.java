package dev.hilla.parser.core;

import java.util.HashMap;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;

public class ReplaceMap extends HashMap<String, ClassInfoModel> {
    ReplaceMap() {
        super();
    }

    @Override
    public ClassInfoModel put(String key, ClassInfoModel value) {
        return super.put(key, value);
    }

    public ClassInfoModel put(Class<?> key, ClassInfoModel value) {
        return put(key.getName(), value);
    }

    public ClassInfoModel replace(ClassInfoModel model) {
        return getOrDefault(model.getName(), model);
    }

    public ClassRefSignatureModel replace(ClassRefSignatureModel model) {
        var reference = model.resolve();
        model.setReference(replace(reference));
        return model;
    }

    public boolean containsKey(ClassInfoModel model) {
        return containsKey(model.getName());
    }
}
