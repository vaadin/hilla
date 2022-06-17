package dev.hilla.parser.core.basic;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.utils.Streams;

public class BasicPlugin implements Plugin.Processor {
    public static final String STORAGE_KEY = "BasicPluginResult";

    private int order = 0;
    private SharedStorage storage;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void process(@Nonnull Collection<ClassInfoModel> endpoints,
            @Nonnull Collection<ClassInfoModel> entities) {
        storage.getPluginStorage().put(STORAGE_KEY,
                endpoints.stream().flatMap(endpoint -> Streams.combine(
                        endpoint.getFieldsStream().map(FieldInfoModel::getName),
                        endpoint.getMethodsStream()
                                .map(MethodInfoModel::getName),
                        endpoint.getInnerClassesStream()
                                .map(ClassInfoModel::getName)))
                        .collect(Collectors.toList()));
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }
}
