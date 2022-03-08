package dev.hilla.parser.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Stream;

import dev.hilla.parser.models.ClassInfoModel;

public class ReplaceMap extends HashMap<String, ClassInfoModel> {
    ReplaceMap() {
      super();
    }

    public ClassInfoModel put(Class<?> key, ClassInfoModel value) {
        return super.put(key.getName(), value);
    }

    public Stream<ClassInfoModel> process(Stream<ClassInfoModel> classes) {
        return classes.map(cls -> getOrDefault(cls.getName(), cls));
    }

    public Stream<ClassInfoModel> process(Collection<ClassInfoModel> classes) {
        return process(classes.stream());
    }

    public Stream<ClassInfoModel> process(ClassInfoModel... classes) {
        return process(Arrays.stream(classes));
    }
}
