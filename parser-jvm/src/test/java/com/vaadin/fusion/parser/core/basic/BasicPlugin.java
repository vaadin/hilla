package com.vaadin.fusion.parser.core.basic;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.Plugin;
import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.SharedStorage;
import com.vaadin.fusion.parser.testutils.PluginElementsFilter;

public class BasicPlugin implements Plugin {
    public static final String STORAGE_KEY = "BasicPluginResult";

    protected final PluginElementsFilter filter = new PluginElementsFilter(
            "Basic");

    @Override
    public void execute(@Nonnull List<RelativeClassInfo> endpoints,
            @Nonnull List<RelativeClassInfo> entities, SharedStorage storage) {
        storage.getPluginStorage().put(STORAGE_KEY, filter.apply(endpoints)
                .stream()
                .flatMap(endpoint -> Stream
                        .of(endpoint.getFields().stream()
                                .map(field -> field.get().getName()),
                                endpoint.getMethods().stream()
                                        .map(method -> method.get().getName()),
                                endpoint.getInnerClasses().stream()
                                        .map(cls -> cls.get().getName()))
                        .flatMap(Function.identity()))
                .collect(Collectors.toList()));
    }
}
