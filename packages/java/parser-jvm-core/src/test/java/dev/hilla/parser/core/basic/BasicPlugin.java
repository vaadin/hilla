package dev.hilla.parser.core.basic;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.RelativeClassInfo;
import dev.hilla.parser.core.RelativeFieldInfo;
import dev.hilla.parser.core.RelativeMethodInfo;
import dev.hilla.parser.core.SharedStorage;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;

public class BasicPlugin implements Plugin {
    public static final String STORAGE_KEY = "BasicPluginResult";

    private int order = 0;

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

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }
}
