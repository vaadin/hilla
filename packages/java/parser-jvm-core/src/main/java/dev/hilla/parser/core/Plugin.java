package dev.hilla.parser.core;

import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.ClassInfoModel;

public interface Plugin {
    int getOrder();

    void setOrder(int order);

    default void setConfig(PluginConfiguration config) {
        if (config != null) {
            throw new IllegalArgumentException(String.format(
                    "The '%s' plugin does not expect configuration set",
                    getClass().getName()));
        }
    }

    void setStorage(@Nonnull SharedStorage storage);

    interface Processor extends Plugin {
        void process(@Nonnull Collection<ClassInfoModel> endpoints,
                @Nonnull Collection<ClassInfoModel> entities);
    }

    interface Transformer extends Plugin {
        @Nonnull
        Stream<ClassInfoModel> transform(
                @Nonnull Stream<ClassInfoModel> stream);
    }
}
