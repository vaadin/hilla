package dev.hilla.parser.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.models.ClassInfoModel;

public class ReplaceMap extends HashMap<String, ClassInfoModel> {
    ReplaceMap() {
        super();
    }

    public ClassInfoModel put(Class<?> key, ClassInfoModel value) {
        return super.put(key.getName(), value);
    }

    public ScanElementsCollector replace(Collection<ClassInfoModel> endpoints,
            Collection<ClassInfoModel> entities) {
        return new ScanElementsCollector(
                replace(endpoints.stream()).collect(Collectors.toList()),
                replace(entities.stream()).collect(Collectors.toList()));
    }

    public Stream<ClassInfoModel> replace(Stream<ClassInfoModel> classes) {
        return classes.map(cls -> getOrDefault(cls.getName(), cls));
    }

    public Stream<ClassInfoModel> replace(Collection<ClassInfoModel> classes) {
        return replace(classes.stream());
    }

    public Stream<ClassInfoModel> replace(ClassInfoModel... classes) {
        return replace(Arrays.stream(classes));
    }
}
