package com.vaadin.fusion.parser.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.vaadin.fusion.parser.Plugin;
import com.vaadin.fusion.parser.RelativeClassInfo;
import com.vaadin.fusion.parser.RelativeClassList;
import com.vaadin.fusion.parser.SharedStorage;
import com.vaadin.fusion.parser.TestUtils;

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
        RelativeClassList actualEndpoints = filter.apply(endpoints);
        List<String> endpointMemberNames = new ArrayList<>();

        for (RelativeClassInfo endpoint : actualEndpoints) {
            Stream.of(
                    endpoint.get().getFieldInfo().stream()
                            .map(FieldInfo::getName),
                    endpoint.get().getMethodInfo().stream()
                            .map(MethodInfo::getName),
                    endpoint.get().getInnerClasses().stream()
                            .map(ClassInfo::getName))
                    .flatMap(Function.identity())
                    .forEach(endpointMemberNames::add);
        }

        storage.getPluginStorage().put(STORAGE_KEY, endpointMemberNames);
    }
}
