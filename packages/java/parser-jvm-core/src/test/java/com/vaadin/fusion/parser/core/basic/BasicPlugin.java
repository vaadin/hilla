package com.vaadin.fusion.parser.core.basic;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.Plugin;
import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.RelativeFieldInfo;
import com.vaadin.fusion.parser.core.RelativeMethodInfo;
import com.vaadin.fusion.parser.core.SharedStorage;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;

public class BasicPlugin implements Plugin {
    public static final String STORAGE_KEY = "BasicPluginResult";

    @Override
    public void execute(@Nonnull Collection<RelativeClassInfo> endpoints,
            @Nonnull Collection<RelativeClassInfo> entities,
            SharedStorage storage) {
        storage.getPluginStorage().put(STORAGE_KEY,
                endpoints.stream().flatMap(endpoint -> Stream.of(
                        endpoint.getFieldsStream().map(RelativeFieldInfo::get)
                                .map(FieldInfo::getName),
                        endpoint.getMethodsStream().map(RelativeMethodInfo::get)
                                .map(MethodInfo::getName),
                        endpoint.getInnerClassesStream()
                                .map(RelativeClassInfo::get)
                                .map(ClassInfo::getName))
                        .flatMap(Function.identity()))
                        .collect(Collectors.toList()));
    }
}
