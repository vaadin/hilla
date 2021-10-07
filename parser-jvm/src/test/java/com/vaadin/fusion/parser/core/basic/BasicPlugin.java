package com.vaadin.fusion.parser.core.basic;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.fusion.parser.core.Plugin;
import com.vaadin.fusion.parser.core.RelativeClassList;
import com.vaadin.fusion.parser.core.SharedStorage;
import com.vaadin.fusion.parser.core.TestUtils;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;

public class BasicPlugin implements Plugin {
    public static final String STORAGE_KEY = "BasicPluginResult";

    protected final TestUtils.PluginElementsFilter filter = new TestUtils.PluginElementsFilter(
            "Basic");

    @Override
    public void execute(RelativeClassList endpoints, RelativeClassList entities,
            SharedStorage storage) {
        storage.getPluginStorage().put(STORAGE_KEY, filter.apply(endpoints)
                .stream()
                .flatMap(endpoint -> Stream
                        .of(endpoint.get().getFieldInfo().stream()
                                .map(FieldInfo::getName),
                                endpoint.get().getMethodInfo().stream()
                                        .map(MethodInfo::getName),
                                endpoint.get().getInnerClasses().stream()
                                        .map(ClassInfo::getName))
                        .flatMap(Function.identity()))
                .collect(Collectors.toList()));
    }
}
